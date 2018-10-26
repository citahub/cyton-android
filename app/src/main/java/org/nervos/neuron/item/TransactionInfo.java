package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class TransactionInfo implements Parcelable {

    private static final String TYPE_ETH = "ETH";
    private static final String TYPE_APPCHAIN = "AppChain";

    public String from;
    public String to;
    public String nonce;
    private long quota = -1;
    public long validUntilBlock;
    public String data;
    private String value;
    public long chainId;
    public int version;
    public String gasLimit;
    public String gasPrice;
    public String chainType;

    public TransactionInfo(String to, String value) {
        this.to = to;
        this.value = NumberUtil.getWeiFromEth(Double.parseDouble(value)).toString();
    }

    public double getDoubleValue() {
        value = TextUtils.isEmpty(value)? "0":value;
        return NumberUtil.getEthFromWeiForDouble(value);
    }

    public BigInteger getBigIntegerValue() {
        if (TextUtils.isEmpty(value)) return BigInteger.ZERO;
        return Numeric.toBigInt(value);
    }

    public double getDoubleQuota() {
        return NumberUtil.getEthFromWeiForDouble(String.valueOf(quota));
    }

    public long getLongQuota() {
        return quota;
    }

    public double getGas() {
        BigInteger limitBig = TextUtils.isEmpty(gasLimit)? BigInteger.ZERO : Numeric.toBigInt(gasLimit);
        BigInteger priceBig = TextUtils.isEmpty(gasPrice)? BigInteger.ZERO : Numeric.toBigInt(gasPrice);
        return NumberUtil.getEthFromWei(limitBig.multiply(priceBig));
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(chainType) && TYPE_ETH.equals(chainType);
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
        dest.writeLong(this.quota);
        dest.writeString(this.data);
        dest.writeString(this.value);
        dest.writeLong(this.chainId);
        dest.writeInt(this.version);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gasPrice);
    }

    protected TransactionInfo(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.nonce = in.readString();
        this.quota = in.readLong();
        this.data = in.readString();
        this.value = in.readString();
        this.chainId = in.readLong();
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
