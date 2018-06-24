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
     * Credentials
     */
    private Credentials credentials;
    /**
     * wallet password
     */
    private String walletPass;
    /**
     *  KeyStore file
     */
    private WalletFile walletFile;
    private String mnemonic;
    /**
     * mnemonic password
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
     * create a wallet with mnemonic
     *
     * @param password mnemonic password and imToken password is null
     * @param path
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
     * import mnemonic to generate a wallet
     *
     * @param mnemonic
     * @param path      m/44'/60'/0'／0／0   m/purpse'/coin_type'/account'/change/address_index
     * @return wallet
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
     * import private key to generate a wallet
     *
     * @param privateKey
     * @return wallet
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
     * import keystore to generate a wallet
     *
     * @param keystore
     * @return wallet
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
     * @param seed
     * @param path  m/44'/60'/0'／0／0
     * @return key
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
     * export a new wallet file
     *
     * @param privateKey
     * @return wallet file
     */
    public static String exportKeyStore(String password, String privateKey){
        WalletFile walletFile = null;
        try {
            Credentials credentials = Credentials.create(privateKey);
            ECKeyPair ekp = credentials.getEcKeyPair();
            walletFile = Wallet.create(password, ekp, 1024, 1);
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return walletFileJson(walletFile);
    }

    /**
     * json content of wallet file
     *
     * @param wf  wallet file
     * @return json content
     */
    public static String walletFileJson(WalletFile wf) {
        try {
            return objectMapper.writeValueAsString(wf);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * check wallet file valid
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
     * get a wallet file from byte content
     *
     * @param content
     * @return wallet file
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
     * get a wallet file from string content
     *
     * @param content
     * @return wallet file
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
     * get a wallet file from file
     *
     * @param file
     * @return wallet file
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
     * get address from public key
     *
     * @param publicKey
     * @return address
     */
    public static String address(BigInteger publicKey) {
        return Keys.toChecksumAddress(Keys.getAddress(publicKey));
    }

    /**
     * get private key from BigInteger
     *
     * @param prik private key
     * @return hex string of private key
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
