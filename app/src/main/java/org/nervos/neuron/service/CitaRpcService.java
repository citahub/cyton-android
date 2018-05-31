package org.nervos.neuron.service;


import android.text.TextUtils;
import android.util.Log;

import org.nervos.neuron.item.TokenItem;
import org.nervos.web3j.protocol.Web3j;
import org.nervos.web3j.protocol.account.Account;
import org.nervos.web3j.protocol.account.CompiledContract;
import org.nervos.web3j.protocol.core.DefaultBlockParameter;
import org.nervos.web3j.protocol.core.methods.response.AbiDefinition;
import org.nervos.web3j.protocol.core.methods.response.EthGetBalance;
import org.nervos.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;
import org.nervos.web3j.protocol.core.methods.response.EthSendTransaction;
import org.nervos.web3j.protocol.http.HttpService;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CitaRpcService {

    public static final String NODE_IP = "http://47.75.129.215:1337";

    private static Web3j service;
    private static Account account;
    private static CompiledContract mContract;

    private static Random random;
    private static BigInteger quota = BigInteger.valueOf(1000000);
    private static int version = 0;
    private static int chainId = 1;
    private static long chainValue = 0;

    public static void init(String privateKey, String httpProvider) {
        service = Web3j.build(new HttpService(httpProvider));
        account = new Account(privateKey, service);
    }

    public static void init(String httpProvider) {
        service = Web3j.build(new HttpService(httpProvider));
        account = new Account(WalletConfig.PRIVKEY, service);
    }

    private static BigInteger randomNonce() {
        random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }


    public static TokenItem getTokenInfo(String contractAddress, String address) {
        try {
            String abi = account.getAbi(contractAddress);
            mContract = new CompiledContract(abi);

            TokenItem tokenItem = new TokenItem();
            tokenItem.contractAddress = contractAddress;

            AbiDefinition symbol = mContract.getFunctionAbi("symbol", 0);
            tokenItem.symbol = account.callContract(contractAddress, symbol, randomNonce(),
                    quota, version, chainId, chainValue).toString();

            AbiDefinition decimals = mContract.getFunctionAbi("decimals", 0);
            String decimal = account.callContract(contractAddress, decimals, randomNonce(),
                    quota, version, chainId, chainValue).toString();
            tokenItem.decimals = Integer.parseInt(decimal);

            AbiDefinition nameAbi = mContract.getFunctionAbi("name", 0);
            tokenItem.name = account.callContract(contractAddress, nameAbi, randomNonce(),
                    quota, version, chainId, chainValue).toString();

            AbiDefinition balanceOfAbi = mContract.getFunctionAbi("balanceOf", 1);
            String balance = account.callContract(contractAddress, balanceOfAbi, randomNonce(),
                    quota, version, chainId, chainValue, address).toString();
            tokenItem.balance = Float.valueOf(balance);

            Log.d("wallet", "cita erc20 balance: " + tokenItem.balance);

            return tokenItem;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static EthMetaData getMetaData() {
        try {
            return service.ethMetaData(DefaultBlockParameter.valueOf("latest")).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static EthGetBalance getBalance(String address) {
        try {
            return service.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void transfer(String contractAddress, String address, long value, OnTransferResultListener listener) {
        try {
            String abi = account.getAbi(contractAddress);

            mContract = new CompiledContract(abi);

            AbiDefinition transfer = mContract.getFunctionAbi("transfer", 2);
            EthSendTransaction ethSendTransaction = (EthSendTransaction)account.callContract(contractAddress,
                    transfer, randomNonce(), quota, version, chainId, chainValue, address, BigInteger.valueOf(value));
            Thread.sleep(6000);
            service.ethGetTransactionReceipt(ethSendTransaction.getSendTransactionResult().getHash())
                    .observable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<EthGetTransactionReceipt>() {
                        @Override
                        public void onCompleted() {

                        }
                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            if( listener != null) {
                                listener.onError(e);
                            }
                        }
                        @Override
                        public void onNext(EthGetTransactionReceipt ethGetTransactionReceipt) {
                            if(ethGetTransactionReceipt.getTransactionReceipt() != null
                                    && ethGetTransactionReceipt.getTransactionReceipt().getErrorMessage() == null) {
                                if (listener != null) {
                                    listener.onSuccess(ethGetTransactionReceipt);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onError(new Throwable("receipt is null"));
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public interface OnTransferResultListener{
        void onSuccess(EthGetTransactionReceipt receipt);
        void onError(Throwable e);
    }




}
