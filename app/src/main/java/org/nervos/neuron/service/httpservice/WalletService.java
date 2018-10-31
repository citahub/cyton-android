package org.nervos.neuron.service.httpservice;

import android.content.Context;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WalletService {

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void getWalletTokenBalance(Context context,
                                             OnGetWalletTokenListener listener) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        if (walletItem == null || walletItem.tokenItems.size() == 0) return;
        List<TokenItem> tokenItemList = new ArrayList<>();
        executorService.execute(() -> {
            Iterator<TokenItem> iterator = walletItem.tokenItems.iterator();
            while (iterator.hasNext()) {
                TokenItem tokenItem = iterator.next();
                iterator.remove();
                try {
                    if (tokenItem.chainId < 0) {
                        if (ConstUtil.ETH.equals(tokenItem.symbol)) {
                            tokenItem.balance = EthRpcService.getEthBalance(walletItem.address);
                            tokenItemList.add(tokenItem);
                            track(ConstUtil.ETH, ConstUtil.ETH, tokenItem.balance);
                        } else {
                            tokenItem.balance = EthRpcService.getERC20Balance(tokenItem.contractAddress, walletItem.address);
                            tokenItemList.add(tokenItem);
                            track(ConstUtil.ETH, tokenItem.symbol, tokenItem.balance);
                        }
                    } else {
                        ChainItem chainItem = DBChainUtil.getChain(context, tokenItem.chainId);
                        if (chainItem != null) {
                            String httpProvider = chainItem.httpProvider;
                            AppChainRpcService.init(context, httpProvider);
                            if (!TextUtils.isEmpty(tokenItem.contractAddress)) {
                                tokenItem.balance = AppChainRpcService.getErc20Balance(tokenItem, walletItem.address);
                                tokenItem.chainName = chainItem.name;
                                tokenItemList.add(tokenItem);
                            } else {
                                tokenItem.balance = AppChainRpcService.getBalance(walletItem.address);
                                tokenItem.chainName = chainItem.name;
                                tokenItemList.add(tokenItem);
                            }
                            track(tokenItem.chainName, tokenItem.symbol, tokenItem.balance);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onGetWalletError(e.getMessage());
                    }
                }
            }
            walletItem.tokenItems = tokenItemList;
            if (listener != null) {
                listener.onGetWalletToken(walletItem);
            }
        });
    }

    private static void track(String chain, String type, double number) {
        try {
            JSONObject object = new JSONObject();
            object.put("currency_chain", chain);
            object.put("currency_type", type);
            object.put("currency_number", number);
            SensorsDataAPI.sharedInstance().track("possess_money", object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnGetWalletTokenListener {
        void onGetWalletToken(WalletItem walletItem);

        void onGetWalletError(String message);
    }


    public static Observable<Double> getBalanceWithToken(Context context, TokenItem tokenItem) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<Double>() {
            @Override
            public Double call() {
                try {
                    if (tokenItem.chainId < 0) {                // ethereum
                        if (ConstUtil.ETH.equals(tokenItem.symbol)) {
                            return EthRpcService.getEthBalance(walletItem.address);
                        } else {
                            return EthRpcService.getERC20Balance(
                                    tokenItem.contractAddress, walletItem.address);
                        }
                    } else {                                    // nervos
                        ChainItem chainItem = DBChainUtil.getChain(context, tokenItem.chainId);
                        if (chainItem == null) return 0.0;
                        String httpProvider = chainItem.httpProvider;
                        AppChainRpcService.init(context, httpProvider);
                        if (!TextUtils.isEmpty(tokenItem.contractAddress)) {
                            return AppChainRpcService.getErc20Balance(
                                    tokenItem, walletItem.address);
                        } else {
                            return AppChainRpcService.getBalance(walletItem.address);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0.0;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Double> getBalanceWithNativeToken(Context context, TokenItem tokenItem) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<Double>() {
            @Override
            public Double call() {
                try {
                    if (tokenItem.chainId < 0) {                // ethereum
                        return EthRpcService.getEthBalance(walletItem.address);
                    } else {                                    // appChain
                        ChainItem chainItem = DBChainUtil.getChain(context, tokenItem.chainId);
                        if (chainItem == null) return 0.0;
                        String httpProvider = chainItem.httpProvider;
                        AppChainRpcService.init(context, httpProvider);
                        return AppChainRpcService.getBalance(walletItem.address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0.0;
            }
        }).subscribeOn(Schedulers.io())
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
