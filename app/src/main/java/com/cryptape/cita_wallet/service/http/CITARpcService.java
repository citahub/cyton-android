package com.cryptape.cita_wallet.service.http;

import android.content.Context;
import android.text.TextUtils;

import com.cryptape.cita.protocol.CITAj;
import com.cryptape.cita.protocol.core.DefaultBlockParameterName;
import com.cryptape.cita.protocol.core.methods.request.Call;
import com.cryptape.cita.protocol.core.methods.request.Transaction;
import com.cryptape.cita.protocol.core.methods.response.AppMetaData;
import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.cryptape.cita.protocol.core.methods.response.AppTransaction;
import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;
import com.cryptape.cita.protocol.system.CITAjSystemContract;
import com.cryptape.cita.protocol.http.HttpService;

import com.cryptape.cita_wallet.BuildConfig;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.transaction.RpcTransaction;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.crypto.WalletEntity;
import com.cryptape.cita_wallet.util.db.CITATransactionsUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.exception.TransactionErrorException;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by duanyytop on 2018/4/17
 */
public class CITARpcService {

    private static final String TRANSFER_FAIL = "Transfer fail";
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static CITAj service;

    private static Random random;
    private static Wallet wallet;

    public static void init(Context context, String httpProvider) {
        HttpService.setDebug(BuildConfig.IS_DEBUG);
        service = CITAj.build(new HttpService(httpProvider));
        wallet = DBWalletUtil.getCurrentWallet(context);
    }

    public static void init(Context context) {
        wallet = DBWalletUtil.getCurrentWallet(context);
    }

    public static void setHttpProvider(String httpProvider) {
        service = CITAj.build(new HttpService(httpProvider));
    }

    private static String randomNonce() {
        random = new Random(System.currentTimeMillis());
        return String.valueOf(Math.abs(random.nextLong()));
    }


    public static Token getErc20TokenInfo(String contractAddress) {
        try {
            return new Token(getErc20Name(contractAddress), getErc20Symbol(contractAddress), getErc20Decimals(contractAddress), contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Observable<Double> getErc20Balance(Token token, String address) {
        return Observable.fromCallable(new Callable<Double>() {
            @Override
            public Double call() throws IOException {
                Call balanceCall = new Call(address, token.contractAddress,
                        ConstantUtil.BALANCE_OF_HASH + ConstantUtil.ZERO_16 + Numeric.cleanHexPrefix(address));
                String balanceOf = service.appCall(balanceCall, DefaultBlockParameterName.LATEST).send().getValue();
                if (!TextUtils.isEmpty(balanceOf) && !ConstantUtil.RPC_RESULT_ZERO.equals(balanceOf)) {
                    initIntTypes();
                    Int256 balance = (Int256) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
                    double balances = balance.getValue().doubleValue();
                    if (token.decimals == 0) return balances;
                    else return balances / (Math.pow(10, token.decimals));
                }
                return 0.0;
            }
        }).subscribeOn(Schedulers.io());

    }


    public static AppMetaData getMetaData() {
        try {
            return service.appMetaData(DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static AppTransaction getTransactionByHash(String hash) {
        try {
            return service.appGetTransactionByHash(hash).send();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Observable<Double> getBalance(String address) {
        return Observable.fromCallable(() -> service.appGetBalance(address, DefaultBlockParameterName.LATEST).send())
                .filter(appGetBalance -> appGetBalance != null)
                .map(appGetBalance -> NumberUtil.getEthFromWei(appGetBalance.getBalance()))
                .subscribeOn(Schedulers.io());
    }

    public static Observable<String> getQuotaPrice(String from) {
        return Observable.fromCallable(() -> {
            try {
                return BigInteger.valueOf(new CITAjSystemContract(service).getQuotaPrice(from)).toString();
            } catch (Exception e) {
                e.printStackTrace();
                Observable.error(e);
            }
            return ConstantUtil.QUOTA_PRICE_DEFAULT;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<AppSendTransaction> transferErc20(Context context, Token token, String address, String value, long quota, BigInteger chainId, String password) {
        String data = createTokenTransferData(Numeric.cleanHexPrefix(address), getERC20TransferValue(token, value));

        return getValidUntilBlock().flatMap((Func1<BigInteger, Observable<AppSendTransaction>>) validUntilBlock -> {
            try {

                AppMetaData.AppMetaDataResult appMetaDataResult = Objects.requireNonNull(getMetaData()).getAppMetaDataResult();

                Transaction transaction = Transaction.createFunctionCallTransaction(NumberUtil.toLowerCaseWithout0x(token.contractAddress), randomNonce(), quota, validUntilBlock
                        .longValue(), appMetaDataResult.getVersion(), chainId, "0", TextUtils.isEmpty(data) ? "" : data);

                AppSendTransaction appSendTransaction = signTransaction(transaction, password);

                if (appSendTransaction.getError() != null) {
                    Observable.error(new TransactionErrorException(appSendTransaction.getError().getMessage()));
                } else {
                    saveLocalTransaction(context, wallet.address, address, String.valueOf(value), validUntilBlock.longValue(), chainId.toString(), token.contractAddress, appSendTransaction
                            .getSendTransactionResult()
                            .getHash(), String.valueOf(quota));
                }
                return Observable.just(appSendTransaction);
            } catch (Exception e) {
                e.printStackTrace();
                Observable.error(e);
            }
            return Observable.error(new Throwable(TRANSFER_FAIL));
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<AppSendTransaction> transferCITA(Context context, String toAddress, String value, String data, long quota, BigInteger chainId, String password) {

        return getValidUntilBlock().flatMap((Func1<BigInteger, Observable<AppSendTransaction>>) validUntilBlock -> {
            try {

                AppMetaData.AppMetaDataResult appMetaDataResult = Objects.requireNonNull(getMetaData()).getAppMetaDataResult();

                Transaction transaction = Transaction.createFunctionCallTransaction(NumberUtil.toLowerCaseWithout0x(toAddress), randomNonce(), quota, validUntilBlock
                        .longValue(), appMetaDataResult.getVersion(), chainId, NumberUtil.getWeiFromEth(value)
                        .toString(), TextUtils.isEmpty(data) ? "" : data);

                AppSendTransaction appSendTransaction = signTransaction(transaction, password);

                if (appSendTransaction.getError() != null) {
                    Observable.error(new TransactionErrorException(appSendTransaction.getError().getMessage()));
                } else {
                    saveLocalTransaction(context, wallet.address, toAddress, String.valueOf(value), validUntilBlock.longValue(), chainId.toString(), "", appSendTransaction
                            .getSendTransactionResult()
                            .getHash(), String.valueOf(quota));
                }
                return Observable.just(appSendTransaction);
            } catch (Exception e) {
                e.printStackTrace();
                Observable.error(e);
            }
            return Observable.error(new Throwable(TRANSFER_FAIL));
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static AppSendTransaction signTransaction(Transaction transaction, String password) throws Exception {
        WalletEntity walletEntity = WalletEntity.fromKeyStore(password, wallet.keystore);
        String privateKey = NumberUtil.toLowerCaseWithout0x(walletEntity.getPrivateKey());
        String rawTx = transaction.sign(privateKey, Transaction.CryptoTx.SECP256K1, false);
        return service.appSendRawTransaction(rawTx).send();
    }


    private static void saveLocalTransaction(Context context, String from, String to, String value, long validUntilBlock, String chainId, String contractAddress, String hash, String limit) {
        executorService.execute(() -> {
            String chainName = Objects.requireNonNull(DBWalletUtil.getChainItemFromCurrentWallet(context, chainId)).name;
            RpcTransaction item = new RpcTransaction(from, to, value, chainId, chainName, RpcTransaction.PENDING, System.currentTimeMillis(), hash);
            item.gasLimit = limit;
            item.validUntilBlock = String.valueOf(validUntilBlock);
            item.contractAddress = contractAddress;
            CITATransactionsUtil.save(context, item);
        });

    }

    public static TransactionReceipt getTransactionReceipt(String hash) {
        try {
            return service.appGetTransactionReceipt(hash).send().getTransactionReceipt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Observable<BigInteger> getValidUntilBlock() {
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                return BigInteger.valueOf(
                        (service.appBlockNumber().send()).getBlockNumber().longValue() + ConstantUtil.VALID_BLOCK_NUMBER_DIFF);
            }
        });
    }

    public static BigInteger getBlockNumber() {
        try {
            return (service.appBlockNumber().send()).getBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BigInteger.ZERO;
    }

    private static final String TRANSFER_METHOD = "transfer";

    private static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {});
        Function function = new Function(TRANSFER_METHOD, params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    private static BigInteger getERC20TransferValue(Token token, String value) {
        return BigDecimal.TEN.pow(token.decimals).multiply(new BigDecimal(value)).toBigInteger();
    }

    private static String getErc20Name(String contractAddress) throws Exception {
        Call nameCall = new Call(wallet.address, contractAddress, ConstantUtil.NAME_HASH);
        String name = service.appCall(nameCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(name) || ConstantUtil.RPC_RESULT_ZERO.equals(name)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();
    }


    private static String getErc20Symbol(String contractAddress) throws Exception {
        Call symbolCall = new Call(wallet.address, contractAddress, ConstantUtil.SYMBOL_HASH);
        String symbol = service.appCall(symbolCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(symbol) || ConstantUtil.RPC_RESULT_ZERO.equals(symbol)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();
    }

    private static int getErc20Decimals(String contractAddress) throws Exception {
        Call decimalsCall = new Call(wallet.address, contractAddress, ConstantUtil.DECIMALS_HASH);
        String decimals = service.appCall(decimalsCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(decimals) && !ConstantUtil.RPC_RESULT_ZERO.equals(decimals)) {
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
