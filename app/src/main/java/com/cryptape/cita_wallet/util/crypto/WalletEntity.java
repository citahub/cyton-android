package com.cryptape.cita_wallet.util.crypto;

import android.content.Context;

import java.io.File;
import java.math.BigInteger;
import java.security.*;

import com.cryptape.cita_wallet.util.crypto.bip44.HdKeyNode;
import com.cryptape.cita_wallet.util.crypto.bip44.hdpath.HdKeyPath;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.web3j.crypto.*;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;


public class WalletEntity {

    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PASSPHRASE = "";

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Credentials credentials;
    private WalletFile walletFile;
    private String mnemonic;
    private String passphrase;
    private String path;
    private String keystore;

    private WalletEntity() {
    }

    public static void initWalletMnemonic(Context context) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                MnemonicUtils.initWordList(context);
            }
        }.start();
    }


    public static WalletEntity createWithMnemonic(String path, String password) {

        WalletEntity wa = new WalletEntity();
        byte[] initialEntropy = new byte[16];
        secureRandom.nextBytes(initialEntropy);
        String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, PASSPHRASE);
        ECKeyPair ecKeyPair = createBip44NodeFromSeed(seed, path);
        try {
            wa.walletFile = Wallet.create(password, ecKeyPair, 1024, 1);
        } catch (CipherException e) {
            e.printStackTrace();
        }
        wa.keystore = new Gson().toJson(wa.walletFile);
        wa.credentials = Credentials.create(ecKeyPair);
        wa.mnemonic = mnemonic;
        wa.passphrase = PASSPHRASE;
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
    public static WalletEntity fromMnemonic(String mnemonic, String path, String password) throws Exception {
        WalletEntity wa = new WalletEntity();
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, PASSPHRASE);
        ECKeyPair ecKeyPair = createBip44NodeFromSeed(seed, path);
        wa.walletFile = Wallet.create(password, ecKeyPair, 1024, 1);
        wa.keystore = new Gson().toJson(wa.walletFile);
        wa.credentials = Credentials.create(ecKeyPair);
        wa.mnemonic = mnemonic;
        wa.passphrase = PASSPHRASE;
        wa.path = path;
        return wa;
    }


    public static WalletEntity fromPrivateKey(BigInteger privateKey, String password) throws CipherException {
        WalletEntity wa = new WalletEntity();
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        wa.walletFile = Wallet.create(password, ecKeyPair, 1024, 1);
        wa.keystore = new Gson().toJson(wa.walletFile);
        wa.credentials = Credentials.create(ecKeyPair);
        return wa;
    }


    public static WalletEntity fromKeyStore(String password, String keystore) throws Exception {
        WalletEntity wa = new WalletEntity();
        wa.keystore = keystore;
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
        return ECKeyPair.create(privateKeyByte);
    }

    public static String exportKeyStore(String password, String privateKey) {
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

    public static String walletFileJson(WalletFile wf) {
        try {
            return objectMapper.writeValueAsString(wf);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static WalletFile createWalletFile(byte[] content) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            return objectMapper.readValue(content, WalletFile.class);
        } catch (IOException ex) {

        }
        return null;
    }

    public static WalletFile createWalletFile(String content) {
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            return objectMapper.readValue(content, WalletFile.class);
        } catch (IOException ex) {

        }
        return null;
    }

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

    public Credentials getCredentials() {
        return credentials;
    }

    public WalletFile getWalletFile() {
        return walletFile;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getPath() {
        return path;
    }

}
