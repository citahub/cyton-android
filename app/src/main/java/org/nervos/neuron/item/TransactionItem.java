package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.nervos.neuron.util.NumberUtil;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.nervos.neuron.util.ConstantUtil.ETHDecimal;

public class TransactionItem implements Parcelable{

    public String hash;
    public String from;
    public String to;
    public String value;
    private long timestamp;
    private long timeStamp;
    public String chainName;
    public String content;
    public String gasUsed;
    public String gas;
    public String gasPrice;
    public String blockNumber;


    public String getDate() {
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        if (timeStamp > 0) {
            return ft.format(timeStamp * 1000);
        } else {
            return ft.format(timestamp);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.hash);
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.value);
        dest.writeLong(this.timestamp);
        dest.writeLong(this.timeStamp);
        dest.writeString(this.chainName);
        dest.writeString(this.content);
        dest.writeString(this.gasUsed);
        dest.writeString(this.gas);
        dest.writeString(this.gasPrice);
        dest.writeString(this.blockNumber);
    }

    public TransactionItem() {
    }

    protected TransactionItem(Parcel in) {
        this.hash = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.value = in.readString();
        this.timestamp = in.readLong();
        this.timeStamp = in.readLong();
        this.chainName = in.readString();
        this.content = in.readString();
        this.gasUsed = in.readString();
        this.gas = in.readString();
        this.gasPrice = in.readString();
        this.blockNumber = in.readString();
    }

    public static final Creator<TransactionItem> CREATOR = new Creator<TransactionItem>() {
        @Override
        public TransactionItem createFromParcel(Parcel source) {
            return new TransactionItem(source);
        }

        @Override
        public TransactionItem[] newArray(int size) {
            return new TransactionItem[size];
        }
    };
}
