package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by BaojunCZ on 2018/11/16.
 */
public class CollectDAppItem extends AppItem implements Parcelable {

    public Long collectTime;

    public CollectDAppItem() {

    }

    public CollectDAppItem(AppItem appItem, Long collectTime) {
        this.entry = appItem.entry;
        this.icon = appItem.icon;
        this.name = appItem.name;
        this.provider = appItem.provider;
        this.blockViewer = appItem.blockViewer;
        this.chainSet = appItem.chainSet;
        this.collectTime = collectTime;
    }

    protected CollectDAppItem(Parcel in) {
        super(in);
        if (in.readByte() == 0) {
            collectTime = null;
        } else {
            collectTime = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (collectTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(collectTime);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectDAppItem> CREATOR = new Creator<CollectDAppItem>() {
        @Override
        public CollectDAppItem createFromParcel(Parcel in) {
            return new CollectDAppItem(in);
        }

        @Override
        public CollectDAppItem[] newArray(int size) {
            return new CollectDAppItem[size];
        }
    };
}
