package org.nervos.neuron.service;

import org.nervos.neuron.item.TokenItem;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.IntType;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.account.Account;
import org.web3j.protocol.account.CompiledContract;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Call;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.infura.InfuraHttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jnr.ffi.Struct;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ETHJsonRpcService {

    private static final String MAIN_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String ROPSTEN_NODE_IP = "https://ropsten.infura.io/h3iIzGIN6msu3KeUrdlt";

    private static final String NAME_HASH = "06fdde03";
    private static final String SYMBOL_HASH = "95d89b41";
    private static final String DECIMALS_HASH = "313ce567";

    private static Web3j service;
    private static Account account;
    private static CompiledContract mContract;

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static Random random;

    private static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    private static List<TypeReference<Type>> intTypes = new ArrayList<>();

    public static void init() {
        if(service == null ) {
            service = Web3j.build(new InfuraHttpService(ROPSTEN_NODE_IP));
        }
        if (account == null) {
            account = new Account(WalletConfig.PRIVKEY, service);
        }

        stringTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Utf8String.class;
            }
        });

        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int64.class;
            }
        });
    }

    private static BigInteger randomNonce() {
        random = new Random(System.currentTimeMillis());
        return BigInteger.valueOf(Math.abs(random.nextLong()));
    }


    public static TokenItem getTokenInfo(String contractAddress) {
        try {

            Call nameCall = new Call(WalletConfig.ADDRESS, contractAddress, NAME_HASH);
            String name = service.ethCall(nameCall, DefaultBlockParameter.valueOf("latest")).send().getValue();
            String nameStr = FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();

            Call symbolCall = new Call(WalletConfig.ADDRESS, contractAddress, SYMBOL_HASH);
            String symbol = service.ethCall(symbolCall, DefaultBlockParameter.valueOf("latest")).send().getValue();
            String symbolStr = FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();

            Call decimalsCall = new Call(WalletConfig.ADDRESS, contractAddress, DECIMALS_HASH);
            String decimals = service.ethCall(decimalsCall, DefaultBlockParameter.valueOf("latest")).send().getValue();
            Int64 type = (Int64) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
            int decimalsInt = type.getValue().intValue();

            TokenItem tokenItem = new TokenItem();

            tokenItem.decimals = decimalsInt;
            tokenItem.symbol = symbolStr;
            tokenItem.name = nameStr;

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
                    transfer, randomNonce(), BigInteger.valueOf(1000), 0, 0, WalletConfig.ADDRESS, BigInteger.valueOf(value));
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
