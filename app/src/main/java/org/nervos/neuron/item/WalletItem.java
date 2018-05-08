package org.nervos.neuron.item;

import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletFile;

import java.util.List;

public class WalletItem {

    /**
     * 钱包名称
     */
    public String name;

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
    public List<TokenItem> tokenItems;

    public boolean currentSelected = false;

    public static WalletItem fromWalletEntity(WalletEntity walletEntity) {
        WalletItem walletItem = new WalletItem();
        walletItem.address = walletEntity.getCredentials().getAddress();
        walletItem.privateKey = walletEntity.getPrivateKey();
        walletItem.mnemonic = walletEntity.getMnemonic();
        walletItem.passphrase = walletEntity.getPassphrase();
        walletItem.path = walletEntity.getPath();
        walletItem.walletFile = walletEntity.getWalletFile();
        walletItem.walletPass = walletEntity.getWalletPass();
        return walletItem;
    }

}
