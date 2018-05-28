package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

public class TokenItem implements Parcelable{

    public String name;
    public int image;
    public String contractAddress;
    public String symbol;
    public int decimals;
    public String amount;
    public int chainId;
    public String chainName;
    public float balance;

    public TokenItem(){}

    public TokenItem(String symbol, int image, float balance) {
        this.symbol = symbol;
        this.image = image;
        this.balance = balance;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.image);
        dest.writeString(this.contractAddress);
        dest.writeString(this.symbol);
        dest.writeInt(this.decimals);
        dest.writeString(this.amount);
        dest.writeInt(this.chainId);
        dest.writeString(this.chainName);
        dest.writeFloat(this.balance);
    }

    protected TokenItem(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.contractAddress = in.readString();
        this.symbol = in.readString();
        this.decimals = in.readInt();
        this.amount = in.readString();
        this.chainId = in.readInt();
        this.chainName = in.readString();
        this.balance = in.readFloat();
    }

    public static final Creator<TokenItem> CREATOR = new Creator<TokenItem>() {
        @Override
        public TokenItem createFromParcel(Parcel source) {
            return new TokenItem(source);
        }

        @Override
        public TokenItem[] newArray(int size) {
            return new TokenItem[size];
        }
    };
}
