package org.nervos.neuron.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duanyytop on 2018/11/23.
 */
public class TransactionResponse extends BaseResponse implements Parcelable {

    public String chainId;

    public TransactionResponse(String from, String to, String value, String chainId, String chainName, int status, long timestamp, String hash) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainId = chainId;
        this.chainName = chainName;
        this.status = status;
        this.setTimestamp(timestamp);
        this.hash = hash;
    }

    public TransactionResponse(TransactionItem transactionItem) {
        this.chainId = transactionItem.getChainId();
        this.from = transactionItem.from;
        this.to = transactionItem.to;
        this.value = transactionItem.value;
        this.chainId = transactionItem.getChainId();
        this.chainName = transactionItem.chainName;
        this.status = transactionItem.status;
        this.setTimestamp(transactionItem.getTimestamp());
        this.hash = hash;
    }

    public TransactionResponse() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.chainId);
    }

    protected TransactionResponse(Parcel in) {
        super(in);
        this.chainId = in.readString();
    }

    public static final Creator<TransactionResponse> CREATOR = new Creator<TransactionResponse>() {
        @Override
        public TransactionResponse createFromParcel(Parcel source) {return new TransactionResponse(source);}

        @Override
        public TransactionResponse[] newArray(int size) {return new TransactionResponse[size];}
    };
}
