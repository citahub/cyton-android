package org.nervos.neuron.service;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.response.EthTransactionResponse;
import org.nervos.neuron.response.NervosTransactionResponse;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;

import java.io.IOException;
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

public class NervosHttpService {

    private static OkHttpClient mOkHttpClient;
    private static EthMetaData.EthMetaDataResult ethMetaDataResult;

    public static OkHttpClient getHttpClient() {
        if (mOkHttpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            mOkHttpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
        }
        return mOkHttpClient;
    }

    public static Observable<List<TransactionItem>> getTransactionList(Context context) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<EthMetaData.EthMetaDataResult>() {
                @Override
                public EthMetaData.EthMetaDataResult call() {
                    NervosRpcService.init(context, HttpUrls.NERVOS_NODE_IP);
                    return NervosRpcService.getMetaData().getEthMetaDataResult();
                }
            }).flatMap(new Func1<EthMetaData.EthMetaDataResult, Observable<List<TransactionItem>>>() {
                @Override
                public Observable<List<TransactionItem>> call(EthMetaData.EthMetaDataResult result) {
                    ethMetaDataResult = result;
                    try {
                        String nervosUrl = HttpUrls.NERVOS_TRANSACTION_URL + walletItem.address;
                        final Request nervosRequest = new Request.Builder().url(nervosUrl).build();
                        Call nervosCall = NervosHttpService.getHttpClient().newCall(nervosRequest);

                        NervosTransactionResponse response = new Gson().fromJson(nervosCall.execute()
                                .body().string(), NervosTransactionResponse.class);
                        for (TransactionItem item : response.result.transactions) {
                            item.chainName = ethMetaDataResult.chainName;
                            item.value = NumberUtil.getEthFromWeiForStringDecimal6(item.value)
                                    + ethMetaDataResult.tokenSymbol;
                        }
                        return Observable.just(response.result.transactions);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Observable.just(null);
                }
            }).flatMap(new Func1<List<TransactionItem>, Observable<List<TransactionItem>>>() {
                @Override
                public Observable<List<TransactionItem>> call(List<TransactionItem> list) {
                    try {
                        String ethUrl = HttpUrls.ETH_TRANSACTION_URL + walletItem.address;
                        final Request ethRequest = new Request.Builder().url(ethUrl).build();
                        Call ethCall = NervosHttpService.getHttpClient().newCall(ethRequest);
                        EthTransactionResponse response = new Gson().fromJson(ethCall.execute()
                                .body().string(), EthTransactionResponse.class);
                        for(TransactionItem item : response.result) {
                            item.chainName = ConstUtil.ETH_MAINNET;
                            item.value = (NumberUtil.getEthFromWeiForStringDecimal6(item.value) + ConstUtil.ETH);
                        }
                        if (list != null && list.size() != 0) {
                            response.result.addAll(list);
                        }
                        return Observable.just(response.result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Observable.just(null);
                }
            }).subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread());
        }




    public static Observable<List<TransactionItem>> getETHTransactionList(Context context) {
        return Observable.just(DBWalletUtil.getCurrentWallet(context))
                .flatMap(new Func1<WalletItem, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(WalletItem walletItem) {
                        try {
                            String ethUrl = HttpUrls.ETH_TRANSACTION_URL + walletItem.address;
                            final Request ethRequest = new Request.Builder().url(ethUrl).build();
                            Call ethCall = NervosHttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute()
                                    .body().string(), EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for(TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.getEthFromWeiForStringDecimal6(item.value) + ConstUtil.ETH);
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


    public static Observable<List<TransactionItem>> getNervosTransactionList(Context context) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<EthMetaData.EthMetaDataResult>() {
                    @Override
                    public EthMetaData.EthMetaDataResult call() {
                        NervosRpcService.init(context, HttpUrls.NERVOS_NODE_IP);
                        return NervosRpcService.getMetaData().getEthMetaDataResult();
                    }
                }).flatMap(new Func1<EthMetaData.EthMetaDataResult, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(EthMetaData.EthMetaDataResult result) {
                        try {
                            String nervosUrl = HttpUrls.NERVOS_TRANSACTION_URL + walletItem.address;
                            final Request nervosRequest = new Request.Builder().url(nervosUrl).build();
                            Call nervosCall = NervosHttpService.getHttpClient().newCall(nervosRequest);

                            NervosTransactionResponse response = new Gson().fromJson(nervosCall.execute()
                                    .body().string(), NervosTransactionResponse.class);
                            for (TransactionItem item : response.result.transactions) {
                                item.chainName = result.chainName;
                                item.value = NumberUtil.getEthFromWeiForStringDecimal6(item.value)
                                        + result.tokenSymbol;
                            }
                            return Observable.just(response.result.transactions);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                }).subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread());
    }

}
