package org.nervos.neuron.view.webview;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SimpleWebViewClient extends WebViewClient {

    private View mWebErrorView;

    public SimpleWebViewClient(View webErrorView) {
        mWebErrorView = webErrorView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        try {
            switch (error.getErrorCode()) {
                case ERROR_AUTHENTICATION:
                case ERROR_BAD_URL:
                case ERROR_CONNECT:
                case ERROR_FAILED_SSL_HANDSHAKE:
                case ERROR_HOST_LOOKUP:
                case ERROR_PROXY_AUTHENTICATION:
                case ERROR_TIMEOUT:
                    mWebErrorView.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
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
            switch (errorCode) {
                case ERROR_AUTHENTICATION:
                case ERROR_BAD_URL:
                case ERROR_CONNECT:
                case ERROR_FAILED_SSL_HANDSHAKE:
                case ERROR_HOST_LOOKUP:
                case ERROR_PROXY_AUTHENTICATION:
                case ERROR_TIMEOUT:
                case ERROR_UNKNOWN:
                    mWebErrorView.setVisibility(View.VISIBLE);
                    view.setVisibility(View.GONE);
                    break;
            }
        } catch (Exception e) {

        }
    }

}
