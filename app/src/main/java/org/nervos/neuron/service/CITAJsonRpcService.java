package org.nervos.neuron.service;

import org.nervos.neuron.item.TokenItem;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.account.Account;
import org.web3j.protocol.account.CompiledContract;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CITAJsonRpcService {

    private static final String NODE_IP = "http://47.75.129.215:1337";

    private static Web3j service;
    private static Account account;
    private static CompiledContract mContract;

    private static Random random;
    private static BigInteger quota = BigInteger.valueOf(1000000);
    private static int version = 0;
    private static int chainId = 0;

    public static void init() {
        if(service == null ) {
            service = Web3j.build(new HttpService(NODE_IP));
        }
        if (account == null) {
            account = new Account(WalletConfig.PRIVKEY, service);
        }
    }

    private static BigInteger randomNonce() {
        random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }


    public static TokenItem getTokenInfo(String contractAddress) {
        try {
            String abi = account.getAbi(contractAddress);
            mContract = new CompiledContract(abi);

            TokenItem tokenItem = new TokenItem();

            AbiDefinition symbol = mContract.getFunctionAbi("symbol", 0);
            tokenItem.symbol = account.callContract(contractAddress, symbol, randomNonce(), quota, version, chainId).toString();

            AbiDefinition decimals = mContract.getFunctionAbi("decimals", 0);
            String decimal = account.callContract(contractAddress, decimals, randomNonce(), quota, version, chainId).toString();
            tokenItem.decimals = Integer.parseInt(decimal);

            AbiDefinition nameAbi = mContract.getFunctionAbi("name", 0);
            tokenItem.name = account.callContract(contractAddress, nameAbi, randomNonce(), quota, version, chainId).toString();

            return tokenItem;

        } catch (Exception e) {
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
                    transfer, randomNonce(), quota, version, chainId, address, BigInteger.valueOf(value));
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
                            if(ethGetTransactionReceipt.getTransactionReceipt() != null) {
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
