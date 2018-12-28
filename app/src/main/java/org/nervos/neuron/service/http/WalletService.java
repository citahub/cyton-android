package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
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

    public static Observable<TokenItem> getTokenBalance(Context context, TokenItem tokenItem) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context).address)
                .flatMap(new Func1<String, Observable<Double>>() {
                    @Override
                    public Observable<Double> call(String address) {
                        if (EtherUtil.isEther(tokenItem)) {
                            tokenItem.chainName = ConstantUtil.ETH;
                            return EtherUtil.isNative(tokenItem)
                                    ? EthRpcService.getEthBalance(address)
                                    :EthRpcService.getERC20Balance(tokenItem.contractAddress, address);
                        } else {
                            ChainItem chainItem = DBWalletUtil.getChainItemFromCurrentWallet(context, tokenItem.getChainId());
                            tokenItem.chainName = Objects.requireNonNull(chainItem).name;
                            AppChainRpcService.init(context, Objects.requireNonNull(chainItem).httpProvider);
                            return EtherUtil.isNative(tokenItem)
                                    ? AppChainRpcService.getBalance(address)
                                    : AppChainRpcService.getErc20Balance(tokenItem, address);
                        }
                    }
                }).map(balance -> {
                    tokenItem.balance = balance;
                    return tokenItem;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Double> getBalanceWithToken(Context context, TokenItem tokenItem) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        Observable<Double> balanceObservable;
        if (EtherUtil.isEther(tokenItem)) {
            tokenItem.chainName = ConstantUtil.ETH;
            balanceObservable = EtherUtil.isNative(tokenItem)
                    ? EthRpcService.getEthBalance(walletItem.address)
                    : EthRpcService.getERC20Balance(tokenItem.contractAddress, walletItem.address);
        } else {
            ChainItem chainItem = DBWalletUtil.getChainItemFromCurrentWallet(context, tokenItem.getChainId());
            tokenItem.chainName = Objects.requireNonNull(chainItem).name;
            AppChainRpcService.init(context, Objects.requireNonNull(chainItem).httpProvider);
            balanceObservable = EtherUtil.isNative(tokenItem)
                    ? AppChainRpcService.getBalance(walletItem.address)
                    : AppChainRpcService.getErc20Balance(tokenItem, walletItem.address);
        }
        return balanceObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Double> getBalanceWithNativeToken(Context context, TokenItem tokenItem) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        Observable<Double> balanceObservable;
        if (EtherUtil.isEther(tokenItem)) {
            tokenItem.chainName = ConstantUtil.ETH;
            balanceObservable = EthRpcService.getEthBalance(walletItem.address);
        } else {
            ChainItem chainItem = DBWalletUtil.getChainItemFromCurrentWallet(context, tokenItem.getChainId());
            tokenItem.chainName = Objects.requireNonNull(chainItem).name;
            AppChainRpcService.init(context, Objects.requireNonNull(chainItem).httpProvider);
            balanceObservable = AppChainRpcService.getBalance(walletItem.address);
        }
        return balanceObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static boolean checkPassword(Context context, String password, WalletItem walletItem) {
        try {
            Credentials credentials;
            if (!TextUtils.isEmpty(walletItem.keystore)) {
                WalletEntity walletEntity = WalletEntity.fromKeyStore(password, walletItem.keystore);
                credentials = walletEntity.getCredentials();
            } else {
                String privateKey = AESCrypt.decrypt(password, walletItem.cryptPrivateKey);
                WalletEntity walletEntity = WalletEntity.fromPrivateKey(Numeric.toBigInt(privateKey), password);
                credentials = walletEntity.getCredentials();
                walletItem.keystore = walletEntity.getKeystore();
                walletItem.cryptPrivateKey = "";
                DBWalletUtil.saveWallet(context, walletItem);
                reInitRpcService(context);
            }
            return walletItem.address.equalsIgnoreCase(credentials.getAddress());
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
