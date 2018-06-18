package org.nervos.neuron.util.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nervos.neuron.event.AppCollectEvent;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.service.NervosHttpService;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBAppUtil;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WebAppUtil {

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
                        webView.loadUrl(getInjectNervosWeb3());
                        DBChainUtil.saveChain(webView.getContext(), mChainItem);
                        if (!TextUtils.isEmpty(mChainItem.tokenName)) {
                            TokenItem tokenItem = new TokenItem(mChainItem);
                            DBWalletUtil.addTokenToAllWallet(webView.getContext(), tokenItem);
                        }
                    }
                }
            });
    }


    public static boolean isCollectApp(Context context) {
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            return DBAppUtil.findApp(context, mChainItem.entry);
        }
        return false;
    }

    public static void collectApp(Context context) {
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            AppItem appItem = new AppItem(mChainItem.entry,
                    mChainItem.icon, mChainItem.name, mChainItem.provider);
            DBAppUtil.saveDbApp(context, appItem);
            EventBus.getDefault().post(new AppCollectEvent(true, appItem));
            Toast.makeText(context, "收藏成功", Toast.LENGTH_SHORT).show();
        }
    }

    public static void cancelCollectApp(Context context) {
        if (mChainItem != null && !TextUtils.isEmpty(mChainItem.entry)) {
            DBAppUtil.deleteApp(context, mChainItem.entry);
            AppItem appItem = new AppItem(mChainItem.entry,
                    mChainItem.icon, mChainItem.name, mChainItem.provider);
            EventBus.getDefault().post(new AppCollectEvent(false, appItem));
            Toast.makeText(context, "取消收藏", Toast.LENGTH_SHORT).show();
        }
    }

    public static ChainItem getChainItem() {
        return mChainItem;
    }

    /**
     * read String content of rejected JavaScript file from assets
     * @param fileName    the name of JavaScript file
     * @return  the content of JavaScript file
     */
    private static String getInjectedJsFile(Context context, String fileName) {
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

    public static String getInjectEthWeb3(Context context) {
        WalletItem walletItem = DBWalletUtil.getCurrentWallet(context);
        return "javascript: web3.setProvider(new Web3.providers.HttpProvider('"
                + EthRpcService.ETH_NODE_IP + "')); web3.currentProvider.isMetaMask = true; web3.eth.defaultAccount = '"
                + walletItem.address + "'";
    }

    private static String getInjectNervosWeb3() {
        return "javascript: web3.setProvider('" + mChainItem.httpProvider + "'); web3.currentProvider.isMetaMask = true;";
    }

    public static String getInjectTransactionJs() {
        return "javascript: web3.eth.sendTransaction = function(tx) {appHybrid.showTransaction(JSON.stringify(tx));console.log(JSON.stringify(tx));}";
    }

    public static String getInjectSignJs() {
        return "javascript: web3.eth.signTransaction = function(tx) {appHybrid.signTransaction(JSON.stringify(tx));console.log(JSON.stringify(tx));}";
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


    public static boolean isHttpUrl(String urlString) {
        URI uri = null;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        if(uri.getHost() == null){
            return false;
        }
        if(uri.getScheme().equalsIgnoreCase("http")
                || uri.getScheme().equalsIgnoreCase("https")){
            return true;
        }
        return false;
    }

}
