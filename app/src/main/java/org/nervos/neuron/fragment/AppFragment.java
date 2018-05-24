package org.nervos.neuron.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddAppWebsiteActivity;
import org.nervos.neuron.activity.WebActivity;

public class AppFragment extends Fragment {

    private static final String DISCOVER_URL = "http://47.97.171.140:8866/";

    public static final String TAG = AppFragment.class.getName();

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        webView = view.findViewById(R.id.webview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.loadUrl(DISCOVER_URL);
        initWebSettings();
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettings() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }
        });

        webView.addJavascriptInterface(new AppHybrid(), "appHybrid");

    }

    private class AppHybrid {

        @JavascriptInterface
        public void startAddWebsitePage() {
            startActivity(new Intent(getActivity(), AddAppWebsiteActivity.class));
        }
    }


}
