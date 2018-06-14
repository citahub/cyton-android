package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionItem implements Parcelable{

    @SerializedName("hash")
    public String id;
    public String from;
    public String to;
    public String value;
    private long timestamp;
    public String chainName;
    public String gasUsed;
    public String blockNumber;

    public TransactionItem(String id, String from, String to, String value, String chainName) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainName = chainName;
    }

    public String getDate() {
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        return ft.format(timestamp);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.value);
        dest.writeLong(this.timestamp);
        dest.writeString(this.chainName);
        dest.writeString(this.gasUsed);
        dest.writeString(this.blockNumber);
    }

    protected TransactionItem(Parcel in) {
        this.id = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.value = in.readString();
        this.timestamp = in.readLong();
        this.chainName = in.readString();
        this.gasUsed = in.readString();
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
