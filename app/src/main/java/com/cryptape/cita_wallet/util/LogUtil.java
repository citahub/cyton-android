package com.cryptape.cita_wallet.util;

import android.text.TextUtils;
import android.util.Log;

import com.cryptape.cita_wallet.BuildConfig;

public class LogUtil {

    private static final String TAG = "Cyton";

    public static void v(String tag, String msg) {
        if (BuildConfig.IS_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.v(tag, msg);
        }
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.IS_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.IS_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.IS_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.IS_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

}
