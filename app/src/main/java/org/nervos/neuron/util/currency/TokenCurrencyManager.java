package org.nervos.neuron.util.currency;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.nervos.neuron.item.CurrencyIDItem;
import org.nervos.neuron.item.CurrencyIDList;
import org.nervos.neuron.service.NervosHttpService;
import org.nervos.neuron.util.ConstUtil;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Request;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by BaojunCZ on 2018/8/3.
 */
public class TokenCurrencyManager {

    private static ArrayList<CurrencyIDItem> list = null;

    public static Observable<String> getTokenCurrency(String ID, String currency) {
        return Observable.fromCallable(() -> {
            String url = ConstUtil.Token_CURRENCY.replace("@ID", ID).replace("@Currency", currency);
            Request request = new Request.Builder().url(url).build();
            Call call = NervosHttpService.getHttpClient().newCall(request);
            String response = "";
            try {
                response = call.execute().body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject object = new JSONObject(response);
            JSONObject data = object.optJSONObject("data");
            JSONObject quotes = data.optJSONObject("quotes");
            JSONObject quote = quotes.optJSONObject(currency);
            if (quote != null) {
                return quote.optString("price");
            } else {
                return null;
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<String> getTokenID(String symbol) {
        return Observable.fromCallable(() -> {
            if (list == null) {
                Request request = new Request.Builder().url(ConstUtil.TOKEN_ID).build();
                Call call = NervosHttpService.getHttpClient().newCall(request);
                String response = "";
                try {
                    response = call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Gson gson = new Gson();
                list = gson.fromJson(response, CurrencyIDList.class).getList();
            }
            return list;
        }).subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<ArrayList<CurrencyIDItem>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ArrayList<CurrencyIDItem> currencyIDItems) {
                        CurrencyIDItem res = null;
                        for (CurrencyIDItem item : list) {
                            if (item.getSymbol().equals(symbol)) {
                                res = item;
                                break;
                            }
                        }
                        if (res == null) {
                            return Observable.just(null);
                        } else {
                            return Observable.just(res.getId());
                        }
                    }
                });
    }

}
