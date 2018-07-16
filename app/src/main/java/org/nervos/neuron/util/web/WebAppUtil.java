package org.nervos.neuron.util.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nervos.neuron.R;
import org.nervos.neuron.event.AppCollectEvent;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosHttpService;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.DBAppUtil;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.Request;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WebAppUtil {

    private static final String WEB_ICON_PATH = "favicon.ico";
    private static AppItem mAppItem;

    /**
     * get app information from manifest.json and getMetaData from each http provider host
     * @param webView
     * @param url           the web url from the third part
     */
    public static void getHttpManifest(WebView webView, String url) {
        Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws IOException {
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.getElementsByTag("link");
                for(Element element: elements) {
                    if ("manifest".equals(element.attr("ref"))) {
                        return element.attr("href");
                    }
                }
                return "";
            }
        }).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String path) {
                return !TextUtils.isEmpty(path);
            }
        }).flatMap(new Func1<String, Observable<AppItem>>() {
            @Override
            public Observable<AppItem> call(String path) {
                URI uri = URI.create(url);
                String manifestUrl = uri.getScheme() + "://" + uri.getAuthority() + path;
                Request request = new Request.Builder().url(manifestUrl).build();
                Call call = NervosHttpService.getHttpClient().newCall(request);
                String response = "";
                try {
                    response = call.execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAppItem = new Gson().fromJson(response, AppItem.class);
                return Observable.just(mAppItem);
            }
        }).filter(new Func1<AppItem, Boolean>() {
            @Override
            public Boolean call(AppItem appItem) {
                return appItem.chainset.size() > 0 && appItem.chainset.size() <= 5;
            }
        }).flatMap(new Func1<AppItem, Observable<ChainItem>>() {
            @Override
            public Observable<ChainItem> call(AppItem appItem) {
                Map<String, String> chainSet = appItem.chainset;
                List<ChainItem> chainItemList = new ArrayList<>();
                for(Map.Entry<String, String> entry : chainSet.entrySet()) {
                    ChainItem item = new ChainItem();
                    item.chainId = Integer.parseInt(entry.getKey());
                    item.httpProvider = entry.getValue();
                    chainItemList.add(item);
                    SharePrefUtil.putChainIdAndHost(entry.getKey(), entry.getValue());
                }
                return Observable.from(chainItemList);
            }
        }).flatMap(new Func1<ChainItem, Observable<ChainItem>>() {
            @Override
            public Observable<ChainItem> call(ChainItem chainItem) {
                NervosRpcService.init(webView.getContext(), chainItem.httpProvider);
                EthMetaData.EthMetaDataResult ethMetaData =
                        NervosRpcService.getMetaData().getEthMetaDataResult();
                if (ethMetaData != null) {
                    chainItem.name = ethMetaData.chainName;
                    chainItem.tokenAvatar = ethMetaData.tokenAvatar;
                    chainItem.tokenSymbol = ethMetaData.tokenSymbol;
                    chainItem.tokenName = ethMetaData.tokenName;
                } else {
                    chainItem.errorMessage = webView.getContext().getString(R.string.meta_data_error)
                            + chainItem.httpProvider;
                }
                return Observable.just(chainItem);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Subscriber<ChainItem>() {
            @Override
            public void onCompleted() { }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
            @Override
            public void onNext(ChainItem chainItem) {
                if (TextUtils.isEmpty(chainItem.errorMessage)) {
                    DBChainUtil.saveChain(webView.getContext(), chainItem);
                    if (!TextUtils.isEmpty(chainItem.tokenName)) {
                        TokenItem tokenItem = new TokenItem(chainItem);
                        DBWalletUtil.addTokenToAllWallet(webView.getContext(), tokenItem);
                    }
                } else {
                    Toast.makeText(webView.getContext(), chainItem.errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static boolean isCollectApp(WebView webView) {
        if (mAppItem != null && !TextUtils.isEmpty(mAppItem.entry)) {
            return DBAppUtil.findApp(webView.getContext(), mAppItem.entry);
        } else {
            return DBAppUtil.findApp(webView.getContext(), webView.getUrl());
        }
    }

    public static void collectApp(WebView webView) {
        AppItem appItem;
        if (mAppItem != null && !TextUtils.isEmpty(mAppItem.entry)) {
            appItem = new AppItem(mAppItem.entry,
                    mAppItem.icon, mAppItem.name, mAppItem.provider);
        } else {
            String icon = webView.getUrl() + WEB_ICON_PATH;
            appItem = new AppItem(webView.getUrl(), icon, webView.getTitle(), webView.getUrl());
        }
        DBAppUtil.saveDbApp(webView.getContext(), appItem);
        EventBus.getDefault().post(new AppCollectEvent(true, appItem));
        Toast.makeText(webView.getContext(), R.string.collect_success, Toast.LENGTH_SHORT).show();
    }

    public static void cancelCollectApp(WebView webView) {
        AppItem appItem;
        if (mAppItem != null && !TextUtils.isEmpty(mAppItem.entry)) {
            appItem = new AppItem(mAppItem.entry,
                    mAppItem.icon, mAppItem.name, mAppItem.provider);
        } else {
            String icon = webView.getUrl() + WEB_ICON_PATH;
            appItem = new AppItem(webView.getUrl(), icon, webView.getTitle(), webView.getUrl());
        }
        DBAppUtil.deleteApp(webView.getContext(), appItem.entry);
        EventBus.getDefault().post(new AppCollectEvent(false, appItem));
        Toast.makeText(webView.getContext(), R.string.cancel_collect, Toast.LENGTH_SHORT).show();
    }

    public static AppItem getAppItem() {
        return mAppItem;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void initWebSettings(WebSettings webSettings) {
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setBuiltInZoomControls(false);

        WebView.setWebContentsDebuggingEnabled(true);
    }

    public static void initWebViewCache(Context context, WebSettings webSettings) {
        String cacheDirPath = context.getFilesDir().getAbsolutePath()+"cache/";
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);
    }




}
