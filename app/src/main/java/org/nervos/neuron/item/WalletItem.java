package org.nervos.neuron.item;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.WalletFile;

import java.util.ArrayList;
import java.util.List;

public class WalletItem implements Parcelable{

    /**
     * 钱包名称
     */
    public String name;

    @DrawableRes
    public int image;

    /**
     * 钱包密码
     */
    public String password;
    /**
     * 钱包地址
     */
    public String address;

    /**
     * 钱包私钥
     */
    public String privateKey;
    /**
     * 钱包文件密码
     */
    public String walletPass;
    /**
     * 钱包文件
     */
    public WalletFile walletFile;
    /**
     * 助记词
     */
    public String mnemonic;
    /**
     * 助记词密码
     */
    public String passphrase;
    /**
     * 路径
     */
    public String path;

    /**
     * 钱包中含有的Token
     */
    public List<TokenItem> tokenItems = new ArrayList<>();

    public boolean currentSelected = false;

    public static WalletItem fromWalletEntity(WalletEntity walletEntity) {
        WalletItem walletItem = new WalletItem();
        walletItem.address = walletEntity.getAddress();
        walletItem.privateKey = walletEntity.getPrivateKey();
        walletItem.mnemonic = walletEntity.getMnemonic();
        walletItem.passphrase = walletEntity.getPassphrase();
        walletItem.path = walletEntity.getPath();
        walletItem.walletFile = walletEntity.getWalletFile();
        walletItem.walletPass = walletEntity.getWalletPass();
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
        dest.writeString(this.password);
        dest.writeString(this.address);
        dest.writeString(this.privateKey);
        dest.writeString(this.walletPass);
        dest.writeString(this.mnemonic);
        dest.writeString(this.passphrase);
        dest.writeString(this.path);
        dest.writeTypedList(this.tokenItems);
        dest.writeByte(this.currentSelected ? (byte) 1 : (byte) 0);
    }

    protected WalletItem(Parcel in) {
        this.name = in.readString();
        this.image = in.readInt();
        this.password = in.readString();
        this.address = in.readString();
        this.privateKey = in.readString();
        this.walletPass = in.readString();
        this.walletFile = in.readParcelable(WalletFile.class.getClassLoader());
        this.mnemonic = in.readString();
        this.passphrase = in.readString();
        this.path = in.readString();
        this.tokenItems = in.createTypedArrayList(TokenItem.CREATOR);
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
