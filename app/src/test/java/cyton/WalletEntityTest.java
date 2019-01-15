package cyton;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.cryptape.cita_wallet.util.crypto.MnemonicUtils;
import com.cryptape.cita_wallet.util.crypto.WalletEntity;
import org.web3j.utils.Numeric;

/**
 * Created by duanyytop on 2019/1/9.
 */
public class WalletEntityTest {

    private static final String PRIVATE_KEY = "0x95ba839425d01128a08722aa68984ae5da4352fdba5d5c0cfdb8d9a355a264d2";
    private static final String MNEMONIC = "come assume destroy crouch old original yard lamp diesel inform country lawn";
    private static final String ADDRESS = "0xfd4BB01f1fbF45A39e9E44a1B7f1310868599E0d";
    private static final String PATH = "m/44'/60'/0'/0/0";
    private static final String PASSWORD = "password";
    private static final String KEYSTORE = "{\"address\":\"fd4bb01f1fbf45a39e9e44a1b7f1310868599e0d\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"f5841a190c6c6f3acbd5f1be7d9d6e0393ebe4164f68ad491df5bc27f229ae21\",\"cipherparams\":{\"iv\":\"e833fe0fc01a8b366413110aa17bb646\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":1024,\"p\":1,\"r\":8,\"salt\":\"2c0d9ad3643f8f60cb41c6c1826549814402d75477e0cd15836b2639ac603941\"},\"mac\":\"7d2b983a06ec7fbf76ee1f17fe5d2c4fcb774d22caadf581fb95e291deb21829\"},\"id\":\"52896213-888f-4865-992d-646f3cd0a325\",\"version\":3}\n";

    @Before
    public void init() {
        MnemonicUtils.initMnemonicWord();
    }

    @Test
    public void testImportMnemonic() throws Exception {
        WalletEntity walletEntity = WalletEntity.fromMnemonic(MNEMONIC, PATH, PASSWORD);
        Assert.assertEquals(walletEntity.getAddress(), ADDRESS);
        Assert.assertEquals(walletEntity.getPrivateKey(), PRIVATE_KEY);
    }

    @Test
    public void testImportInvalidMnemonic() {
        try {
            WalletEntity.fromMnemonic("abc", PATH, PASSWORD);
            Assert.fail("Should have thrown import mnemonic exception");
        } catch(Exception e) {

        }
    }

    @Test
    public void testImportKeystore() throws Exception {
        WalletEntity walletEntity = WalletEntity.fromKeyStore(PASSWORD, KEYSTORE);
        Assert.assertEquals(walletEntity.getAddress(), ADDRESS);
        Assert.assertEquals(walletEntity.getPrivateKey(), PRIVATE_KEY);
    }


    @Test
    public void testImportInvalidKeystore() {
        try {
            WalletEntity.fromKeyStore(PASSWORD, "abc");
            Assert.fail("Should have thrown import keystore exception");
        } catch(Exception e) {

        }

        try {
            WalletEntity.fromKeyStore("abc", KEYSTORE);
            Assert.fail("Should have thrown import keystore exception");
        } catch(Exception e) {

        }
    }

    @Test
    public void testImportPrivateKey() throws Exception {
        WalletEntity walletEntity = WalletEntity.fromPrivateKey(Numeric.toBigInt(PRIVATE_KEY), PASSWORD);
        Assert.assertEquals(walletEntity.getAddress(), ADDRESS);
    }

}
