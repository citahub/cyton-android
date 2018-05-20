package org.nervos.neuron.util.crypto;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.math.BigInteger;
import java.security.*;

import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.bip44.HdKeyNode;
import org.nervos.neuron.util.bip44.hdpath.HdKeyPath;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


/**
 * 钱包
 */
public class WalletEntity {

    private static final String PASSWORD = "";

    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 身份凭证
     */
    private Credentials credentials;
    /**
     * 钱包文件密码
     */
    private String walletPass;
    /**
     * Ethereum KeyStore file
     */
    private WalletFile walletFile;
    /**
     * 助记词
     */
    private String mnemonic;
    /**
     * 助记词密码
     */
    private String passphrase;
    /**
     * 路径
     */
    private String path;


    private WalletEntity() {
    }

    public static void initWalletMnemonic(Context context) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                MnemonicUtils.initWordList(context);
            }
        }.start();
    }

    /**
     * 构造(根据私钥构建)
     *
     * @param privateKey 私钥
     */
    public WalletEntity(String privateKey) {
        credentials = Credentials.create(privateKey);
    }

    /**
     * 创建一个新的钱包(没有助记词的官方钱包)
     *
     * @param password wallet file的密码
     * @return
     * @throws Exception
     */
    public static WalletEntity create(String password) throws Exception {
        WalletEntity wa = new WalletEntity();
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        wa.walletFile = Wallet.createStandard(password, ecKeyPair);
        wa.credentials = Credentials.create(Wallet.decrypt(password, wa.walletFile));
        wa.walletPass = password;
        return wa;
    }

    /**
     * 创建新钱包（生成助记词）
     *
     * @param password 助记词密码，也作为即将生成的wallet file的密码；如果助记词密码为空则不再生成wallet file
     *                 imtoken助记词密码为null
     * @param path     路径
     * @return
     * @throws CipherException
     */
    public static WalletEntity createWithMnemonic(String password, String path) {

        WalletEntity wa = new WalletEntity();
        byte[] initialEntropy = new byte[16];
        secureRandom.nextBytes(initialEntropy);
        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, password);
        ECKeyPair ecKeyPair = createBip44NodeFromSeed(seed, path);
        if (password != null) {
            try {
                wa.walletFile = Wallet.create(password, ecKeyPair, 1024, 1);
            } catch (CipherException e) {
                e.printStackTrace();
            }
        }
        wa.credentials = Credentials.create(ecKeyPair);
        wa.walletPass = password;
        wa.mnemonic = mnemonic;
        wa.passphrase = password;
        wa.path = path;
        return wa;
    }


    /**
     * 根据助记词导入一个钱包(imtoken助记词密码为null)
     *
     * @param mnemonic 助记词
     * @param path     路径 m/44'/60'/0'／0／0   m/purpse'/coin_type'/account'/change/address_index
     * @return 钱包
     * @throws CipherException
     */
    public static WalletEntity fromMnemonic(String mnemonic, String path) throws CipherException {
        WalletEntity wa = new WalletEntity();
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, PASSWORD);
        ECKeyPair ecKeyPair = createBip44NodeFromSeed(seed, path);
        wa.walletFile = Wallet.create(PASSWORD, ecKeyPair, 1024, 1);
        wa.credentials = Credentials.create(ecKeyPair);
        wa.walletPass = PASSWORD;
        wa.mnemonic = mnemonic;
        wa.passphrase = PASSWORD;
        wa.path = path;
        return wa;
    }


    /**
     * 根据私钥导入钱包
     *
     * @param privateKey 私钥
     * @return 钱包
     * @throws CipherException
     */
    public static WalletEntity fromPrivateKey(BigInteger privateKey) throws CipherException {
        WalletEntity wa = new WalletEntity();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        wa.walletFile = Wallet.create(PASSWORD, ecKeyPair, 1024, 1);
        wa.credentials = Credentials.create(ecKeyPair);
        return wa;
    }


    /**
     * 根据keystore导入钱包
     *
     * @param keystore 钱包信息
     * @return 钱包
     * @throws CipherException
     */
    public static WalletEntity fromKeyStore(String password, String keystore) throws CipherException {
        WalletEntity wa = new WalletEntity();
        wa.walletFile = createWalletFile(keystore);
        wa.credentials = Credentials.create(Wallet.decrypt(password, wa.walletFile));
        return wa;
    }

    /**
     * m/purpse'/coin_type'/account'/change/address_index
     * m/44'/60'/0'／0／0
     *
     * @param seed 种子
     * @param path 路径 m/44'/60'/0'／0／0
     * @return key 秘钥
     */
    private static ECKeyPair createBip44NodeFromSeed(byte[] seed, String path) {
        HdKeyPath p = HdKeyPath.valueOf(path);
        HdKeyNode node = HdKeyNode.fromSeed(seed);
        node = node.createChildNode(p);
        byte[] privateKeyByte = node.getPrivateKey().getPrivateKeyBytes();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKeyByte);
        return ecKeyPair;
    }

    /**
     * 导出一个新的wallet file，并记录下当前的wallet file信息
     *
     * @param password 设定一个钱包文件密码
     * @return 钱包文件
     * @throws CipherException
     */
    public WalletFile exportWalletFile(String password) throws CipherException {
        ECKeyPair ekp = credentials.getEcKeyPair();
        WalletFile walletFile = Wallet.createStandard(password, ekp);
        this.walletFile = walletFile;
        this.walletPass = password;
        return walletFile;
    }

    public static String exportKeyStore(WalletItem walletItem){
        return walletFileJson(walletItem.walletFile);
    }

    /**
     * wallet file的json内容
     *
     * @param wf
     * @return json格式的内容
     */
    public static String walletFileJson(WalletFile wf) {
        try {
            return objectMapper.writeValueAsString(wf);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 是否合法
     *
     * @return
     */
    public boolean valid() {
        try {
            Wallet.decrypt(walletPass, walletFile);
        } catch (CipherException e) {
            return false;
        }
        return true;
    }

    /**
     * 反序列化一个钱包
     *
     * @param content
     * @return 钱包文件
     */
    public static WalletFile createWalletFile(byte[] content) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            WalletFile walletFile = objectMapper.readValue(content, WalletFile.class);
            return walletFile;
        } catch (IOException ex) {

        }
        return null;
    }

    /**
     * 反序列化钱包
     *
     * @param content
     * @return 钱包文件
     */
    public static WalletFile createWalletFile(String content) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            WalletFile walletFile = objectMapper.readValue(content, WalletFile.class);
            return walletFile;
        } catch (IOException ex) {

        }
        return null;
    }

    /**
     * 反序列化一个钱包
     *
     * @param file
     * @return 钱包文件
     */
    public static WalletFile createWalletFile(File file) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            WalletFile walletFile = objectMapper.readValue(file, WalletFile.class);
            return walletFile;
        } catch (IOException ex) {

        }
        return null;
    }

    /**
     * 地址转换
     *
     * @param number 公钥
     * @return 地址
     */
    public static String address(BigInteger number) {
        return Keys.toChecksumAddress(Keys.getAddress(number));
    }

    /**
     * 私钥转换
     *
     * @param prik 私钥
     * @return 私钥的hex格式
     */
    public static String privateKey(BigInteger prik) {
        return Numeric.toHexStringWithPrefixZeroPadded(prik, Keys.PRIVATE_KEY_LENGTH_IN_HEX);
    }

    public String getAddress() {
        return address(credentials.getEcKeyPair().getPublicKey());
    }

    public String getPrivateKey() {
        return privateKey(credentials.getEcKeyPair().getPrivateKey());
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getWalletPass() {
        return walletPass;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public WalletFile getWalletFile() {
        return walletFile;
    }

    public String getPath() {
        return path;
    }

}
