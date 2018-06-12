package org.nervos.neuron.service;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.nervos.neuron.item.TokenItem;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EthErc20RpcService extends EthRpcService{

    /**
     * get standard erc20 token info through function hash and parameters
     */
    public static TokenItem getTokenInfo(String contractAddress, String address) {
        try {
            TokenItem tokenItem = new TokenItem();
            tokenItem.contractAddress = contractAddress;

            Transaction nameCall = Transaction.createEthCallTransaction(address, contractAddress, NAME_HASH);
            String name = service.ethCall(nameCall, DefaultBlockParameterName.LATEST).send().getValue();
            if (TextUtils.isEmpty(name) || "0x".equals(name)) return null;
            initStringTypes();
            tokenItem.name = FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();

            Transaction symbolCall = Transaction.createEthCallTransaction(address, contractAddress, SYMBOL_HASH);
            String symbol = service.ethCall(symbolCall, DefaultBlockParameterName.LATEST).send().getValue();
            if (TextUtils.isEmpty(symbol) || "0x".equals(symbol)) return null;
            initStringTypes();
            tokenItem.symbol = FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();

            Transaction decimalsCall = Transaction.createEthCallTransaction(address, contractAddress, DECIMALS_HASH);
            String decimals = service.ethCall(decimalsCall, DefaultBlockParameterName.LATEST).send().getValue();
            if (!TextUtils.isEmpty(decimals) && !"0x".equals(decimals)) {
                initIntTypes();
                Int64 type = (Int64) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
                tokenItem.decimals = type.getValue().intValue();
            }

            return tokenItem;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double getERC20Balance(String contractAddress, String address) {
        try {
            Transaction decimalsCall = Transaction.createEthCallTransaction(address, contractAddress, DECIMALS_HASH);
            String decimals = service.ethCall(decimalsCall, DefaultBlockParameterName.LATEST).send().getValue();
            Log.d("wallet", "erc20 decimals: " + decimals);
            long decimal = 0;
            if (!TextUtils.isEmpty(decimals) && !"0x".equals(decimals)) {
                initIntTypes();
                Int64 type = (Int64) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
                decimal = type.getValue().longValue();
            }

            Transaction balanceCall = Transaction.createEthCallTransaction(address, contractAddress,
                    BALANCEOF_HASH + ZERO_16 + Numeric.cleanHexPrefix(address));
            String balanceOf = service.ethCall(balanceCall, DefaultBlockParameterName.LATEST).send().getValue();
            Log.d("wallet", "erc20 balanceOf: " + balanceOf);
            if (!TextUtils.isEmpty(balanceOf) && !"0x".equals(balanceOf)) {
                initIntTypes();
                Int64 balance = (Int64) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
                double balances = balance.getValue().doubleValue();
                if (decimal == 0) return balances;
                else return balances/(Math.pow(10, decimal));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public static Observable<EthSendTransaction> transferErc20(TokenItem tokenItem, String address,
                                                               double value, BigInteger gasPrice) {
        BigInteger transferValue = getTransferValue(tokenItem, value);
        String data = createTokenTransferData(address, transferValue);
        return Observable.fromCallable(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetTransactionCount ethGetTransactionCount = service
                        .ethGetTransactionCount(walletItem.address, DefaultBlockParameterName.LATEST).send();
                return ethGetTransactionCount.getTransactionCount();
            }
        }).flatMap(new Func1<BigInteger, Observable<String>>() {
            @Override
            public Observable<String> call(BigInteger nonce) {
                Credentials credentials = Credentials.create(walletItem.privateKey);
                RawTransaction rawTransaction = RawTransaction.createTransaction(nonce,
                        gasPrice, Numeric.toBigInt("0x23280"), tokenItem.contractAddress, data);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                return Observable.just(Numeric.toHexString(signedMessage));
            }
        }).flatMap(new Func1<String, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(String hexValue){
                try {
                    EthSendTransaction ethSendTransaction =
                            service.ethSendRawTransaction(hexValue).sendAsync().get();
                    return Observable.just(ethSendTransaction);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).filter(ethSendTransaction -> ethSendTransaction != null
                && !TextUtils.isEmpty(ethSendTransaction.getTransactionHash()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
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

    public static void getTransactionReceipt(String hash) {
        Observable.fromCallable(new Callable<EthGetTransactionReceipt>() {
            @Override
            public EthGetTransactionReceipt call() {
                try {
                    return service.ethGetTransactionReceipt(hash).send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<EthGetTransactionReceipt>() {
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(EthGetTransactionReceipt ethGetTransactionReceipt) {
                        Log.d("wallet", "transaction receipt: " + ethGetTransactionReceipt.getTransactionReceipt());
                    }
                });
    }


    private static String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));

        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });

        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
    }

}
