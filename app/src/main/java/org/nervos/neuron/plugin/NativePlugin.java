package org.nervos.neuron.plugin;

import android.app.Activity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.JSLoadUtils;

/**
 * Created by BaojunCZ on 2018/10/19.
 */
public class NativePlugin {

    private Activity mContext;
    private WebView mWebView;

    public NativePlugin(Activity context, WebView webView) {
        mContext = context;
        mWebView = webView;
    }

    @JavascriptInterface
    public void getTokenPrice(String symbol, String callback) {
        CurrencyItem currencyItem = CurrencyUtil.getCurrencyItem(mContext);
        TokenService.getCurrency(symbol, currencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) {
                            JSLoadUtils.loadFunc(mWebView, callback, "");
                        } else {
                            JSLoadUtils.loadFunc(mWebView, callback
                                    , currencyItem.getSymbol() + price);
                        }
                    }
                });
    }
}
