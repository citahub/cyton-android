package org.nervos.neuron.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class TransactionItem extends BaseResponse implements Parcelable {

    private int chainId;
    private String chainIdV1;

    public TransactionItem(){}


    public TransactionItem(String from, String to, String value, String chainId, String chainName, int status, long timestamp, String hash) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainIdV1 = chainId;
        this.chainName = chainName;
        this.status = status;
        this.setTimestamp(timestamp);
        this.hash = hash;
    }

    public void setChainId(String chainId) {
        this.chainIdV1 = chainId;
    }

    public String getChainId() {
        if (TextUtils.isEmpty(chainIdV1)) return String.valueOf(chainId);
        else return chainIdV1;
    }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.chainId);
        dest.writeString(this.chainIdV1);
    }

    protected TransactionItem(Parcel in) {
        super(in);
        this.chainId = in.readInt();
        this.chainIdV1 = in.readString();
    }

    public static final Creator<TransactionItem> CREATOR = new Creator<TransactionItem>() {
        @Override
        public TransactionItem createFromParcel(Parcel source) {return new TransactionItem(source);}

        @Override
        public TransactionItem[] newArray(int size) {return new TransactionItem[size];}
    };
}
