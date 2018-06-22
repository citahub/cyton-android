package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

public class TokenItem implements Parcelable{

    private static int NERVOS_DECIMAL = 18;

    public String name;
    @DrawableRes
    public int image;               // local resource
    public String avatar;           // image uri
    public String contractAddress;
    public String symbol;
    public int decimals;
    public String amount;
    public int chainId;
    public String chainName;
    public double balance;

    public TokenItem(){}

    public TokenItem(String name, String symbol, int decimals, String contractAddress) {
        this.symbol = symbol;
        this.name = name;
        this.decimals = decimals;
        this.contractAddress = contractAddress;
    }

    public TokenItem(String symbol, int image, double balance, int chainId) {
        this.symbol = symbol;
        this.image = image;
        this.balance = balance;
        this.chainId = chainId;
    }

    public TokenItem(String name, String symbol, int decimals, String avatar, int chainId) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.avatar = avatar;
        this.chainId = chainId;
    }


    public TokenItem(ChainItem chainItem) {
        this.name = chainItem.tokenName;
        this.symbol = chainItem.tokenSymbol;
        this.decimals = NERVOS_DECIMAL;
        this.avatar = chainItem.tokenAvatar;
        this.chainId = chainItem.chainId;
    }

    public TokenItem(TokenEntity tokenEntity) {
        this.name = tokenEntity.name;
        this.symbol = tokenEntity.symbol;
        this.contractAddress = tokenEntity.address;
        this.avatar = tokenEntity.logo.src;
        this.decimals = tokenEntity.decimals;
        this.chainId = -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.image);
        dest.writeString(this.avatar);
        dest.writeString(this.contractAddress);
        dest.writeString(this.symbol);
        dest.writeInt(this.decimals);
        dest.writeString(this.amount);
        dest.writeInt(this.chainId);
        dest.writeString(this.chainName);
        dest.writeDouble(this.balance);
    }

    protected TokenItem(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.avatar = in.readString();
        this.contractAddress = in.readString();
        this.symbol = in.readString();
        this.decimals = in.readInt();
        this.amount = in.readString();
        this.chainId = in.readInt();
        this.chainName = in.readString();
        this.balance = in.readDouble();
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
