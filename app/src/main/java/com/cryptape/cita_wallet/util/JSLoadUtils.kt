package com.cryptape.cita_wallet.util

import android.webkit.WebView

/**
 * Created by BaojunCZ on 2018/10/18.
 */
object JSLoadUtils {
    fun loadFunc(webView: WebView, func: String) {
        webView.loadUrl("javascript:$func()")
    }

    fun loadFunc(webView: WebView, func: String, arg1: String) {
        webView.loadUrl("javascript:$func('$arg1')")
    }

    fun loadFunc(webView: WebView, func: String, arg1: String, arg2: String) {
        webView.loadUrl("javascript:$func('$arg1','$arg2')")
    }
}
