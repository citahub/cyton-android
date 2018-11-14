package org.nervos.neuron.item;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class AppItem implements Parcelable {

    public String entry;
    public String icon;
    public String name;
    public String provider;
    public String blockViewer;
    public Long collectTime;
    public Map<String, String> chainSet = new HashMap<>();

    public AppItem() {

    }

    public AppItem(String entry, String icon, String name, String provider) {
        this.entry = entry;
        this.icon = icon;
        this.name = name;
        this.provider = provider;
    }

    public AppItem(String entry) {
        this.entry = entry;
    }


    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel in) {
            return new AppItem(in);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.entry);
        dest.writeString(this.icon);
        dest.writeString(this.name);
        dest.writeString(this.provider);
        dest.writeString(this.blockViewer);
        dest.writeInt(this.chainSet.size());
        dest.writeLong(this.collectTime);
        for (Map.Entry<String, String> entry : this.chainSet.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    protected AppItem(Parcel in) {
        this.entry = in.readString();
        this.icon = in.readString();
        this.name = in.readString();
        this.provider = in.readString();
        this.blockViewer = in.readString();
        int chainsetSize = in.readInt();
        this.collectTime = in.readLong();
        this.chainSet = new HashMap<String, String>(chainsetSize);
        for (int i = 0; i < chainsetSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.chainSet.put(key, value);
        }
    }

}
