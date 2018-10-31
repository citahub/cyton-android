package org.nervos.neuron.service.httpservice;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.NervosjFactory;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.methods.request.Call;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.protocol.system.NervosjSysContract;
import org.nervos.neuron.BuildConfig;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.SaveAppChainPendingItemUtils;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AppChainRpcService {

    private static Nervosj service;

    private static Random random;
    private static int version = 0;
    private static WalletItem walletItem;

    public static void init(Context context, String httpProvider) {
        HttpService.setDebug(BuildConfig.IS_DEBUG);
        service = NervosjFactory.build(new HttpService(httpProvider));
        walletItem = DBWalletUtil.getCurrentWallet(context);
    }

    public static void init(Context context) {
        walletItem = DBWalletUtil.getCurrentWallet(context);
    }

    public static void setHttpProvider(String httpProvider) {
        service = NervosjFactory.build(new HttpService(httpProvider));
    }

    private static BigInteger randomNonce() {
        random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }


    public static TokenItem getErc20TokenInfo(String contractAddress) {
        try {
            return new TokenItem(getErc20Name(contractAddress),
                    getErc20Symbol(contractAddress), getErc20Decimals(contractAddress),
                    contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static double getErc20Balance(TokenItem tokenItem, String address) throws Exception {
        Call balanceCall = new Call(address, tokenItem.contractAddress,
                ConstUtil.BALANCE_OF_HASH + ConstUtil.ZERO_16 + Numeric.cleanHexPrefix(address));
        String balanceOf = service.appCall(balanceCall,
                DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(balanceOf) && !ConstUtil.RPC_RESULT_ZERO.equals(balanceOf)) {
            initIntTypes();
            Int256 balance = (Int256) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
            double balances = balance.getValue().doubleValue();
            if (tokenItem.decimals == 0) return balances;
            else return balances / (Math.pow(10, tokenItem.decimals));
        }
        return 0.0;
    }


    public static AppMetaData getMetaData() {
        try {
            return service.appMetaData(DefaultBlockParameterName.LATEST).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static double getBalance(String address) throws Exception {
        AppGetBalance appGetBalance =
                service.appGetBalance(address, DefaultBlockParameterName.LATEST).send();
        if (appGetBalance != null) {
            return NumberUtil.getEthFromWei(appGetBalance.getBalance());
        }
        return 0.0;
    }

    public static Observable<String> getQuotaPrice(String from) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {
                    String price = new NervosjSysContract(service).getQuotaPrice(from).getValue();
                    price = price.equals("0x")? "1" : price;
                    return price;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "0";
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<AppSendTransaction> transferErc20(Context context, TokenItem tokenItem,
           String contractAddress, String address, double value, long quota, int chainId, String password){
        BigInteger ercValue = getERC20TransferValue(tokenItem, value);
        String data = createTokenTransferData(Numeric.cleanHexPrefix(address), ercValue);
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                return getValidUntilBlock();
            }
        }).flatMap(new Func1<BigInteger, Observable<AppSendTransaction>>() {
            @Override
            public Observable<AppSendTransaction> call(BigInteger validUntilBlock) {
                Transaction transaction = Transaction.createFunctionCallTransaction(
                        NumberUtil.toLowerCaseWithout0x(contractAddress),
                        randomNonce(), quota, validUntilBlock.longValue(),
                        version, chainId, BigInteger.ZERO.toString(), data);
                try {
                    String privateKey = NumberUtil.toLowerCaseWithout0x(
                            WalletEntity.fromKeyStore(password, walletItem.keystore).getPrivateKey());
                    String rawTx = transaction.sign(privateKey, false, false);
                    AppSendTransaction appSendTransaction = service.appSendRawTransaction(rawTx).send();
                    SaveAppChainPendingItemUtils.saveItem(context, appSendTransaction.getSendTransactionResult().getHash(), validUntilBlock.longValue() + "");
                    return Observable.just(appSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());

    }


    public static Observable<AppSendTransaction> transferAppChain(Context context, String toAddress, double value,
                                       String data, long quota, int chainId,  String password) {
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                return getValidUntilBlock();
            }
        }).flatMap(new Func1<BigInteger, Observable<AppSendTransaction>>() {
            @Override
            public Observable<AppSendTransaction> call(BigInteger validUntilBlock) {
                Transaction transaction = Transaction.createFunctionCallTransaction(
                        NumberUtil.toLowerCaseWithout0x(toAddress),
                        randomNonce(), quota ,
                        validUntilBlock.longValue(), version, chainId,
                        NumberUtil.getWeiFromEth(value).toString(), TextUtils.isEmpty(data) ? "" : data);
                try {
                    String privateKey = NumberUtil.toLowerCaseWithout0x(
                            WalletEntity.fromKeyStore(password, walletItem.keystore).getPrivateKey());
                    String rawTx = transaction.sign(privateKey, false, false);
                    AppSendTransaction appSendTransaction = service.appSendRawTransaction(rawTx).send();
                    SaveAppChainPendingItemUtils.saveItem(context, appSendTransaction.getSendTransactionResult().getHash(), validUntilBlock.longValue() + "");
                    return Observable.just(appSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static TransactionReceipt getTransactionReceipt(String hash) throws IOException {
        return service.appGetTransactionReceipt(hash).send().getTransactionReceipt();
    }

    private static BigInteger getValidUntilBlock() {
        try {
            return BigInteger.valueOf((service.appBlockNumber().send())
                    .getBlockNumber().longValue() + ConstUtil.VALID_BLOCK_NUMBER_DIFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigInteger.ZERO;
    }

    public static BigInteger getBlockNumber() throws IOException {
        return (service.appBlockNumber().send()).getBlockNumber();
    }

    private static final String TRANSFER_METHOD = "transfer";

    private static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });
        Function function = new Function(TRANSFER_METHOD, params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    private static BigInteger getERC20TransferValue(TokenItem tokenItem, double value) {
        StringBuilder sb = new StringBuilder("1");
        for (int i = 0; i < tokenItem.decimals; i++) {
            sb.append("0");
        }
        BigInteger ERC20Decimal = new BigInteger(sb.toString());
        return ERC20Decimal.multiply(BigInteger.valueOf((long) (ConstUtil.LONG_6 * value)))
                .divide(BigInteger.valueOf(ConstUtil.LONG_6));
    }

    private static String getErc20Name(String contractAddress) throws Exception {
        Call nameCall = new Call(walletItem.address, contractAddress, ConstUtil.NAME_HASH);
        String name = service.appCall(nameCall, DefaultBlockParameterName.LATEST)
                .send().getValue();
        if (TextUtils.isEmpty(name) || ConstUtil.RPC_RESULT_ZERO.equals(name)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();
    }


    private static String getErc20Symbol(String contractAddress) throws Exception {
        Call symbolCall = new Call(walletItem.address, contractAddress, ConstUtil.SYMBOL_HASH);
        String symbol = service.appCall(symbolCall, DefaultBlockParameterName.LATEST)
                .send().getValue();
        if (TextUtils.isEmpty(symbol) || ConstUtil.RPC_RESULT_ZERO.equals(symbol)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();
    }

    private static int getErc20Decimals(String contractAddress) throws Exception {
        Call decimalsCall = new Call(walletItem.address, contractAddress, ConstUtil.DECIMALS_HASH);
        String decimals = service.appCall(decimalsCall,
                DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(decimals) && !ConstUtil.RPC_RESULT_ZERO.equals(decimals)) {
            initIntTypes();
            Int256 type = (Int256) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
            return type.getValue().intValue();
        }
        return 0;
    }

    private static List<TypeReference<Type>> intTypes = new ArrayList<>();

    private static void initIntTypes() {
        intTypes.clear();
        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int256.class;
            }
        });
    }

    private static List<TypeReference<Type>> stringTypes = new ArrayList<>();

    private static void initStringTypes() {
        stringTypes.clear();
        stringTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Utf8String.class;
            }
        });
    }

}
