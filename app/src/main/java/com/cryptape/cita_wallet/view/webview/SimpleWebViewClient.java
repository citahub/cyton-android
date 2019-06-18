package com.cryptape.cita_wallet.view.webview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.WebErrorView;

public class SimpleWebViewClient extends WebViewClient {

    private WebErrorView mWebErrorView;
    private Context mContext;

    public SimpleWebViewClient(Context context, WebErrorView webErrorView) {
        mWebErrorView = webErrorView;
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        try {
            if (!mContext.getString(R.string.about_blank).equals(view.getUrl()))
                mWebErrorView.setReloadUrl(view.getUrl());
            switch (error.getErrorCode()) {
                case ERROR_CONNECT:
                case ERROR_TIMEOUT:
                case ERROR_HOST_LOOKUP:
                    mWebErrorView.post(() -> {
                        mWebErrorView.setVisibility(View.VISIBLE);
                        view.setVisibility(View.GONE);
                        final String htmlText = "<html><head><title>Error</title></head><body></body></html>";
                        view.loadDataWithBaseURL("about:blank", htmlText, "text/html", "utf-8", null);
                    });
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        try {
            if (!view.getUrl().equals(mContext.getString(R.string.about_blank)))
                mWebErrorView.setReloadUrl(view.getUrl());
            switch (errorCode) {
                case ERROR_CONNECT:
                case ERROR_TIMEOUT:
                case ERROR_HOST_LOOKUP:
                    mWebErrorView.post(() -> {
                        mWebErrorView.setVisibility(View.VISIBLE);
                        view.setVisibility(View.GONE);
                        final String htmlText = "<html><head><title>Error</title></head><body></body></html>";
                        view.loadDataWithBaseURL("about:blank", htmlText, "text/html", "utf-8", null);
                    });
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
