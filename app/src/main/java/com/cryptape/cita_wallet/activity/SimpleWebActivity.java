package com.cryptape.cita_wallet.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.plugin.TokenPricePlugin;
import com.cryptape.cita_wallet.util.web.WebAppUtil;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.WebErrorView;
import com.cryptape.cita_wallet.view.webview.SimpleWebViewClient;

/**
 * Created by duanyytop on 2018/6/6
 */
public class SimpleWebActivity extends BaseActivity {
    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private TitleBar titleBar;
    private ProgressBar progressBar;
    private WebErrorView webErrorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web);
        String url = getIntent().getStringExtra(EXTRA_URL);
        initWebView();
        WebAppUtil.loadUrl(webView, url);
    }

    public static void gotoSimpleWeb(Context context, String url) {
        Intent intent = new Intent(context, SimpleWebActivity.class);
        intent.putExtra(SimpleWebActivity.EXTRA_URL, url);
        context.startActivity(intent);
    }

    private void initWebView() {
        webView = findViewById(R.id.webview);
        titleBar = findViewById(R.id.title);
        progressBar = findViewById(R.id.progressBar);
        webErrorView = findViewById(R.id.view_web_error);
        WebAppUtil.initWebSettings(webView.getSettings());
        webView.addJavascriptInterface(new TokenPricePlugin(webView), "tokenPricePlugin");
        webErrorView.setImpl((reloadUrl) -> {
            WebAppUtil.loadUrl(webView, reloadUrl);
            webView.setVisibility(View.VISIBLE);
            webErrorView.setVisibility(View.GONE);
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleBar.setTitle(title);
            }
        });
        webView.setWebViewClient(new SimpleWebViewClient(this, webErrorView) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("weixin://") || url.startsWith("alipay")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        PackageManager packageManager = getPackageManager();
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                    }
                }
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    WebAppUtil.loadUrl(webView, url);
                }
                return false;
            }
        });
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
