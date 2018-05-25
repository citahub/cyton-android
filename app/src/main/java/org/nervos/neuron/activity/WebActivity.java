package org.nervos.neuron.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.util.WebUtil;

public class WebActivity extends BaseActivity {

//    private static final String URL = "http://172.20.10.10:8080/";
//    private static final String URL = "http://192.168.2.84:8080/";
    private static final String MANIFEST_URL = "http://47.97.171.140:8095/contracts/new";
    private static final String PROVIDER = "http://39.104.94.244:1301";
    private WebView webView;
    private Boolean mIsJsInjected;
    private String mLastUrl;
    private TextView titleText;
    private TextView collectText;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        url = getIntent().getStringExtra(AddWebsiteActivity.EXTRA_URL);

        initTitleView();
        initWebView();
        webView.loadUrl(TextUtils.isEmpty(url)? MANIFEST_URL:url);
        WebUtil.getHtmlManifest(this, MANIFEST_URL);

    }

    private void initTitleView() {
        titleText = findViewById(R.id.title_bar_center);
        titleText.setText("浏览器");
        collectText = findViewById(R.id.menu_collect);
        initCollectView();
        findViewById(R.id.title_left_close).setOnClickListener(v -> finish());
        findViewById(R.id.title_bar_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMenuView();
            }
        });
    }

    private void initMenuView() {
        findViewById(R.id.menu_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_background).setVisibility(View.VISIBLE);
        findViewById(R.id.menu_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.menu_layout).setVisibility(View.GONE);
                findViewById(R.id.menu_background).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.menu_collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WebUtil.isCollectApp(mActivity)) {
                    WebUtil.cancelCollectApp(mActivity);
                } else {
                    WebUtil.collectApp(mActivity);
                }
                initCollectView();
            }
        });
        findViewById(R.id.menu_reload).setOnClickListener(v1 -> webView.reload());
        findViewById(R.id.menu_dapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "dapp详情", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCollectView() {
        collectText.setText(WebUtil.isCollectApp(mActivity)? "取消收藏":"收藏");
        findViewById(R.id.menu_layout).setVisibility(View.GONE);
        findViewById(R.id.menu_background).setVisibility(View.GONE);
    }


    private void initWebView() {
        webView = findViewById(R.id.webview);
        WebUtil.initWebSettings(webView.getSettings());
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
                titleText.setText(title);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
