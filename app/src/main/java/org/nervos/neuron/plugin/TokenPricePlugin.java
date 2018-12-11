package org.nervos.neuron.plugin;

import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.service.http.NeuronSubscriber;
import org.nervos.neuron.service.http.TokenService;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.JSLoadUtils;

/**
 * Created by BaojunCZ on 2018/10/19.
 */
public class TokenPricePlugin {

    private WebView mWebView;

    public TokenPricePlugin(WebView webView) {
        mWebView = webView;
    }

    @JavascriptInterface
    public void getTokenPrice(String symbol, String callback) {
        CurrencyItem currencyItem = CurrencyUtil.getCurrencyItem(mWebView.getContext());
        TokenService.getCurrency(symbol, currencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) {
                            JSLoadUtils.INSTANCE.loadFunc(mWebView, callback, "");
                        } else {
                            JSLoadUtils.INSTANCE.loadFunc(mWebView, callback
                                    , currencyItem.getSymbol() + CurrencyUtil.formatCurrency(Double.valueOf(price)));
                        }
                    }
                });
    }
}
