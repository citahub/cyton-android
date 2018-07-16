package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.nervos.neuron.util.NumberUtil;
import org.web3j.utils.Numeric;

public class TransactionInfo implements Parcelable {

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
    public long nonce;
    private long quota = -1;
    public String data;
    @SerializedName("value")
    public String value;
    public long chainId;
    public int version;
    private String gasLimit;
    private String gasPrice;
    public String uuid;

    public TransactionInfo(String to, String value) {
        this.to = to;
        this.value = NumberUtil.getWeiFromEth(Double.parseDouble(value)).toString();
    }

    public double getValue() {
        return NumberUtil.getEthFromWeiForDoubleDecimal6(value);
    }

    public double getQuota() {
        return NumberUtil.getEthFromWeiForDoubleDecimal6(String.valueOf(quota));
    }

    public double getGas() {
        return NumberUtil.getEthFromWeiForDoubleDecimal6(
                Numeric.toBigInt(gasLimit).multiply(Numeric.toBigInt(gasPrice)).toString());
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setQuota(long quota) {
        this.quota = quota;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public boolean isEthereum() {
        return !TextUtils.isEmpty(gasPrice);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeLong(this.nonce);
        dest.writeLong(this.quota);
        dest.writeString(this.data);
        dest.writeString(this.value);
        dest.writeLong(this.chainId);
        dest.writeInt(this.version);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gasPrice);
        dest.writeString(this.uuid);
    }

    protected TransactionInfo(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.nonce = in.readLong();
        this.quota = in.readLong();
        this.data = in.readString();
        this.value = in.readString();
        this.chainId = in.readLong();
        this.version = in.readInt();
        this.gasLimit = in.readString();
        this.gasPrice = in.readString();
        this.uuid = in.readString();
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
