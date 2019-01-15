package com.cryptape.cita_wallet.util.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encrypt and decrypt messages using AES 256 bit encryption.
 * <p/>
 * Created by scottab on 04/10/2014.
 */
public final class AESCrypt {

    private static final String AES_MODE = "AES/CBC/PKCS7Padding";
    private static final String CHARSET = "UTF-8";
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final String KEY_IV = "00";

    /**
     * Generates SHA256 hash of the password which is used as key
     *
     * @param password used to generated key
     * @return SHA256 of the password
     */
    private static SecretKeySpec generateKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }


    /**
     * Encrypt and encode message using 256-bit AES with key generated from password.
     *
     *
     * @param password used to generated key
     * @param message the thing you want to encrypt assumed String UTF-8
     * @return Base64 encoded CipherText
     * @throws GeneralSecurityException if problems occur during encryption
     */
    public static String encrypt(final String password, String message)
            throws GeneralSecurityException {
        try {
            final SecretKeySpec key = generateKey(password);
            byte[] cipherText = encrypt(key, getIV(), message.getBytes(CHARSET));
            return Base64.encodeToString(cipherText, Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new GeneralSecurityException(e);
        }
    }


    /**
     * More flexible AES encrypt that doesn't encode
     * @param key AES key typically 128, 192 or 256 bit
     * @param iv Initiation Vector
     * @param message in bytes (assumed it's already been decoded)
     * @return Encrypted cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    public static byte[] encrypt(final SecretKeySpec key, final byte[] iv, final byte[] message)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher.doFinal(message);
    }


    /**
     * Decrypt and decode ciphertext using 256-bit AES with key generated from password
     *
     * @param password used to generated key
     * @param base64EncodedCipherText the encrpyted message encoded with base64
     * @return message in Plain text (String UTF-8)
     * @throws GeneralSecurityException if there's an issue decrypting
     */
    public static String decrypt(final String password, String base64EncodedCipherText)
            throws GeneralSecurityException {
        try {
            final SecretKeySpec key = generateKey(password);
            byte[] decodedCipherText = Base64.decode(base64EncodedCipherText, Base64.NO_WRAP);
            byte[] decryptedBytes = decrypt(key, getIV(), decodedCipherText);
            return new String(decryptedBytes, CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new GeneralSecurityException(e);
        }
    }


    /**
     * More flexible AES decrypt that doesn't encode
     *
     * @param key AES key typically 128, 192 or 256 bit
     * @param iv Initiation Vector
     * @param decodedCipherText in bytes (assumed it's already been decoded)
     * @return Decrypted message cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    public static byte[] decrypt(final SecretKeySpec key, final byte[] iv, final byte[] decodedCipherText)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(AES_MODE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(decodedCipherText);
    }

    private static byte[] getIV() {
        return readIvFromFileOrGenerateNew();
    }

    private static SharedPreferences preferences;
    public static void init(Context context) {
        preferences = context.getSharedPreferences("ae", Context.MODE_PRIVATE);
    }

    private static byte[] readIvFromFileOrGenerateNew() {
        final String encoded = preferences.getString(KEY_IV, null);
        if (encoded == null) {
            return generateAndSaveIv();
        }
        return Base64.decode(encoded, Base64.NO_WRAP);
    }

    private static byte[] generateAndSaveIv() {
        final SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        saveIvToFile(bytes);
        return bytes;
    }

    private static void saveIvToFile(final byte[] iv) {
        final String toWrite = Base64.encodeToString(iv, Base64.NO_WRAP);
        preferences.edit().putString(KEY_IV, toWrite).apply();
    }

}
