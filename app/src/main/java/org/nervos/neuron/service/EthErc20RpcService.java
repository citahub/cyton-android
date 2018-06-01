package org.nervos.neuron.service;

import android.text.TextUtils;
import android.util.Log;

import org.nervos.neuron.item.TokenItem;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Numeric;

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



}
