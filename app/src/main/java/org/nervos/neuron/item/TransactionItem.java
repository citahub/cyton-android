package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionItem implements Parcelable {

    public static final int FAILED = 0;
    public static final int SUCCESS = 1;
    public static final int PENDING = 2;

    // base data
    public String from;
    public String to;
    public String value;
    public String hash;

    public long chainId;
    public String symbol;
    public String nativeSymbol;
    public String chainName;

    //0 update 1 success 2 pending
    public int status;


    // ethereum data
    public String gasUsed;
    public String gas;
    public String gasPrice;
    public String blockNumber;


    // AppChain
    private long timestamp;
    private long timeStamp;
    public String content;
    public String errorMessage;
    public String validUntilBlock;


    public TransactionItem(String from, String to, String value, String chainName, int status, long timestamp, String hash) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainName = chainName;
        this.status = status;
        this.setTimestamp(timestamp);
        this.hash = hash;
    }

    public String getDate() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        if (timeStamp > 0) {
            return ft.format(timeStamp * 1000);
        } else {
            return ft.format(timestamp);
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
        dest.writeInt(this.status);
        dest.writeString(this.errorMessage);
        dest.writeLong(this.chainId);
        dest.writeString(this.symbol);
        dest.writeString(this.nativeSymbol);
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
        this.status = in.readInt();
        this.errorMessage = in.readString();
        this.chainId = in.readLong();
        this.symbol = in.readString();
        this.nativeSymbol = in.readString();
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
