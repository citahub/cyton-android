package com.cryptape.cita_wallet.view.webview.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.web3j.utils.Numeric;

public class Address implements Parcelable {

    public static final Address EMPTY = new Address("");

    private final String value;

    public Address(@NonNull String value) {
        this.value = value;
    }

    protected Address(Parcel in) {
        value = in.readString();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Address && value.equals(((Address) other).value);

    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
    }
}
