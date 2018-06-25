package org.nervos.neuron.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWebsiteActivity;
import org.nervos.neuron.activity.AppWebActivity;
import org.nervos.neuron.event.AppCollectEvent;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.web.WebAppUtil;

public class AppFragment extends Fragment {

    public static final String TAG = AppFragment.class.getName();


    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        webView = view.findViewById(R.id.webview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.loadUrl(ConstantUtil.DISCOVER_URL);
        initWebSettings();
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebSettings() {
        WebAppUtil.initWebSettings(webView.getSettings());
        WebAppUtil.initWebViewCache(getContext(), webView.getSettings());
    }

    private void initWebView() {

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(ConstantUtil.INNER_URL)) {
                    return false;
                } else {
                    LogUtil.d(url);
                    Intent intent = new Intent(getContext(), AppWebActivity.class);
                    intent.putExtra(AppWebActivity.EXTRA_URL, url);
                    startActivity(intent);
                    return true;
                }
            }
        });

        webView.addJavascriptInterface(new AppHybrid(), "appHybrid");

    }

    private class AppHybrid {

        @JavascriptInterface
        public void startAddWebsitePage() {
            startActivity(new Intent(getActivity(), AddWebsiteActivity.class));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAppCollectEvent(AppCollectEvent event) {
        if (event.isCollect) {
            String app = new Gson().toJson(event.appItem);
            webView.loadUrl("javascript:__mydapp.add("+ app + ")");
        } else {
            webView.loadUrl("javascript:__mydapp.remove('" + event.appItem.entry + "')");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
