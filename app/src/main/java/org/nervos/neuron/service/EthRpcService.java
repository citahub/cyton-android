package org.nervos.neuron.service;

import org.nervos.neuron.item.TokenItem;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.infura.InfuraHttpService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EthRpcService {

    private static final String MAIN_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String ROPSTEN_NODE_IP = "https://ropsten.infura.io/h3iIzGIN6msu3KeUrdlt";

    private static final String NAME_HASH = "06fdde03";
    private static final String SYMBOL_HASH = "95d89b41";
    private static final String DECIMALS_HASH = "313ce567";
    private static final String BALANCEOF_HASH = "70a08231";        // function balanceOf

    private static Web3j service;

    private static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    private static List<TypeReference<Type>> intTypes = new ArrayList<>();

    public static void init() {
        if(service == null ) {
            service = Web3jFactory.build(new InfuraHttpService(ROPSTEN_NODE_IP));
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

    public static TokenItem getTokenInfo(String contractAddress) {
        try {

            TokenItem tokenItem = new TokenItem();

            Transaction nameCall = Transaction.createEthCallTransaction(WalletConfig.ADDRESS, contractAddress, NAME_HASH);
            String name = service.ethCall(nameCall, DefaultBlockParameterName.valueOf("latest")).send().getValue();
            tokenItem.name = FunctionReturnDecoder.decode(name, stringTypes).get(0).toString();

            Transaction symbolCall = Transaction.createEthCallTransaction(WalletConfig.ADDRESS, contractAddress, SYMBOL_HASH);
            String symbol = service.ethCall(symbolCall, DefaultBlockParameterName.valueOf("latest")).send().getValue();
            tokenItem.symbol = FunctionReturnDecoder.decode(symbol, stringTypes).get(0).toString();

            Transaction decimalsCall = Transaction.createEthCallTransaction(WalletConfig.ADDRESS, contractAddress, DECIMALS_HASH);
            String decimals = service.ethCall(decimalsCall, DefaultBlockParameterName.valueOf("latest")).send().getValue();
            Int64 type = (Int64) FunctionReturnDecoder.decode(decimals, intTypes).get(0);
            tokenItem.decimals = type.getValue().intValue();

            Transaction balanceCall = Transaction.createEthCallTransaction(WalletConfig.ADDRESS, contractAddress, BALANCEOF_HASH);
            String balanceOf = service.ethCall(balanceCall, DefaultBlockParameterName.valueOf("latest")).send().getValue();
            Int64 balance = (Int64) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
            tokenItem.balance = balance.getValue().intValue();

            return tokenItem;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static EthGetBalance getBalance(String address) {
        try {
            return service.ethGetBalance(address, DefaultBlockParameterName.valueOf("latest")).send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void transfer(String contractAddress, String address, long value, OnTransferResultListener listener) {

    }


    public interface OnTransferResultListener{
        void onSuccess(EthGetTransactionReceipt receipt);
        void onError(Throwable e);
    }




}
