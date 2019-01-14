package com.cryptape.cita_wallet.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by duanyytop on 2018/11/23.
 */
public class BaseTransaction implements Parcelable {

    public static final int FAILED = 0;
    public static final int SUCCESS = 1;
    public static final int PENDING = 2;

    // base data
    public String from;
    public String to;
    public String value;
    public String hash;

    public String symbol;
    public String nativeSymbol;
    public String chainName;
    public String contractAddress;

    //0 update 1 success 2 pending
    public int status;


    // ethereum data
    public String gasUsed;
    public String gasLimit;
    public String gas;
    public String gasPrice;
    public String blockNumber;


    // AppChain
    private long timestamp;
    private long timeStamp;
    public String content;
    public String errorMessage;
    public String validUntilBlock;

    public String getDate() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        return timeStamp > 0 ? ft.format(timeStamp * 1000) : ft.format(timestamp);
    }

    public long getTimestamp() {
        return timeStamp > 0 ? timeStamp * 1000 : timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public BaseTransaction(String from, String to, String value, String chainName, int status, long timestamp, String hash) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainName = chainName;
        this.status = status;
        this.setTimestamp(timestamp);
        this.hash = hash;
    }

    public BaseTransaction() {}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        BaseTransaction other = (BaseTransaction) obj;

        if (hash == null) {
            return other.hash == null;
        } else return hash.equalsIgnoreCase(other.hash);
    }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.value);
        dest.writeString(this.hash);
        dest.writeString(this.symbol);
        dest.writeString(this.nativeSymbol);
        dest.writeString(this.chainName);
        dest.writeString(this.contractAddress);
        dest.writeInt(this.status);
        dest.writeString(this.gasUsed);
        dest.writeString(this.gasLimit);
        dest.writeString(this.gas);
        dest.writeString(this.gasPrice);
        dest.writeString(this.blockNumber);
        dest.writeLong(this.timestamp);
        dest.writeLong(this.timeStamp);
        dest.writeString(this.content);
        dest.writeString(this.errorMessage);
        dest.writeString(this.validUntilBlock);
    }

    protected BaseTransaction(Parcel in) {
        this.from = in.readString();
        this.to = in.readString();
        this.value = in.readString();
        this.hash = in.readString();
        this.symbol = in.readString();
        this.nativeSymbol = in.readString();
        this.chainName = in.readString();
        this.contractAddress = in.readString();
        this.status = in.readInt();
        this.gasUsed = in.readString();
        this.gasLimit = in.readString();
        this.gas = in.readString();
        this.gasPrice = in.readString();
        this.blockNumber = in.readString();
        this.timestamp = in.readLong();
        this.timeStamp = in.readLong();
        this.content = in.readString();
        this.errorMessage = in.readString();
        this.validUntilBlock = in.readString();
    }

    public static final Creator<BaseTransaction> CREATOR = new Creator<BaseTransaction>() {
        @Override
        public BaseTransaction createFromParcel(Parcel source) {return new BaseTransaction(source);}

        @Override
        public BaseTransaction[] newArray(int size) {return new BaseTransaction[size];}
    };
}
