package org.nervos.neuron.util;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class ConstUtil {

    public static final String ETH = "ETH";
    public static final int ETHEREUM_ID = -1;
    public static final String ETH_MAINNET = "Ethereum Mainnet";
    public static final long VALID_BLOCK_NUMBER_DIFF = 80L;
    public static final long LONG_6 = 1000000;
    public static final long DEFAULT_QUOTA = LONG_6;


    // gas constant data
    public static final BigInteger GAS_LIMIT = Numeric.toBigInt("0x5208");  // default eth gas limit is 21000
    public static final BigInteger GAS_ERC20_LIMIT = Numeric.toBigInt("0x23280");  // default eth gas limit is 144000
    public static final BigInteger GAS_MIN_LIMIT = Numeric.toBigInt("0x9D8");      // default eth gas min limit is 2520
    public static final BigInteger GAS_PRICE = Numeric.toBigInt("0x2540BE400");
    public static final String RPC_RESULT_ZERO = "0x";
    public static final BigInteger QUOTA_TOKEN = new BigInteger("1000000");
    public static final BigInteger QUOTA_ERC20 = new BigInteger("100000000");


    // ERC20 method hashes
    public static final String ZERO_16 = "000000000000000000000000";
    public static final String NAME_HASH = "06fdde03";
    public static final String SYMBOL_HASH = "95d89b41";
    public static final String DECIMALS_HASH = "313ce567";
    public static final String BALANCEOF_HASH = "70a08231";


    public static final String FingerPrint = "FingerPrint";
    public static final String Currency = "Currency";
}
