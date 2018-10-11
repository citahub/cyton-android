package org.nervos.neuron.service;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.neuron.BuildConfig;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.item.response.EthTransactionResponse;
import org.nervos.neuron.item.response.AppChainTransactionResponse;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AppChainHttpService {

    private static OkHttpClient mOkHttpClient;

    public static OkHttpClient getHttpClient() {
        if (mOkHttpClient == null) {
            if (BuildConfig.IS_DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                mOkHttpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
            } else {
                mOkHttpClient = new OkHttpClient.Builder().build();
            }
        }
        return mOkHttpClient;
    }

    public static Observable<List<TransactionItem>> getETHTransactionList(Context context) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context))
                .flatMap(new Func1<WalletItem, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(WalletItem walletItem) {
                        try {
                            String ethUrl = HttpUrls.ETH_TRANSACTION_URL + walletItem.address;
                            final Request ethRequest = new Request.Builder().url(ethUrl).build();
                            Call ethCall = AppChainHttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute()
                                    .body().string(), EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for (TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.getEthFromWeiForStringDecimal8Sub(
                                        new BigInteger(item.value)));
                            }
                            return Observable.just(transactionItemList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<TransactionItem>> getETHERC20TransactionList(
            Context context, TokenItem tokenItem) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context))
                .flatMap(new Func1<WalletItem, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(WalletItem walletItem) {
                        try {
                            String ethUrl = HttpUrls.ETH_ERC20_TRANSACTION_URL
                                    + "&contractaddress=" + tokenItem.contractAddress
                                    + "&address=" + walletItem.address
                                    + "&page=1&offset=30";
                            final Request ethRequest = new Request.Builder().url(ethUrl).build();
                            Call ethCall = AppChainHttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute()
                                    .body().string(), EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for (TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.divideDecimal8Sub(
                                        new BigInteger(item.value), tokenItem.decimals));
                            }
                            return Observable.just(transactionItemList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public static Observable<List<TransactionItem>> getAppChainTransactionList(Context context) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<AppMetaData.AppMetaDataResult>() {
            @Override
            public AppMetaData.AppMetaDataResult call() {
                AppChainRpcService.init(context, HttpUrls.APPCHAIN_NODE_IP);
                return AppChainRpcService.getMetaData().getAppMetaDataResult();
            }
        }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<TransactionItem>>>() {
            @Override
            public Observable<List<TransactionItem>> call(AppMetaData.AppMetaDataResult result) {
                try {
                    String nervosUrl = HttpUrls.APPCHAIN_TRANSACTION_URL + walletItem.address;
                    final Request nervosRequest = new Request.Builder().url(nervosUrl).build();
                    Call nervosCall = AppChainHttpService.getHttpClient().newCall(nervosRequest);

                    AppChainTransactionResponse response = new Gson().fromJson(nervosCall.execute()
                            .body().string(), AppChainTransactionResponse.class);
                    for (TransactionItem item : response.result.transactions) {
                        item.chainName = result.chainName;
                        item.value = NumberUtil.getEthFromWeiForStringDecimal8Sub(Numeric.toBigInt(item.value));
                    }
                    return Observable.just(response.result.transactions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Observable.just(null);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
