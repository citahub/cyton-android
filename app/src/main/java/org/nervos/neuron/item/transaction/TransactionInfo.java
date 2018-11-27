package org.nervos.neuron.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import org.nervos.neuron.util.NumberUtil;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionInfo implements Parcelable {

    private static final String TYPE_ETH = "ETH";
    private static final String TYPE_APPCHAIN = "AppChain";

    public String from;
    public String to;
    public String nonce;
    private String quota;
    public String validUntilBlock;
    public String data;
    private String value;
    public String chainId;
    public int version;
    public String gasLimit;
    public String gasPrice;
    public String chainType;

    public TransactionInfo(String to, String value) {
        this.to = to;
        this.value = NumberUtil.getWeiFromEth(value).toString();
    }

    public String getStringValue() {
        value = isValid(value) ? value : "0";
        return NumberUtil.getEthFromWeiForString(value);
    }

    public double getDoubleValue() {
        value = isValid(value) ? value : "0";
        return NumberUtil.getEthFromWeiForDouble(value);
    }

    public BigInteger getBigIntegerValue() {
        if (isValid(value)) {
            return BigInteger.ZERO;
        }
        return Numeric.toBigInt(value);
    }

    public double getDoubleQuota() {
        quota = isValid(quota) ? quota : "0";
        return NumberUtil.getEthFromWeiForDouble(String.valueOf(quota));
    }

    public long getLongQuota() {
        quota = isValid(quota) ? quota : "0";
        return Numeric.toBigInt(quota).longValue();
    }

    public double getGas() {
        BigInteger limitBig = isValid(gasLimit) ? Numeric.toBigInt(gasLimit) : BigInteger.ZERO;
        BigInteger priceBig = isValid(gasPrice) ? Numeric.toBigInt(gasPrice) : BigInteger.ZERO;
        return NumberUtil.getEthFromWei(limitBig.multiply(priceBig));
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(chainType) && TYPE_ETH.equals(chainType);
    }

    private boolean isValid(String value) {
        return !TextUtils.isEmpty(value) && NumberUtil.isHex(value);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.nonce);
        dest.writeString(this.quota);
        dest.writeString(this.data);
        dest.writeString(this.value);
        dest.writeString(this.chainId);
        dest.writeInt(this.version);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gasPrice);
    }

    protected TransactionInfo(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.nonce = in.readString();
        this.quota = in.readString();
        this.data = in.readString();
        this.value = in.readString();
        this.chainId = in.readString();
        this.version = in.readInt();
        this.gasLimit = in.readString();
        this.gasPrice = in.readString();
    }

    public static final Creator<TransactionInfo> CREATOR = new Creator<TransactionInfo>() {
        @Override
        public TransactionInfo createFromParcel(Parcel source) {
            return new TransactionInfo(source);
        }

        @Override
        public TransactionInfo[] newArray(int size) {
            return new TransactionInfo[size];
        }
    };
}
