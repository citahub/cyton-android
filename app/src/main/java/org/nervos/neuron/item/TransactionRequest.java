package org.nervos.neuron.item;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionRequest {

    private static final BigInteger TOKENDecimal = new BigInteger("1000000000000000000");

    /**
     * from : 0x627306090abaB3A6e1400e9345bC60c78a8BEf57
     * nonce : 100
     * quota : 100
     * data : 0x627306090abaB3A6e1400e9345bC60c78a8BEf57
     * value : 0
     * chainId : 1
     * version : 0
     */

    public String from;
    public String to;
    public int nonce;
    private int quota = -1;
    public String data;
    private String value;
    public int chainId;
    public int version;
    private String gasLimit;
    private String gasPrice;

    public double getValue() {
        return new BigInteger(value).multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public double getQuota() {
        return BigInteger.valueOf(quota).multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public double getGas() {
        return Numeric.toBigInt(gasLimit).multiply(Numeric.toBigInt(gasPrice))
                .multiply(BigInteger.valueOf(10000))
                .divide(TOKENDecimal).doubleValue()/10000.0;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(gasPrice);
    }
}
