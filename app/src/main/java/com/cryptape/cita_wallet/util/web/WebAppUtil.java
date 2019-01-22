package com.cryptape.cita_wallet.util.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.cryptape.cita.protocol.core.methods.response.AppMetaData;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.cryptape.cita_wallet.BuildConfig;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.url.HttpUrls;
import com.cryptape.cita_wallet.event.AppHistoryEvent;
import com.cryptape.cita_wallet.item.App;
import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.service.http.CITARpcService;
import com.cryptape.cita_wallet.service.http.HttpService;
import com.cryptape.cita_wallet.util.NetworkUtil;
import com.cryptape.cita_wallet.util.db.DBAppUtil;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by duanyytop on 2018/5/24
 */
public class WebAppUtil {

    private static final String WEB_ICON_PATH = "favicon.ico";
    private static final String MANIFEST = "manifest";
    private static App mApp = null;

    public static void init() {
        mApp = null;
    }

    /**
     * get app information from manifest.json and getMetaData from each http provider host
     *
     * @param webView
     * @param url     the web url from the third part
     */
    public static Observable<Chain> getHttpManifest(WebView webView, String url) {
        return Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws IOException {
                return getManifestPath(url);
            }
        }).filter(path -> !TextUtils.isEmpty(path)).flatMap((Func1<String, Observable<App>>) path -> {
            try {
                mApp = new Gson().fromJson(getManifestResponse(handleManifestPath(url, path)), App.class);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Observable.error(throwable);
            }
            if (mApp == null || mApp.chainSet == null || mApp.chainSet.size() <= 0 || mApp.chainSet.size() > 5) {
                Observable.error(new Throwable(new Throwable("Manifest chain set is null, please provide chain id and host")));
            }
            return Observable.just(mApp);
        }).flatMap((Func1<App, Observable<Chain>>) appItem -> {
            List<Chain> chainList = new ArrayList<>();
            for (Map.Entry<String, String> entry : appItem.chainSet.entrySet()) {
                chainList.add(new Chain(entry.getKey(), mApp.name, entry.getValue()));
            }
            return Observable.from(chainList);
        }).flatMap(new Func1<Chain, Observable<Chain>>() {
            @Override
            public Observable<Chain> call(Chain chain) {
                CITARpcService.init(webView.getContext(), chain.httpProvider);
                AppMetaData.AppMetaDataResult ethMetaData = Objects.requireNonNull(CITARpcService.getMetaData()).getAppMetaDataResult();
                if (ethMetaData != null) {
                    chain.setChainId(ethMetaData.getChainIdV1());
                    chain.name = ethMetaData.getChainName();
                    chain.tokenAvatar = ethMetaData.getTokenAvatar();
                    chain.tokenSymbol = ethMetaData.getTokenSymbol();
                    chain.tokenName = ethMetaData.getTokenName();
                } else {
                    Observable.error(new Throwable(webView.getContext().getString(R.string.meta_data_error) + chain.httpProvider));
                }
                return Observable.just(chain);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static String getManifestResponse(String path) throws Throwable {
        Request request = new Request.Builder().url(path).build();
        Call call = HttpService.getHttpClient().newCall(request);
        Response resp = null;
        try {
            resp = call.execute();
            if (resp.code() == 200 && resp.body() != null) {
                return resp.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new Throwable(e.getMessage());
        } finally {
            if (resp != null && resp.body() != null) {
                resp.body().close();
            }
        }
        return null;
    }


    private static final String TAG_LINK = "link";
    private static final String TAG_REL = "rel";
    private static final String TAG_HREF = "href";

    private static String getManifestPath(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.getElementsByTag(TAG_LINK);
        for (Element element : elements) {
            if (MANIFEST.equals(element.attr(TAG_REL))) {
                return element.attr(TAG_HREF);
            }
        }
        return "";
    }


    private static final String TAG_HTTP = "http";
    private static final String TAG_DOT = ".";
    private static final String TAG_SLASH = "/";
    private static final String TAG_TWO_SLASH = "//";
    private static final String TAG_THREE_SLASH = "///";

    private static String handleManifestPath(String url, String path) {
        URI uri = URI.create(url);
        String manifestUrl = path;
        if (!path.startsWith(TAG_HTTP)) {
            manifestUrl = uri.getAuthority() + TAG_SLASH;
            if (path.startsWith(TAG_DOT)) {
                if (uri.getPath().contains(TAG_SLASH)) {
                    int index = uri.getPath().lastIndexOf(TAG_SLASH);
                    manifestUrl += uri.getPath().substring(0, index) + TAG_SLASH + path.substring(1);
                } else {
                    manifestUrl += uri.getPath() + TAG_SLASH + path.substring(1);
                }
            } else {
                manifestUrl += path;
            }
            manifestUrl = uri.getScheme() + "://" + formatUrl(manifestUrl);
        }
        return manifestUrl;
    }

    private static String formatUrl(String url) {
        if (url.contains(TAG_THREE_SLASH)) {
            url = url.replace(TAG_THREE_SLASH, TAG_SLASH);
        }
        if (url.contains(TAG_TWO_SLASH)) {
            url = url.replace(TAG_TWO_SLASH, TAG_SLASH);
        }
        return url;
    }


    public static boolean isApp(WebView webView) {
        return DBAppUtil.findApp(webView.getContext(), mApp.entry);
    }

    public static void collectApp(WebView webView) {
        mApp.timestamp = System.currentTimeMillis();
        DBAppUtil.saveDbApp(webView.getContext(), mApp);
        Toast.makeText(webView.getContext(), R.string.collect_success, Toast.LENGTH_SHORT).show();
    }

    public static void cancelApp(WebView webView) {
        DBAppUtil.deleteApp(webView.getContext(), mApp.entry);
        Toast.makeText(webView.getContext(), R.string.cancel_collect, Toast.LENGTH_SHORT).show();
    }

    public static void addHistory() {
        if (WebAppUtil.getAppItem() == null) return;
        EventBus.getDefault().post(new AppHistoryEvent(WebAppUtil.getAppItem(), System.currentTimeMillis()));
    }

    public static void setAppItem(WebView webView) {
        if (mApp != null && mApp.chainSet != null && mApp.chainSet.size() > 0) return;

        try {
            URI uri = URI.create(webView.getUrl());
            String icon = uri.getAuthority() + "/" + uri.getPath() + "/" + WEB_ICON_PATH;
            icon = uri.getScheme() + "://" + formatUrl(icon);
            icon = UrlUtil.exists(icon) ? icon : HttpUrls.DEFAULT_WEB_IMAGE_URL;
            mApp = new App(webView.getUrl(), icon, webView.getTitle(), webView.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static App getAppItem() {
        return mApp;
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
                webSettings.getUserAgentString() + "Cyton(Platform=Android&AppVersion=" + BuildConfig.VERSION_NAME + ")");

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

    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    public static void loadUrl(WebView web, String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put(ACCEPT_LANGUAGE, Locale.getDefault().getLanguage());
        web.loadUrl(url, headers);
    }


}
