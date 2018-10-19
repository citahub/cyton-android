package org.nervos.neuron.plugin;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.JSLoadUtils;

/**
 * Created by BaojunCZ on 2018/10/19.
 */
public class NativePlugin {

    @JavascriptInterface
    public void getTokenPrice(Context context, String symbol, String callback) {
        TokenService.getCurrency(symbol, CurrencyUtil.getCurrencyItem(context).getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (!TextUtils.isEmpty(price)) {
                            JSLoadUtils.loadFunc(callback, "");
                        } else {
                            JSLoadUtils.loadFunc(callback, price);
                        }
                    }
                });
    }
}
