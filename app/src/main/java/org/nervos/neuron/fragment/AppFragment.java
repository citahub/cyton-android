package org.nervos.neuron.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AppWebActivity;
import org.nervos.neuron.event.AppCollectEvent;
import org.nervos.neuron.event.AppHistoryEvent;
import org.nervos.neuron.plugin.AppTabPlugin;
import org.nervos.neuron.util.url.HttpUrls;
import org.nervos.neuron.util.web.WebAppUtil;
import org.nervos.neuron.view.WebErrorView;
import org.nervos.neuron.view.webview.SimpleWebViewClient;

/**
 * Created by duanyytop on 2018/5/18
 */
public class AppFragment extends Fragment {

    public static final String TAG = AppFragment.class.getName();

    private WebView webView;
    private WebErrorView webErrorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        webView = view.findViewById(R.id.webview);
        webErrorView = view.findViewById(R.id.view_web_error);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.loadUrl(HttpUrls.DISCOVER_URL);
        initWebSettings();
        initWebView();
        webErrorView.setImpl((reloadUrl) -> {
            webView.loadUrl(reloadUrl);
            webView.setVisibility(View.VISIBLE);
            webErrorView.setVisibility(View.GONE);
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettings() {
        SensorsDataAPI.sharedInstance()
                .showUpWebView(webView, false, true);
        WebAppUtil.initWebSettings(webView.getSettings());
        WebAppUtil.initWebViewCache(getContext(), webView.getSettings());
    }

    private void initWebView() {

        webView.setWebViewClient(new SimpleWebViewClient(webErrorView) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(getContext(), AppWebActivity.class);
                intent.putExtra(AppWebActivity.EXTRA_URL, url);
                startActivity(intent);
                return true;
            }
        });

        webView.addJavascriptInterface(new AppTabPlugin(getContext()), "appHybrid");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppCollectEvent(AppCollectEvent event) {
        if (event.isCollect) {
            String app = new Gson().toJson(event.appInfo);
            webView.loadUrl("javascript:__mydapp.add(" + app + ")");
        } else {
            webView.loadUrl("javascript:__mydapp.remove('" + event.appInfo.entry + "')");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppHistoryEvent(AppHistoryEvent event) {
        String app = new Gson().toJson(event.appInfo);
        webView.loadUrl("javascript:window.__myhistory.add(" + app + ")");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault()
                .register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault()
                .unregister(this);
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

}
