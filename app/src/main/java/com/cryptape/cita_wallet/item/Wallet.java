package com.cryptape.cita_wallet.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import com.cryptape.cita_wallet.util.crypto.WalletEntity;

import java.util.ArrayList;
import java.util.List;

public class Wallet implements Parcelable {

    /**
     * wallet name
     */
    public String name;

    @DrawableRes
    public int image;

    /**
     * wallet address
     */
    public String address;

    /**
     * the cipher text of wallet private key
     */
    public String cryptPrivateKey;

    public String keystore;

    /**
     * all tokens in wallet
     */
    public List<Token> tokens = new ArrayList<>();

    /**
     * all chain in wallet
     */

    public List<Chain> chains = new ArrayList<>();

    public long timestamp = System.currentTimeMillis();

    public boolean currentSelected = false;

    public static Wallet fromWalletEntity(WalletEntity walletEntity) {
        Wallet wallet = new Wallet();
        wallet.address = walletEntity.getAddress();
        wallet.keystore = walletEntity.getKeystore();
        return wallet;
    }

    public Wallet() {
    }

    public Wallet(String name, String address, @DrawableRes int image) {
        this.name = name;
        this.address = address;
        this.image = image;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.image);
        dest.writeString(this.address);
        dest.writeString(this.cryptPrivateKey);
        dest.writeTypedList(this.tokens);
        dest.writeTypedList(this.chains);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.currentSelected ? (byte) 1 : (byte) 0);
    }

    protected Wallet(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.address = in.readString();
        this.cryptPrivateKey = in.readString();
        this.tokens = in.createTypedArrayList(Token.CREATOR);
        this.chains = in.createTypedArrayList(Chain.CREATOR);
        this.timestamp = in.readLong();
        this.currentSelected = in.readByte() != 0;
    }

    public static final Creator<Wallet> CREATOR = new Creator<Wallet>() {
        @Override
        public Wallet createFromParcel(Parcel source) {
            return new Wallet(source);
        }

        @Override
        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };
}
