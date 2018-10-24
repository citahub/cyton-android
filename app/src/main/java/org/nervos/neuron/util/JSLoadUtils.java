package org.nervos.neuron.util;

import android.webkit.WebView;

/**
 * Created by BaojunCZ on 2018/10/18.
 */
public class JSLoadUtils {
    public static void loadFunc(WebView webView, final String func) {
        webView.loadUrl("javascript:" + func + "()");
    }

    public static void loadFunc(WebView webView, final String func, final String arg1) {
        webView.loadUrl("javascript:" + func + "('" + arg1 + "')");
    }

    public static void loadFunc(WebView webView, final String func
            , final String arg1, final String arg2) {
        webView.loadUrl("javascript:" + func + "('" + arg1 + "','" + arg2 + "')");
    }
}
