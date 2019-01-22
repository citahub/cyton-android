package com.cryptape.cita_wallet.item.transaction;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by duanyytop on 2018/11/23.
 */
public class RestTransaction extends BaseTransaction implements Parcelable {

    public String chainId;

    public RestTransaction(String from, String to, String value, String chainId, String chainName, int status, long timestamp, String hash) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.chainId = chainId;
        this.chainName = chainName;
        this.status = status;
        this.setTimestamp(timestamp);
        this.hash = hash;
    }

    public RestTransaction(RpcTransaction rpcTransaction) {
        this.chainId = rpcTransaction.getChainId();
        this.from = rpcTransaction.from;
        this.to = rpcTransaction.to;
        this.value = rpcTransaction.value;
        this.chainId = rpcTransaction.getChainId();
        this.chainName = rpcTransaction.chainName;
        this.status = rpcTransaction.status;
        this.setTimestamp(rpcTransaction.getTimestamp());
        this.hash = hash;
        this.gasUsed = rpcTransaction.gasUsed;
        this.gasPrice = rpcTransaction.gasPrice;
        this.gasLimit = rpcTransaction.gasLimit;
    }

    public RestTransaction() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.chainId);
    }

    protected RestTransaction(Parcel in) {
        super(in);
        this.chainId = in.readString();
    }

    public static final Creator<RestTransaction> CREATOR = new Creator<RestTransaction>() {
        @Override
        public RestTransaction createFromParcel(Parcel source) {
            return new RestTransaction(source);
        }

        @Override
        public RestTransaction[] newArray(int size) {
            return new RestTransaction[size];
        }
    };
}
