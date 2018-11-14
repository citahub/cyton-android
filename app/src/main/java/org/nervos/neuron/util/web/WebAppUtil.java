package org.nervos.neuron.util.web;

import android.annotation.SuppressLint;
import android.content.Context;
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
import org.nervos.appchain.protocol.core.methods.response.AppMetaData;
import org.nervos.neuron.BuildConfig;
import org.nervos.neuron.R;
import org.nervos.neuron.event.AppCollectEvent;
import org.nervos.neuron.event.AppHistoryEvent;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.service.http.HttpUrls;
import org.nervos.neuron.service.http.HttpService;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.util.NetworkUtil;
import org.nervos.neuron.util.db.DBAppUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WebAppUtil {

    private static final String WEB_ICON_PATH = "favicon.ico";
    private static final String MANIFEST = "manifest";
    private static AppItem mAppItem = null;

    public static void init() {
        mAppItem = null;
    }

    /**
     * get app information from manifest.json and getMetaData from each http provider host
     *
     * @param webView
     * @param url     the web url from the third part
     */
    public static Observable<ChainItem> getHttpManifest(WebView webView, String url) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws IOException {
                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.getElementsByTag("link");
                for (Element element : elements) {
                    if (MANIFEST.equals(element.attr("rel"))) {
                        return element.attr("href");
                    }
                }
                return "";
            }
        })
                .filter(path -> !TextUtils.isEmpty(path))
                .flatMap((Func1<String, Observable<AppItem>>) path -> {
                    URI uri = URI.create(url);
                    String manifestUrl = path;
                    if (!path.startsWith("http")) {
                        manifestUrl = uri.getAuthority() + "/";
                        manifestUrl += path.startsWith(".") ? uri.getPath() + "/" + path.substring(1) : path;
                        manifestUrl = uri.getScheme() + "://" + formatUrl(manifestUrl);
                    }

                    Request request = new Request.Builder().url(manifestUrl).build();
                    Call call = HttpService.getHttpClient().newCall(request);
                    String response = "";
                    ResponseBody body = null;
                    try {
                        final Response resp = call.execute();
                        final int code = resp.code();
                        body = resp.body();
                        if (code == 200) {
                            response = body.string();
                        }
                        if (body != null) {
                            body.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Observable.error(new Throwable(e.getMessage()));
                    } finally {
                        if (body != null) {
                            body.close();
                        }
                    }
                    mAppItem = new Gson().fromJson(response, AppItem.class);
                    return Observable.just(mAppItem);
                })
                .filter(appItem -> appItem.chainSet.size() > 0 && appItem.chainSet.size() <= 5)
                .flatMap((Func1<AppItem, Observable<ChainItem>>) appItem -> {
                    Map<String, String> chainSet = appItem.chainSet;
                    List<ChainItem> chainItemList = new ArrayList<>();
                    if (chainSet.size() == 0) {
                        return Observable.error(new Throwable("Manifest chain set is null, please provide chain id and host"));
                    }
                    for (Map.Entry<String, String> entry : chainSet.entrySet()) {
                        ChainItem item = new ChainItem();
                        item.chainId = Integer.parseInt(entry.getKey());
                        item.httpProvider = entry.getValue();
                        chainItemList.add(item);
                        SharePrefUtil.putChainIdAndHost(entry.getKey(), entry.getValue());
                    }
                    return Observable.from(chainItemList);
                })
                .flatMap(new Func1<ChainItem, Observable<ChainItem>>() {
                    @Override
                    public Observable<ChainItem> call(ChainItem chainItem) {
                        AppChainRpcService.init(webView.getContext(), chainItem.httpProvider);
                        AppMetaData.AppMetaDataResult ethMetaData = AppChainRpcService.getMetaData().getAppMetaDataResult();
                        if (ethMetaData != null) {
                            chainItem.name = ethMetaData.chainName;
                            chainItem.tokenAvatar = ethMetaData.tokenAvatar;
                            chainItem.tokenSymbol = ethMetaData.tokenSymbol;
                            chainItem.tokenName = ethMetaData.tokenName;
                        } else {
                            chainItem.errorMessage = webView.getContext().getString(R.string.meta_data_error) + chainItem.httpProvider;
                        }
                        return Observable.just(chainItem);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static String formatUrl(String url) {
        if (url.contains("///")) {
            url = url.replace("///", "/");
        }
        if (url.contains("//")) {
            url = url.replace("//", "/");
        }
        return url;
    }


    public static boolean isCollectApp(WebView webView) {
        return DBAppUtil.findApp(webView.getContext(), mAppItem.entry);
    }

    public static void collectApp(WebView webView) {
        mAppItem.collectTime = System.currentTimeMillis();
        DBAppUtil.saveDbApp(webView.getContext(), mAppItem);
        Toast.makeText(webView.getContext(), R.string.collect_success, Toast.LENGTH_SHORT).show();
    }

    public static void cancelCollectApp(WebView webView) {
        DBAppUtil.deleteApp(webView.getContext(), mAppItem.entry);
        Toast.makeText(webView.getContext(), R.string.cancel_collect, Toast.LENGTH_SHORT).show();
    }

    public static void addHistory() {
        if (WebAppUtil.getAppItem() == null) return;
        EventBus.getDefault().post(new AppHistoryEvent(WebAppUtil.getAppItem(), System.currentTimeMillis()));
    }

    public static void setAppItem(WebView webView) {
        if (mAppItem != null && mAppItem.chainSet != null && mAppItem.chainSet.size() > 0) return;

        try {
            URI uri = URI.create(webView.getUrl());
            String icon = uri.getAuthority() + "/" + uri.getPath() + "/" + WEB_ICON_PATH;
            icon = uri.getScheme() + "://" + formatUrl(icon);
            icon = UrlUtil.exists(icon) ? icon : HttpUrls.DEFAULT_WEB_IMAGE_URL;
            mAppItem = new AppItem(webView.getUrl(), icon, webView.getTitle(), webView.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static AppItem getAppItem() {
        return mAppItem;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void initWebSettings(WebSettings webSettings) {
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setBuiltInZoomControls(false);

        webSettings.setUserAgentString(
                webSettings.getUserAgentString() + "Neuron(Platform=Android&AppVersion=" + BuildConfig.VERSION_NAME + ")");

        WebView.setWebContentsDebuggingEnabled(BuildConfig.IS_DEBUG);
    }

    public static void initWebViewCache(Context context, WebSettings webSettings) {
        if (NetworkUtil.isNetworkAvailable(context)) {
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        String cacheDirPath = context.getFilesDir().getAbsolutePath() + "cache/";
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);
    }


}
