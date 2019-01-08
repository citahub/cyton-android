package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.item.Chain;
import org.nervos.neuron.item.Token;
import org.nervos.neuron.item.Wallet;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.ether.EtherUtil;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by duanyytop on 2018/5/31
 */
public class WalletService {

    public static Observable<Token> getTokenBalance(Context context, Token token) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context).address)
                .flatMap(new Func1<String, Observable<Double>>() {
                    @Override
                    public Observable<Double> call(String address) {
                        if (EtherUtil.isEther(token)) {
                            token.chainName = ConstantUtil.ETH;
                            return EtherUtil.isNative(token)
                                    ? EthRpcService.getEthBalance(address)
                                    :EthRpcService.getERC20Balance(token.contractAddress, address);
                        } else {
                            Chain chain = DBWalletUtil.getChainItemFromCurrentWallet(context, token.getChainId());
                            token.chainName = Objects.requireNonNull(chain).name;
                            AppChainRpcService.init(context, Objects.requireNonNull(chain).httpProvider);
                            return EtherUtil.isNative(token)
                                    ? AppChainRpcService.getBalance(address)
                                    : AppChainRpcService.getErc20Balance(token, address);
                        }
                    }
                }).map(balance -> {
                    token.balance = balance;
                    return token;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Double> getBalanceWithToken(Context context, Token token) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        Observable<Double> balanceObservable;
        if (EtherUtil.isEther(token)) {
            token.chainName = ConstantUtil.ETH;
            balanceObservable = EtherUtil.isNative(token)
                    ? EthRpcService.getEthBalance(wallet.address)
                    : EthRpcService.getERC20Balance(token.contractAddress, wallet.address);
        } else {
            Chain chain = DBWalletUtil.getChainItemFromCurrentWallet(context, token.getChainId());
            token.chainName = Objects.requireNonNull(chain).name;
            AppChainRpcService.init(context, Objects.requireNonNull(chain).httpProvider);
            balanceObservable = EtherUtil.isNative(token)
                    ? AppChainRpcService.getBalance(wallet.address)
                    : AppChainRpcService.getErc20Balance(token, wallet.address);
        }
        return balanceObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Double> getBalanceWithNativeToken(Context context, Token token) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        Observable<Double> balanceObservable;
        if (EtherUtil.isEther(token)) {
            token.chainName = ConstantUtil.ETH;
            balanceObservable = EthRpcService.getEthBalance(wallet.address);
        } else {
            Chain chain = DBWalletUtil.getChainItemFromCurrentWallet(context, token.getChainId());
            token.chainName = Objects.requireNonNull(chain).name;
            AppChainRpcService.init(context, Objects.requireNonNull(chain).httpProvider);
            balanceObservable = AppChainRpcService.getBalance(wallet.address);
        }
        return balanceObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static boolean checkPassword(Context context, String password, Wallet wallet) {
        try {
            Credentials credentials;
            if (!TextUtils.isEmpty(wallet.keystore)) {
                WalletEntity walletEntity = WalletEntity.fromKeyStore(password, wallet.keystore);
                credentials = walletEntity.getCredentials();
            } else {
                String privateKey = AESCrypt.decrypt(password, wallet.cryptPrivateKey);
                WalletEntity walletEntity = WalletEntity.fromPrivateKey(Numeric.toBigInt(privateKey), password);
                credentials = walletEntity.getCredentials();
                wallet.keystore = walletEntity.getKeystore();
                wallet.cryptPrivateKey = "";
                DBWalletUtil.saveWallet(context, wallet);
                reInitRpcService(context);
            }
            return wallet.address.equalsIgnoreCase(credentials.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void reInitRpcService(Context context) {
        EthRpcService.init(context);
        AppChainRpcService.init(context);
    }

}
