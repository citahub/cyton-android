package org.nervos.neuron.service;

import android.text.TextUtils;
import android.util.Log;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.slf4j.LoggerFactory;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EthRpcService {

    private static final String MAIN_NODE_IP = "https://mainnet.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String ROPSTEN_NODE_IP = "https://ropsten.infura.io/h3iIzGIN6msu3KeUrdlt";
    private static final String ETH = "ETH";

    private static final String NAME_HASH = "06fdde03";
    private static final String SYMBOL_HASH = "95d89b41";
    private static final String DECIMALS_HASH = "313ce567";
    private static final String BALANCEOF_HASH = "70a08231";        // function balanceOf

    private static Web3j service;

    private static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    private static List<TypeReference<Type>> intTypes = new ArrayList<>();

    public static void init() {
        service = Web3jFactory.build(new InfuraHttpService(ROPSTEN_NODE_IP));

    }

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

            Log.d("wallet", "contract address: " + contractAddress);
            Log.d("wallet", "address: " + address);
            Transaction balanceCall = Transaction.createEthCallTransaction(address, contractAddress, BALANCEOF_HASH);
            String balanceOf = service.ethCall(balanceCall, DefaultBlockParameterName.LATEST).send().getValue();
            Log.d("wallet", "erc20 balanceOf: " + balanceOf);
            if (!TextUtils.isEmpty(balanceOf) && !"0x".equals(balanceOf)) {
                initIntTypes();
                Int64 balance = (Int64) FunctionReturnDecoder.decode(balanceOf, intTypes).get(0);
                double balances = balance.getValue().doubleValue();
                if (decimal == 0) return balances;
                else return balances/decimal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static EthGetBalance getEthBalance(String address) {
        try {
            return service.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TokenItem getDefaultEth(String address) {
        EthGetBalance ethGetBalance = EthRpcService.getEthBalance(address);
        if (ethGetBalance != null) {
            double balance = ethGetBalance.getBalance().multiply(BigInteger.valueOf(10000))
                    .divide(new BigInteger("1000000000000000000")).doubleValue()/10000.0;
            Log.d("wallet", "eth balanceOf: " + balance);
            return new TokenItem(ETH, R.drawable.ethereum, balance, -1);
        }
        return null;
    }


    public static void transfer(String contractAddress, String address, long value, OnTransferResultListener listener) {

    }


    public interface OnTransferResultListener{
        void onSuccess(EthGetTransactionReceipt receipt);
        void onError(Throwable e);
    }


    private static void initIntTypes() {
        intTypes.clear();
        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int64.class;
            }
        });
    }


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
