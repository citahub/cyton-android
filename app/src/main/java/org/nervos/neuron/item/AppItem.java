package org.nervos.neuron.item;


import android.os.Parcel;
import android.os.Parcelable;

public class AppItem implements Parcelable {

    public String entry;
    public String icon;
    public String name;
    public String provider;

    public AppItem(String entry, String icon, String name, String provider) {
        this.entry = entry;
        this.icon = icon;
        this.name = name;
        this.provider = provider;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.icon);
        dest.writeString(this.name);
        dest.writeString(this.entry);
        dest.writeString(this.provider);
    }

    public AppItem() {
    }

    protected AppItem(Parcel in) {
        this.icon = in.readString();
        this.name = in.readString();
        this.entry = in.readString();
        this.provider = in.readString();
    }

    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel source) {
            return new AppItem(source);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };
}
