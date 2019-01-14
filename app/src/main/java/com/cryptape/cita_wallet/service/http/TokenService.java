package com.cryptape.cita_wallet.service.http;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Request;
import org.json.JSONException;
import org.json.JSONObject;
import com.cryptape.cita_wallet.item.CurrencyId;
import com.cryptape.cita_wallet.item.CurrencyIdList;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.response.CollectionResponse;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.constant.url.HttpUrls;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class TokenService {

    private static final String PARAM_ID = "@ID";
    private static final String PARAM_CURRENCY = "@Currency";

    private static ArrayList<CurrencyId> list = null;

    public static Observable<String> getCurrency(String symbol, String currency) {
        return getTokenID(symbol)
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String ID) {
                        if (!TextUtils.isEmpty(ID)) {
                            String url = HttpUrls.TOKEN_CURRENCY.replace(PARAM_ID, ID).replace(PARAM_CURRENCY, currency);
                            Request request = new Request.Builder().url(url).build();
                            Call call = HttpService.getHttpClient().newCall(request);
                            try {
                                String response = call.execute().body().string();
                                return Observable.just(fetchPriceFromResponse(response, currency));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<String> getTokenCurrency(String ID, String currency) {
        return Observable.fromCallable(() -> {
            String url = HttpUrls.TOKEN_CURRENCY.replace("@ID", ID).replace("@Currency", currency);
            Request request = new Request.Builder().url(url).build();
            Call call = HttpService.getHttpClient().newCall(request);
            return fetchPriceFromResponse(call.execute().body().string(), currency);
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static String fetchPriceFromResponse(String response, String currency) throws JSONException {
        JSONObject object = new JSONObject(response);
        JSONObject data = object.optJSONObject("data");
        JSONObject quotes = data.optJSONObject("quotes");
        JSONObject quote = quotes.optJSONObject(currency);
        if (quote != null) {
            return quote.optString("price");
        } else {
            return null;
        }
    }

    public static Observable<String> getTokenID(String symbol) {
        return Observable.fromCallable(() -> {
            if (list == null) {
                Request request = new Request.Builder().url(HttpUrls.TOKEN_ID).build();
                Call call = HttpService.getHttpClient().newCall(request);
                String response = "";
                try {
                    response = call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                list = new Gson().fromJson(response, CurrencyIdList.class).getList();
            }
            return list;
        }).subscribeOn(Schedulers.newThread())
                .flatMap((Func1<ArrayList<CurrencyId>, Observable<String>>) currencyIDItems -> {
                    for (CurrencyId item : list) {
                        if (item.getSymbol().equals(symbol)) {
                            return Observable.just(item.getId());
                        }
                    }
                    return Observable.just(null);
                });
    }


    /**
     * get ERC721 token transaction list
     *
     * @param context
     * @return
     */
    public static Observable<CollectionResponse> getCollectionList(Context context) {
        Wallet wallet = DBWalletUtil.getCurrentWallet(context);
        return Observable.fromCallable(new Callable<CollectionResponse>() {
            @Override
            public CollectionResponse call() throws Exception {
                Request request = new Request.Builder()
                        .url(HttpUrls.COLLECTION_LIST_URL + wallet.address).build();
                Call call = HttpService.getHttpClient().newCall(request);
                String response = "";
                try {
                    response = call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new Gson().fromJson(response, CollectionResponse.class);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
