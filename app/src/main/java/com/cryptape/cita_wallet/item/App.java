package com.cryptape.cita_wallet.item;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class App implements Parcelable {

    public String entry;
    public String icon;
    public String name;
    public String provider;
    public String blockViewer;
    public Map<String, String> chainSet = new HashMap<>();
    public long timestamp;

    public App() {

    }

    public App(String entry, String icon, String name, String provider) {
        this.entry = entry;
        this.icon = icon;
        this.name = name;
        this.provider = provider;
    }

    public App(String entry) {
        this.entry = entry;
    }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.entry);
        dest.writeString(this.icon);
        dest.writeString(this.name);
        dest.writeString(this.provider);
        dest.writeString(this.blockViewer);
        dest.writeInt(this.chainSet.size());
        for (Map.Entry<String, String> entry : this.chainSet.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeLong(this.timestamp);
    }

    protected App(Parcel in) {
        this.entry = in.readString();
        this.icon = in.readString();
        this.name = in.readString();
        this.provider = in.readString();
        this.blockViewer = in.readString();
        int chainSetSize = in.readInt();
        this.chainSet = new HashMap<String, String>(chainSetSize);
        for (int i = 0; i < chainSetSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.chainSet.put(key, value);
        }
        this.timestamp = in.readLong();
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel source) {return new App(source);}

        @Override
        public App[] newArray(int size) {return new App[size];}
    };
}
