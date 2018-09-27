package org.nervos.neuron.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.nervos.neuron.R;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.util.web.WebAppUtil;


public class SimpleWebActivity extends BaseActivity {
    public static final String EXTRA_URL = "extra_url";

    private WebView webView;
    private TitleBar titleBar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_web);
        String url = getIntent().getStringExtra(EXTRA_URL);
        initWebView();
        webView.loadUrl(url);
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
        WebAppUtil.initWebSettings(webView.getSettings());
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
