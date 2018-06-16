package org.nervos.neuron.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.response.EthTransactionResponse;
import org.nervos.neuron.response.NervosTransactionResponse;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NervosHttpService {

    private static final String API_KEY = "T9GV1IF4V7YDXQ8F53U1FK2KHCE2KUUD8Z";
    private static final String NERVOS_TRANSACTION_URL = "http://47.97.171.140:4000/api/transactions";
    private static final String ETH_TRANSACTION_URL
            = "http://api.etherscan.io/api?apikey=" + API_KEY
            + "&module=account&action=txlist&sort=asc&address=";

    private static OkHttpClient mOkHttpClient;

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

        String nervosUrl = NERVOS_TRANSACTION_URL ;
//                + "?account=" + walletItem.address;
        final Request nervosRequest = new Request.Builder().url(nervosUrl).build();
        Call nervosCall = NervosHttpService.getHttpClient().newCall(nervosRequest);
        Observable<List<TransactionItem>> nervosObservable =
                Observable.fromCallable(new Callable<List<TransactionItem>>() {
            @Override
            public List<TransactionItem> call() throws Exception {
                NervosTransactionResponse response =
                        new Gson().fromJson(nervosCall.execute().body().string(), NervosTransactionResponse.class);
                return response.result.transactions;
            }
        }).subscribeOn(Schedulers.io());


        String ethUrl = ETH_TRANSACTION_URL + "0xbeef281b81d383336aca8b2b067a526227638087";
//                walletItem.address;
        final Request ethRequest = new Request.Builder().url(ethUrl).build();
        Call ethCall = NervosHttpService.getHttpClient().newCall(ethRequest);
        Observable<List<TransactionItem>> ethObservable = Observable.fromCallable(new Callable<List<TransactionItem>>() {
            @Override
            public List<TransactionItem> call() throws Exception {
                EthTransactionResponse response =
                        new Gson().fromJson(ethCall.execute().body().string(), EthTransactionResponse.class);
                return response.result;
            }
        }).subscribeOn(Schedulers.io());

        return Observable.concat(nervosObservable, ethObservable)
                .observeOn(AndroidSchedulers.mainThread());
    }

}
