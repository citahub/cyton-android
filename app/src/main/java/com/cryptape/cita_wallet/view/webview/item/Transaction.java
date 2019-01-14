package com.cryptape.cita_wallet.view.webview.item;

import android.os.Parcel;
import android.os.Parcelable;

public class Transaction implements Parcelable {

    public static final String TYPE_ETH = "ETH";
    public static final String TYPE_CITA = "CITA";

    public Address recipient;
    public Address contract;
    public String value;
    public String gasPrice;
    public String gasLimit;
    public long nonce;
    public long leafPosition;

    public String from;
    public String to;
    public String data;
    public String quota;
    public String validUntilBlock;
    public int version;
    public long chainId;
    public String chainType;

    public Transaction(
            String data,
            String chainType) {
        this.data = data;
        this.chainType = chainType;
    }

    public Transaction(
            Address recipient,
            Address contract,
            String value,
            String gasLimit,
            String gasPrice,
            long nonce,
            String data,
            long chainId,
            int version,
            String chainType) {
        this(recipient, contract, value, gasLimit, gasPrice, nonce, data,
                chainId, version, chainType, 0);
    }

    public Transaction(
            Address recipient,
            Address contract,
            String value,
            String gasLimit,
            String gasPrice,
            long nonce,
            String data,
            long chainId,
            int version,
            String chainType,
            long leafPosition) {
        this.recipient = recipient;
        this.contract = contract;
        this.value = value;
        this.to = recipient.toString();
        this.chainType = chainType;
        if (TYPE_ETH.equalsIgnoreCase(chainType)) {
            this.gasPrice = gasPrice;
            this.gasLimit = gasLimit;
            this.chainId = chainId;
        } else {
            this.quota = gasLimit;
            this.validUntilBlock = gasPrice;
            this.chainId = chainId;
            this.version = version;
        }
        this.nonce = nonce;
        this.data = data;
        this.leafPosition = leafPosition;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.recipient, flags);
        dest.writeParcelable(this.contract, flags);
        dest.writeString(this.value);
        dest.writeString(this.gasPrice);
        dest.writeString(this.gasLimit);
        dest.writeLong(this.nonce);
        dest.writeLong(this.leafPosition);
        dest.writeString(this.from);
        dest.writeString(this.to);
        dest.writeString(this.data);
        dest.writeString(this.quota);
        dest.writeString(this.validUntilBlock);
        dest.writeInt(this.version);
        dest.writeLong(this.chainId);
    }

    protected Transaction(Parcel in) {
        this.recipient = in.readParcelable(Address.class.getClassLoader());
        this.contract = in.readParcelable(Address.class.getClassLoader());
        this.value = in.readString();
        this.gasPrice = in.readString();
        this.gasLimit = in.readString();
        this.nonce = in.readLong();
        this.leafPosition = in.readLong();
        this.from = in.readString();
        this.to = in.readString();
        this.data = in.readString();
        this.quota = in.readString();
        this.validUntilBlock = in.readString();
        this.version = in.readInt();
        this.chainId = in.readLong();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
}
