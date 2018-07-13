package org.nervos.neuron.item;

import android.text.TextUtils;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionRequest {

    private static final BigInteger TOKENDecimal = new BigInteger("1000000000000000000");

    public String from;
    public String to;
    public long nonce;
    private long quota = -1;
    public String data;
    private String value;
    public long chainId;
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
