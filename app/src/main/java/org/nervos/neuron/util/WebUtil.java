package org.nervos.neuron.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.service.CitaRpcService;
import org.nervos.neuron.service.response.ManifestResponse;
import org.nervos.web3j.protocol.core.methods.response.EthMetaData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebUtil {

    /**
     * get manifest path from html link tag
     * @param context
     * @param url    the web url from the third part
     */
    public static void getHtmlManifest(Context context, String url) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements elements = doc.getElementsByTag("link");
                    for(Element element: elements) {
                        if ("manifest".equals(element.attr("ref"))) {
                            getHttpManifest(context, url, element.attr("href"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * get manifest.json from manifest path which contains host and path
     * @param context
     * @param url           the web url from the third part
     * @param path          manifest path
     */
    private static void getHttpManifest(Context context, String url, String path) {
        URI uri = URI.create(url);
        String manifestUrl = uri.getScheme() + "://" + uri.getAuthority() + path;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(manifestUrl)
                .method("GET",null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ManifestResponse manifestResponse =
                            new Gson().fromJson(response.body().string(), ManifestResponse.class);
                    if (!TextUtils.isEmpty(manifestResponse.chainId)
                            && !TextUtils.isEmpty(manifestResponse.httpProvider)) {
                        getMetaData(context, manifestResponse.httpProvider);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * get metadata from cita blockchain
     * @param context
     * @param httpProvider
     */
    private static void getMetaData(Context context, String httpProvider) {
        CitaRpcService.init(httpProvider);
        EthMetaData ethMetaData = CitaRpcService.getMetaData();
        if (ethMetaData != null) {
            ChainItem chainItem = new ChainItem(String.valueOf(ethMetaData.chainId),
                    ethMetaData.chainName, httpProvider);
            DBChainUtil.saveChain(context, chainItem);
        }
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

    public static String getWeb3Js(Context context) {
        return WebUtil.getInjectedJsFile(context, "web3.js");
    }

    public static String getHttpProviderJs(Context context) {
        return WebUtil.getInjectedJsFile(context, "httpprovider.js");
    }

    public static String  getInjectJs() {
        return "javascript: var web3 = new Web3(); web3.initWeb3(); ";
    }


    @SuppressLint("SetJavaScriptEnabled")
    public static void initWebSettings(WebSettings webSettings) {
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(true);
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

}
