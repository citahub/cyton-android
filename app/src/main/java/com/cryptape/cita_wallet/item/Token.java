package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

public class Token implements Parcelable {

    private static int CITA_DECIMAL = 18;
    private static double NO_CACHE = -1.0;

    public String name;
    @DrawableRes
    public int image;               // local resource
    public String avatar;           // image uri
    public String contractAddress;
    public String symbol;
    public int decimals;
    public String amount;
    private int chainId;
    private String chainIdV1;        // hex string
    public String chainName;
    public double balance = NO_CACHE;
    public double currencyPrice;
    public boolean selected = true;
    public boolean loaded = false;

    public Token() {
    }

    public Token(String name, String symbol, int decimals, String contractAddress) {
        this.symbol = symbol;
        this.name = name;
        this.decimals = decimals;
        this.contractAddress = contractAddress;
    }

    public Token(String symbol, int image, double balance, String chainId) {
        this.symbol = symbol;
        this.image = image;
        this.balance = balance;
        this.chainIdV1 = chainId;
    }

    public Token(String name, String symbol, int decimals, String avatar, String chainId) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.avatar = avatar;
        this.chainIdV1 = chainId;
    }

    public Token(String name, String symbol, String chainId) {
        this.name = name;
        this.symbol = symbol;
        this.chainIdV1 = chainId;
    }


    public Token(Chain chain) {
        this.name = chain.tokenName;
        this.symbol = chain.tokenSymbol;
        this.decimals = CITA_DECIMAL;
        this.avatar = chain.tokenAvatar;
        this.chainIdV1 = chain.getChainId();
        this.chainName = chain.name;
    }

    public Token(Token tokenItem) {
        this.amount = tokenItem.amount;
        this.avatar = tokenItem.avatar;
        this.balance = tokenItem.balance;
        this.setChainId(tokenItem.getChainId());
        this.chainName = tokenItem.chainName;
        this.contractAddress = tokenItem.contractAddress;
        this.currencyPrice = tokenItem.currencyPrice;
        this.decimals = tokenItem.decimals;
        this.image = tokenItem.image;
        this.name = tokenItem.name;
        this.symbol = tokenItem.symbol;
    }

    public String getChainId() {
        if (TextUtils.isEmpty(chainIdV1))
            return String.valueOf(chainId);
        else
            return chainIdV1;
    }

    public void setChainId(String chainId) {
        this.chainIdV1 = chainId;
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
        dest.writeString(this.chainIdV1);
        dest.writeString(this.chainName);
        dest.writeDouble(this.balance);
        dest.writeDouble(this.currencyPrice);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
    }

    protected Token(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.avatar = in.readString();
        this.contractAddress = in.readString();
        this.symbol = in.readString();
        this.decimals = in.readInt();
        this.amount = in.readString();
        this.chainId = in.readInt();
        this.chainIdV1 = in.readString();
        this.chainName = in.readString();
        this.balance = in.readDouble();
        this.currencyPrice = in.readDouble();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<Token> CREATOR = new Creator<Token>() {
        @Override
        public Token createFromParcel(Parcel source) {
            return new Token(source);
        }

        @Override
        public Token[] newArray(int size) {
            return new Token[size];
        }
    };
}
