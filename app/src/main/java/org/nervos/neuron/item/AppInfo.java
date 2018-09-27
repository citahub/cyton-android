package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo extends AppItem implements Parcelable {

    public long timestamp;

    public AppInfo(AppItem appItem, long timestamp) {
        super(appItem.entry, appItem.icon, appItem.name, appItem.provider);
        this.timestamp = timestamp;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.timestamp);
    }

    protected AppInfo(Parcel in) {
        super(in);
        this.timestamp = in.readLong();
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
