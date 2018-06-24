package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletFile;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class WalletItem implements Parcelable{

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

    /**
     * all tokens in wallet
     */
    public List<TokenItem> tokenItems = new ArrayList<>();

    public long timestamp = System.currentTimeMillis();

    public boolean currentSelected = false;

    public static WalletItem fromWalletEntity(String password, WalletEntity walletEntity) {
        WalletItem walletItem = new WalletItem();
        walletItem.address = walletEntity.getAddress();
        try {
            walletItem.cryptPrivateKey = AESCrypt.encrypt(password, walletEntity.getPrivateKey());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return walletItem;
    }

    public WalletItem(){}

    public WalletItem(String name, String address, @DrawableRes int image) {
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
        dest.writeTypedList(this.tokenItems);
        dest.writeLong(this.timestamp);
        dest.writeByte(this.currentSelected ? (byte) 1 : (byte) 0);
    }

    protected WalletItem(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.address = in.readString();
        this.cryptPrivateKey = in.readString();
        this.tokenItems = in.createTypedArrayList(TokenItem.CREATOR);
        this.timestamp = in.readLong();
        this.currentSelected = in.readByte() != 0;
    }

    public static final Creator<WalletItem> CREATOR = new Creator<WalletItem>() {
        @Override
        public WalletItem createFromParcel(Parcel source) {
            return new WalletItem(source);
        }

        @Override
        public WalletItem[] newArray(int size) {
            return new WalletItem[size];
        }
    };
}
