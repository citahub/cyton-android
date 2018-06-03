package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionItem implements Parcelable{

    public String id;
    public String from;
    public String to;
    public String value;
    public String date;
    public String chainName;

    public TransactionItem(String id, String from, String to, String value, String date, String chainName) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.value = value;
        this.date = date;
        this.chainName = chainName;
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
        dest.writeString(this.date);
        dest.writeString(this.chainName);
    }

    protected TransactionItem(Parcel in) {
        this.id = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.value = in.readString();
        this.date = in.readString();
        this.chainName = in.readString();
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
