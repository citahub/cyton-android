package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ChainItem implements Parcelable{

    public int chainId = -1;
    public String httpProvider;
    public String blockViewer;
    public String name;
    public String icon;
    public String entry;
    public String provider;
    public List<ChainSet> chainset;
    public String tokenName;
    public String tokenSymbol;
    public String tokenAvatar;

    public static class ChainSet {
        public int chainId;
        public String networkId;
        public String httpProvider;
    }

    public ChainItem(){}

    public ChainItem(int chainId, String name) {
        this.chainId = chainId;
        this.name = name;

    }

    public ChainItem(int chainId, String name, String httpProvider) {
        this.chainId = chainId;
        this.name = name;
        this.httpProvider = httpProvider;
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
