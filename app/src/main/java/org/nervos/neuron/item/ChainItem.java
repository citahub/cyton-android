package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

public class ChainItem implements Parcelable{

    public int chainId;
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

    public ChainItem(){}

    public ChainItem(int chainId, String name, String tokenName, String tokenSymbol) {
        this.chainId = chainId;
        this.name = name;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
    }

    public ChainItem(int chainId, String name, String httpProvider, String tokenName,
                     String tokenSymbol, String tokenAvatar) {
        this.chainId = chainId;
        this.name = name;
        this.httpProvider = httpProvider;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.tokenAvatar = tokenAvatar;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.chainId);
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

    protected ChainItem(Parcel in) {
        this.chainId = in.readInt();
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

    public static final Creator<ChainItem> CREATOR = new Creator<ChainItem>() {
        @Override
        public ChainItem createFromParcel(Parcel source) {
            return new ChainItem(source);
        }

        @Override
        public ChainItem[] newArray(int size) {
            return new ChainItem[size];
        }
    };
}
