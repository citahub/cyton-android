package org.nervos.neuron.service;

import android.content.Context;

import com.google.gson.Gson;

import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.neuron.BuildConfig;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.item.response.EthTransactionResponse;
import org.nervos.neuron.item.response.NervosTransactionResponse;
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

public class NervosHttpService {

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
                            Call ethCall = NervosHttpService.getHttpClient().newCall(ethRequest);
                            EthTransactionResponse response = new Gson().fromJson(ethCall.execute()
                                    .body().string(), EthTransactionResponse.class);
                            List<TransactionItem> transactionItemList = response.result;
                            for(TransactionItem item : transactionItemList) {
                                item.chainName = ConstUtil.ETH_MAINNET;
                                item.value = (NumberUtil.getEthFromWeiForStringDecimal8(
                                        new BigInteger(item.value)) + ConstUtil.ETH);
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
        return Observable.fromCallable(new Callable<AppMetaData.AppMetaDataResult>() {
                    @Override
                    public AppMetaData.AppMetaDataResult call() {
                        NervosRpcService.init(context, HttpUrls.NERVOS_NODE_IP);
                        return NervosRpcService.getMetaData().getAppMetaDataResult();
                    }
                }).flatMap(new Func1<AppMetaData.AppMetaDataResult, Observable<List<TransactionItem>>>() {
                    @Override
                    public Observable<List<TransactionItem>> call(AppMetaData.AppMetaDataResult result) {
                        try {
                            String nervosUrl = HttpUrls.NERVOS_TRANSACTION_URL + walletItem.address;
                            final Request nervosRequest = new Request.Builder().url(nervosUrl).build();
                            Call nervosCall = NervosHttpService.getHttpClient().newCall(nervosRequest);

                            NervosTransactionResponse response = new Gson().fromJson(nervosCall.execute()
                                    .body().string(), NervosTransactionResponse.class);
                            for (TransactionItem item : response.result.transactions) {
                                item.chainName = result.chainName;
                                item.value = NumberUtil.getEthFromWeiForStringDecimal8(Numeric.toBigInt(item.value))
                                        + result.tokenSymbol;
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
