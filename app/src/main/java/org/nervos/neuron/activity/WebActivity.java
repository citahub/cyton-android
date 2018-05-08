package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebActivity extends AppCompatActivity {

//    private static final String URL = "http://172.20.10.10:8080/";
//    private static final String URL = "http://192.168.2.84:8080/";
    private static final String URL = "https://www.cryptokitties.co/";
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

        initWebSettings();
        initWebView();
        webView.loadUrl(URL);

    }

    private void initWebView() {
        webView.addJavascriptInterface(new JSObject(this), "jsObject");

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress <= 25) {
                    mIsJsInjected = false;
                } else if (!mIsJsInjected && !TextUtils.equals(mLastUrl, webview.getUrl())) {
                    injectJs(webview);
                }
                if (newProgress > 75 && !mIsJsInjected) {
                    injectJs(webview);
                }
                Log.d("CitaWallet", "progress: " + newProgress);
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


    private void injectJs(WebView webView) {
        mIsJsInjected = true;
        mLastUrl = webView.getUrl();
        webView.evaluateJavascript(getWeb3Js(), null);
//        webView.evaluateJavascript(getHttpProviderJs(), null);
        webView.evaluateJavascript(getInjectJs(), null);
    }

    private String getWeb3Js() {
        return getInjectedJsFile("web3.js");
    }

    private String getHttpProviderJs() {
        return getInjectedJsFile("httpprovider.js");
    }

    private String  getInjectJs() {
        return "javascript: var web3 = new Web3(); web3.initWeb3(); ";
    }


    /**
     * read String content of rejected JavaScript file from assets
     * @param fileName    the name of JavaScript file
     * @return  the content of JavaScript file
     */
    private String getInjectedJsFile(String fileName) {
        AssetManager am = getAssets();
        try {
            InputStream in = am.open(fileName);
            byte buff[] = new byte[1024];
            ByteArrayOutputStream fromFile = new ByteArrayOutputStream();
            do {
                int num = in.read(buff);
                if (num <= 0) break;
                fromFile.write(buff, 0, num);
            } while (true);
            return fromFile.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class JSObject {
        private Context context;
        public JSObject(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void showTransaction(String payload) {
            Log.d("showTransaction", "payload: " + payload);
            Toast.makeText(context, "show transaction " + payload, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettings() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);

        webSettings.setJavaScriptEnabled(true);
        //支持通过js打开新的窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //启动local storage
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        webSettings.setBuiltInZoomControls(false);

        // 开启调试模式
        WebView.setWebContentsDebuggingEnabled(true);
    }

}
