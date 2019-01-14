package com.cryptape.cita_wallet.util.web;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by duanyytop on 2018/7/13
 */
public class UrlUtil {

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
        return null;
    }

    public static boolean exists(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(3000);
            return (con.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

}
