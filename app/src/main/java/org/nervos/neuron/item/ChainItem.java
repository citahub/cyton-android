package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

public class ChainItem implements Parcelable{

    public String chainId;
    public String httpProvider;
    public String blockViewer;
    public String name;
    public String icon;
    public String entry;
    public String provider;

    public ChainItem(){}

    public ChainItem(String chainId, String name) {
        this.chainId = chainId;
        this.name = name;

    }

    public ChainItem(String chainId, String name, String httpProvider) {
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
        dest.writeString(this.chainId);
        dest.writeString(this.httpProvider);
        dest.writeString(this.blockViewer);
        dest.writeString(this.name);
        dest.writeString(this.icon);
        dest.writeString(this.entry);
        dest.writeString(this.provider);
    }

    protected ChainItem(Parcel in) {
        this.chainId = in.readString();
        this.httpProvider = in.readString();
        this.blockViewer = in.readString();
        this.name = in.readString();
        this.icon = in.readString();
        this.entry = in.readString();
        this.provider = in.readString();
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
