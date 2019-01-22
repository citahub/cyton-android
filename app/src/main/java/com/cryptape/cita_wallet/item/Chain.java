package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class Chain implements Parcelable {

    private int chainId;
    private String chainIdV1;        // hex string
    public String httpProvider;
    public String blockViewer;
    public String name;
    public String icon;
    public String entry;
    public String provider;
    public String tokenName;
    public String tokenSymbol;
    public String tokenAvatar;
    public String errorMessage;

    public Chain(){}

    public Chain(String chainId, String name, String httpProvider) {
        this.chainIdV1 = chainId;
        this.name = name;
        this.httpProvider = httpProvider;
    }

    public Chain(String chainId, String name, String tokenName, String tokenSymbol) {
        this.chainIdV1 = chainId;
        this.name = name;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
    }

    public Chain(String chainId, String name, String httpProvider, String tokenName,
                 String tokenSymbol, String tokenAvatar) {
        this.chainIdV1 = chainId;
        this.name = name;
        this.httpProvider = httpProvider;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.tokenAvatar = tokenAvatar;
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
        dest.writeInt(this.chainId);
        dest.writeString(this.chainIdV1);
        dest.writeString(this.httpProvider);
        dest.writeString(this.blockViewer);
        dest.writeString(this.name);
        dest.writeString(this.icon);
        dest.writeString(this.entry);
        dest.writeString(this.provider);
        dest.writeString(this.tokenName);
        dest.writeString(this.tokenSymbol);
        dest.writeString(this.tokenAvatar);
        dest.writeString(this.errorMessage);
    }

    protected Chain(Parcel in) {
        this.chainId = in.readInt();
        this.chainIdV1 = in.readString();
        this.httpProvider = in.readString();
        this.blockViewer = in.readString();
        this.name = in.readString();
        this.icon = in.readString();
        this.entry = in.readString();
        this.provider = in.readString();
        this.tokenName = in.readString();
        this.tokenSymbol = in.readString();
        this.tokenAvatar = in.readString();
        this.errorMessage = in.readString();
    }

    public static final Creator<Chain> CREATOR = new Creator<Chain>() {
        @Override
        public Chain createFromParcel(Parcel source) {return new Chain(source);}

        @Override
        public Chain[] newArray(int size) {return new Chain[size];}
    };
}
