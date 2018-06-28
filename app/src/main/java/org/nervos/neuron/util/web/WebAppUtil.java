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
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.db.DBAppUtil;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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
    private static ChainItem mChainItem;

    /**
     * get manifest.json from manifest path which contains host and path
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
        }).flatMap(new Func1<String, Observable<ChainItem>>() {
            @Override
            public Observable<ChainItem> call(String path) {
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
                return Observable.just(new Gson().fromJson(response, ChainItem.class));
            }
        }).flatMap(new Func1<ChainItem, Observable<EthMetaData.EthMetaDataResult>>() {
            @Override
            public Observable<EthMetaData.EthMetaDataResult> call(ChainItem chainItem) {
                mChainItem = chainItem;
                NervosRpcService.init(webView.getContext(), mChainItem.httpProvider);
                return Observable.just(NervosRpcService.getMetaData().getEthMetaDataResult());
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<EthMetaData.EthMetaDataResult>() {
                @Override
                public void onCompleted() { }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }
                @Override
                public void onNext(EthMetaData.EthMetaDataResult ethMetaData) {
                    if (ethMetaData != null && ethMetaData.chainId == mChainItem.chainId) {
                        mChainItem.tokenName = ethMetaData.tokenName;
                        mChainItem.tokenSymbol = ethMetaData.tokenSymbol;
                        mChainItem.tokenAvatar = ethMetaData.tokenAvatar;
                        DBChainUtil.saveChain(webView.getContext(), mChainItem);
                        if (!TextUtils.isEmpty(mChainItem.tokenName)) {
                            TokenItem tokenItem = new TokenItem(mChainItem);
                            DBWalletUtil.addTokenToAllWallet(webView.getContext(), tokenItem);
                        }
                    }
                }
            });
    }


    public static boolean isCollectApp(WebView webView) {
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            return DBAppUtil.findApp(webView.getContext(), mChainItem.entry);
        } else {
            return DBAppUtil.findApp(webView.getContext(), webView.getUrl());
        }
    }

    public static void collectApp(WebView webView) {
        AppItem appItem;
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            appItem = new AppItem(mChainItem.entry,
                    mChainItem.icon, mChainItem.name, mChainItem.provider);
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
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            appItem = new AppItem(mChainItem.entry,
                    mChainItem.icon, mChainItem.name, mChainItem.provider);
        } else {
            String icon = webView.getUrl() + WEB_ICON_PATH;
            appItem = new AppItem(webView.getUrl(), icon, webView.getTitle(), webView.getUrl());
        }
        DBAppUtil.deleteApp(webView.getContext(), appItem.entry);
        EventBus.getDefault().post(new AppCollectEvent(false, appItem));
        Toast.makeText(webView.getContext(), R.string.cancel_collect, Toast.LENGTH_SHORT).show();
    }

    public static ChainItem getChainItem() {
        return mChainItem;
    }

    /**
     * read String content of rejected JavaScript file from assets
     * @param fileName    the name of JavaScript file
     * @return  the content of JavaScript file
     */
    public static String getFileFromAsset(Context context, String fileName) {
        AssetManager am = context.getAssets();
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

    public static String getInjectNervosWeb3(Context context) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return "javascript: if (typeof web3 !== 'undefined') { web3 = NervosWeb3(web3.currentProvider) } else { web3 = NervosWeb3('" + ConstantUtil.NERVOS_NODE_URL + "'))}; web3.defaultAccount.address='" + walletItem.address + "'";
    }

    public static String getInjectTransactionJs() {
        return "javascript: web3.eth.sendTransaction = function(tx) {appHybrid.sendTransaction(JSON.stringify(tx))}; web3.eth.signTransaction = function(tx) {appHybrid.signTransaction(JSON.stringify(tx))};";
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


    private final  static String HTTP = "http://";
    private final  static  String HTTPS =  "https://";
    public static String addPrefixUrl(String url) {
        if (url.contains(HTTP) || url.contains(HTTPS)) {
            return url;
        } else if (exists(HTTPS + url)) {
            return (HTTPS + url);
        } else if (exists(HTTP + url)) {
            return (HTTP + url);
        }
        return url;
    }

    private static boolean exists(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(3000);
            return (con.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

}
