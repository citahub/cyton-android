package org.nervos.neuron.service;


import org.nervos.neuron.item.WalletItem;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int64;
import org.web3j.protocol.Web3j;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BaseRpcService {

    public static final String ETH = "ETH";
    public static final BigInteger GAS_LIMIT = Numeric.toBigInt("0x15F90");


    static final String ZERO_16 = "000000000000000000000000";
    static final String NAME_HASH = "06fdde03";
    static final String SYMBOL_HASH = "95d89b41";
    static final String DECIMALS_HASH = "313ce567";
    static final String BALANCEOF_HASH = "70a08231";

    protected static Web3j service;
    protected static WalletItem walletItem;

    static List<TypeReference<Type>> intTypes = new ArrayList<>();
    protected static void initIntTypes() {
        intTypes.clear();
        intTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Int64.class;
            }
        });
    }

    static List<TypeReference<Type>> stringTypes = new ArrayList<>();
    protected static void initStringTypes() {
        stringTypes.clear();
        stringTypes.add(new TypeReference<Type>() {
            @Override
            public java.lang.reflect.Type getType() {
                return Utf8String.class;
            }
        });
    }


}
