package org.nervos.neuron.service.http;


import android.content.Context;
import android.text.TextUtils;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.util.url.HttpEtherUrls;
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
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.infura.InfuraHttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by duanyytop on 2018/4/17
 */
public class EthRpcService {

    private static final String TRANSFER_FAIL = "Transfer fail";
    private static final String SIGN_FAIL = "Sign fail";

    private static WalletItem walletItem;
    private static Web3j service;

    public static void init(Context context) {
        service = Web3jFactory.build(new InfuraHttpService(EtherUtil.getEthNodeUrl()));
        walletItem = DBWalletUtil.getCurrentWallet(context);
    }

    public static void initHttp() {
        service = Web3jFactory.build(new InfuraHttpService(EtherUtil.getEthNodeUrl()));
    }

    public static double getEthBalance(String address) throws Exception {
        EthGetBalance ethGetBalance = service.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        if (ethGetBalance != null) {
            return NumberUtil.getEthFromWei(ethGetBalance.getBalance());
        }
        return 0.0;
    }

    public static Observable<BigInteger> getEthGasPrice() {
        return Observable.fromCallable(() ->
                service.ethGasPrice().send().getGasPrice())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static BigInteger getBlockNumber() {
        try {
            return service.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
    }

    public static Observable<BigInteger> getEthGasLimit(TransactionInfo transactionInfo) {
        String data = TextUtils.isEmpty(transactionInfo.data) ? "" : Numeric.prependHexPrefix(transactionInfo.data);
        Transaction transaction = new Transaction(walletItem.address, null, null, null,
                Numeric.prependHexPrefix(transactionInfo.to), transactionInfo.getBigIntegerValue(), data);
        return Observable.fromCallable(() -> {
            try {
                return service.ethEstimateGas(transaction).send().getAmountUsed();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ConstantUtil.GAS_ERC20_LIMIT;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *
     * @param address
     * @param value
     * @param gasPrice
     * @param gasLimit
     * @param data
     * @param password
     * @return
     */
    public static Observable<EthSendTransaction> transferEth(String address, double value, BigInteger gasPrice,
                                                             BigInteger gasLimit, String data, String password) {
        gasLimit = gasLimit.equals(BigInteger.ZERO) ? ConstantUtil.GAS_LIMIT : gasLimit;
        return signRawTransaction(address, data == null ? "" : data, NumberUtil.getWeiFromEth(value), gasPrice, gasLimit, password)
                .flatMap((Func1<String, Observable<EthSendTransaction>>) hexValue -> {
                    try {
                        return Observable.just(service.ethSendRawTransaction(hexValue).sendAsync().get());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Observable.error(e);
                    }
                    return Observable.error(new Throwable(TRANSFER_FAIL));
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * get standard erc20 token info through function hash and parameters
     */
    public static TokenItem getTokenInfo(String contractAddress, String address) {
        try {
            return new TokenItem(getErc20Name(address, contractAddress), getErc20Symbol(address, contractAddress),
                    getErc20Decimal(address, contractAddress), contractAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EthGetTransactionReceipt getTransactionReceipt(String hash) {
        try {
            return service.ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double getERC20Balance(String contractAddress, String address) throws Exception {
        long decimal = getErc20Decimal(address, contractAddress);
        Transaction balanceCall = Transaction.createEthCallTransaction(address, contractAddress,
                ConstantUtil.BALANCE_OF_HASH + ConstantUtil.ZERO_16 + Numeric.cleanHexPrefix(address));
        String balanceOf = service.ethCall(balanceCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (!TextUtils.isEmpty(balanceOf) && !ConstantUtil.RPC_RESULT_ZERO.equals(balanceOf)) {
            initIntTypes();
            Int256 balance = (Int256) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
            double balances = balance.getValue().doubleValue();
            if (decimal == 0) return balance.getValue().doubleValue();
            else return balances / (Math.pow(10, decimal));
        }
        return 0.0;
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem, String address, double value,
                                                               BigInteger gasPrice, BigInteger gasLimit, String password) {
        gasLimit = gasLimit.equals(BigInteger.ZERO) ? ConstantUtil.GAS_ERC20_LIMIT : gasLimit;
        String data = createTokenTransferData(address, createTransferValue(tokenItem, value));
        return signRawTransaction(tokenItem.contractAddress, data, gasPrice, gasLimit, password)
                .flatMap((Func1<String, Observable<EthSendTransaction>>) signData -> {
                    try {
                        return Observable.just(service.ethSendRawTransaction(signData).sendAsync().get());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Observable.error(e);
                    }
                    return Observable.error(new Throwable(TRANSFER_FAIL));
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }



    private static Observable<String> signRawTransaction(String receiveAddress, String data, BigInteger gasPrice,
                                                         BigInteger gasLimit, String password) {
        return signRawTransaction(receiveAddress, data, BigInteger.ZERO, gasPrice, gasLimit, password);
    }

    private static Observable<String> signRawTransaction(String receiveAddress, String data, BigInteger value, BigInteger gasPrice,
                                                         BigInteger gasLimit, String password) {
        return Observable.fromCallable(() ->
                service.ethGetTransactionCount(walletItem.address, DefaultBlockParameterName.LATEST).send().getTransactionCount())
                .flatMap((Func1<BigInteger, Observable<String>>) nonce -> {
                    try {
                        RawTransaction rawTransaction = RawTransaction.createTransaction(
                                nonce, gasPrice, gasLimit, receiveAddress, value, data);

                        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,
                                Credentials.create(WalletEntity.fromKeyStore(password, walletItem.keystore).getPrivateKey()));
                        return Observable.just(Numeric.toHexString(signedMessage));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Observable.error(e);
                    }
                    return Observable.error(new Throwable(SIGN_FAIL));
                });
    }


    private static BigInteger createTransferValue(TokenItem tokenItem, double value) {
        return BigInteger.TEN.pow(tokenItem.decimals).multiply(BigDecimal.valueOf(value).toBigInteger());
    }


    public static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));
        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {});
        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
    }

    private static String getErc20Name(String address, String contractAddress) throws IOException {
        Transaction nameCall = Transaction.createEthCallTransaction(address, contractAddress, ConstantUtil.NAME_HASH);
        String name = service.ethCall(nameCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(name) || ConstantUtil.RPC_RESULT_ZERO.equals(name)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();
    }

    private static String getErc20Symbol(String address, String contractAddress) throws IOException {
        Transaction symbolCall = Transaction.createEthCallTransaction(address, contractAddress, ConstantUtil.SYMBOL_HASH);
        String symbol = service.ethCall(symbolCall, DefaultBlockParameterName.LATEST).send().getValue();
        if (TextUtils.isEmpty(symbol) || ConstantUtil.RPC_RESULT_ZERO.equals(symbol)) return null;
        initStringTypes();
        return FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();
    }

    private static int getErc20Decimal(String address, String contractAddress) throws IOException {
        Transaction decimalsCall = Transaction.createEthCallTransaction(address, contractAddress, ConstantUtil.DECIMALS_HASH);
        String decimals = service.ethCall(decimalsCall, DefaultBlockParameterName.LATEST).send().getValue();
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
