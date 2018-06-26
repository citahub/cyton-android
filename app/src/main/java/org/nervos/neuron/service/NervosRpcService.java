package org.nervos.neuron.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.Web3j;
import org.nervos.web3j.protocol.account.Account;
import org.nervos.web3j.protocol.account.CompiledContract;
import org.nervos.web3j.protocol.core.DefaultBlockParameter;
import org.nervos.web3j.protocol.core.methods.request.Call;
import org.nervos.web3j.protocol.core.methods.request.Transaction;
import org.nervos.web3j.protocol.core.methods.response.AbiDefinition;
import org.nervos.web3j.protocol.core.methods.response.EthBlockNumber;
import org.nervos.web3j.protocol.core.methods.response.EthGetBalance;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;
import org.nervos.web3j.protocol.core.methods.response.EthSendTransaction;
import org.nervos.web3j.protocol.http.HttpService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Numeric;


import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.security.auth.callback.Callback;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NervosRpcService {

    private static Web3j service;

    private static Random random;
    private static BigInteger quota = BigInteger.valueOf(1000000);
    private static int version = 0;
    private static int chainId = 1;
    private static WalletItem walletItem;

    public static void init(Context context, String httpProvider) {
        HttpService.setDebug(true);
        service = Web3j.build(new HttpService(httpProvider));
        walletItem = DBWalletUtil.getCurrentWallet(context);
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


    public static double getErc20Balance(TokenItem tokenItem, String address) {
        try {
            Call balanceCall = new Call(address, tokenItem.contractAddress,
                    ConstantUtil.BALANCEOF_HASH + ConstantUtil.ZERO_16 + Numeric.cleanHexPrefix(address));
            String balanceOf = service.ethCall(balanceCall,
                    DefaultBlockParameter.valueOf("latest")).send().getValue();
            if (!TextUtils.isEmpty(balanceOf) && !ConstantUtil.RPC_RESULT_ZERO.equals(balanceOf)) {
                initIntTypes();
                Int64 balance = (Int64) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
                double balances = balance.getValue().doubleValue();
                if (tokenItem.decimals == 0) return balances;
                else return balances/(Math.pow(10, tokenItem.decimals));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public static EthMetaData getMetaData() {
        try {
            return service.ethMetaData(DefaultBlockParameter.valueOf("latest")).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static double getBalance(String address) {
        try {
            EthGetBalance ethGetBalance =
                    service.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
            return ethGetBalance.getBalance().multiply(BigInteger.valueOf(10000))
                    .divide(ConstantUtil.NervosDecimal).doubleValue()/10000.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem,
           String contractAddress, String address, double value, String password) throws Exception {
        BigInteger ercValue = getTransferValue(tokenItem, value);
        String data = createTokenTransferData(address, ercValue);
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                return getValidUntilBlock();
            }
        }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger validUntilBlock) {
                Transaction transaction = Transaction.createFunctionCallTransaction(contractAddress,
                        randomNonce(), quota.longValue(), validUntilBlock.longValue(),
                        version, chainId, BigInteger.ZERO, data);
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    String rawTx = transaction.sign(privateKey);
                    return Observable.just(service.ethSendRawTransaction(rawTx).send());
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public static Observable<EthSendTransaction> transferNervos(String toAddress, double value, String password) {
        BigInteger transferValue = ConstantUtil.NervosDecimal
                .multiply(BigInteger.valueOf((long)(10000*value))).divide(BigInteger.valueOf(10000));
        LogUtil.d("transfer value: " + transferValue.toString());
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() {
                return getValidUntilBlock();
            }
        }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger validUntilBlock) {
                Transaction transaction = Transaction.createFunctionCallTransaction(toAddress, randomNonce(), quota.longValue(),
                        validUntilBlock.longValue(), version, chainId, transferValue, "");
                try {
                    String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                    String rawTx = transaction.sign(privateKey);
                    return Observable.just(service.ethSendRawTransaction(rawTx).send());
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    private static BigInteger getValidUntilBlock() {
        try {
            return BigInteger.valueOf((service.ethBlockNumber().send())
                    .getBlockNumber().longValue() + ConstantUtil.VALID_BLOCK_NUMBER_DIFF);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BigInteger.ZERO;
    }

    private static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {});
        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    private static BigInteger getTransferValue(TokenItem tokenItem, double value) {
        StringBuilder sb = new StringBuilder("1");
        for(int i = 0; i < tokenItem.decimals; i++) {
            sb.append("0");
        }
        BigInteger ERC20Decimal = new BigInteger(sb.toString());
        return ERC20Decimal.multiply(BigInteger.valueOf((long)(10000*value)))
                .divide(BigInteger.valueOf(10000));
    }

    private static String getErc20Name(String contractAddress) throws IOException {
        Call nameCall = new Call(walletItem.address, contractAddress, ConstantUtil.NAME_HASH);
        String name = service.ethCall(nameCall, DefaultBlockParameter.valueOf("latest"))
                .send().getValue();
        if (TextUtils.isEmpty(name) || ConstantUtil.RPC_RESULT_ZERO.equals(name)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();
    }


    private static String getErc20Symbol(String contractAddress) throws IOException {
        Call symbolCall = new Call(walletItem.address, contractAddress, ConstantUtil.SYMBOL_HASH);
        String symbol = service.ethCall(symbolCall, DefaultBlockParameter.valueOf("latest"))
                .send().getValue();
        if (TextUtils.isEmpty(symbol) || ConstantUtil.RPC_RESULT_ZERO.equals(symbol)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();
    }

    private static int getErc20Decimals(String contractAddress) throws IOException {
        Call decimalsCall = new Call(walletItem.address, contractAddress, ConstantUtil.DECIMALS_HASH);
        String decimals = service.ethCall(decimalsCall,
                DefaultBlockParameter.valueOf("latest")).send().getValue();
        if (!TextUtils.isEmpty(decimals) && !ConstantUtil.RPC_RESULT_ZERO.equals(decimals)) {
            initIntTypes();
            Int64 type = (Int64) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
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
                return Int64.class;
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
