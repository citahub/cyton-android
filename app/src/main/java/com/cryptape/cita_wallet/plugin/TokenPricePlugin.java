package com.cryptape.cita_wallet.plugin;

import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.cryptape.cita_wallet.item.Currency;
import com.cryptape.cita_wallet.service.http.CytonSubscriber;
import com.cryptape.cita_wallet.service.http.TokenService;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.JSLoadUtils;

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
        Currency currency = CurrencyUtil.getCurrencyItem(mWebView.getContext());
        TokenService.getCurrency(symbol, currency.getName())
                .subscribe(new CytonSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) {
                            JSLoadUtils.INSTANCE.loadFunc(mWebView, callback, "");
                        } else {
                            JSLoadUtils.INSTANCE.loadFunc(mWebView, callback
                                    , currency.getSymbol() + CurrencyUtil.formatCurrency(Double.valueOf(price)));
                        }
                    }
                });
    }
}
