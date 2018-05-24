package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.service.response.ManifestResponse;
import org.nervos.neuron.util.WebUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebActivity extends BaseActivity {

//    private static final String URL = "http://172.20.10.10:8080/";
//    private static final String URL = "http://192.168.2.84:8080/";
    private static final String MANIFEST_URL = "http://47.97.171.140:8095/contracts/new";
    private static final String PROVIDER = "http://39.104.94.244:1301";
    private WebView webView;
    private TitleBar titleBar;
    private Boolean mIsJsInjected;
    private String mLastUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webView = findViewById(R.id.webview);
        titleBar = findViewById(R.id.title);

        WebUtil.initWebSettings(webView.getSettings());
        initWebView();
        webView.loadUrl(MANIFEST_URL);
        WebUtil.getHtmlManifest(this, MANIFEST_URL);

    }

    private void initWebView() {
        webView.addJavascriptInterface(new JSObject(this), "jsObject");
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress <= 25) {
                    mIsJsInjected = false;
                } else if (!mIsJsInjected && !TextUtils.equals(mLastUrl, webview.getUrl())) {
                    injectJs();
                }
                if (newProgress > 75 && !mIsJsInjected) injectJs();
                Log.d("Web", "progress: " + newProgress);
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleBar.setTitle(title);
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
    }


    /**
     * inject js file to webview
     */
    private void injectJs() {
        mIsJsInjected = true;
        mLastUrl = webView.getUrl();
        webView.evaluateJavascript(WebUtil.getWeb3Js(this), null);
//        webView.evaluateJavascript(WebUtil.getHttpProviderJs(this), null);
        webView.evaluateJavascript(WebUtil.getInjectJs(), null);
    }


    private class JSObject {
        private Context context;
        public JSObject(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void showTransaction(String payload) {
            Toast.makeText(context, "show transaction " + payload, Toast.LENGTH_SHORT).show();
        }
    }

}
