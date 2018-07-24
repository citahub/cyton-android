package org.nervos.neuron.service;

import android.content.Context;

import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.security.GeneralSecurityException;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignService {

    public static Observable<String> signEthMessage(Context context,
                                                    String message, String password) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws GeneralSecurityException {
                WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
                String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                Sign.SignatureData signatureData = Sign.signMessage(message.getBytes(),
                        ECKeyPair.create(Numeric.toBigInt(privateKey)));
                return getSignature(signatureData);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<String> signPersonalMessage(Context context,
                                                         String message, String password) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws GeneralSecurityException {
                WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
                String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);

                byte[] unSignData = ("\u0019Ethereum Signed Message:\n"
                        + message.getBytes().length
                        + Numeric.cleanHexPrefix(message)).getBytes();

                Sign.SignatureData signatureData = Sign.signMessage(unSignData,
                        ECKeyPair.create(Numeric.toBigInt(privateKey)));
                
                return getSignature(signatureData);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<String> signNervosMessage(
            Context context, String message, String password) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws GeneralSecurityException {
                WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
                String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                org.nervos.web3j.crypto.Sign.SignatureData signatureData =
                        org.nervos.web3j.crypto.Sign.signMessage(message.getBytes(),
                        org.nervos.web3j.crypto.ECKeyPair.create(Numeric.toBigInt(privateKey)));
                return Numeric.toHexString(signatureData.get_signature());
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }




    private static String getSignature(Sign.SignatureData signatureData) {
        byte[] sig = new byte[65];
        System.arraycopy(signatureData.getR(), 0, sig, 0, 32);
        System.arraycopy(signatureData.getS(), 0, sig, 32, 32);
        sig[64] = signatureData.getV();
        return Numeric.toHexString(sig);
    }


}
