package org.nervos.neuron.util.db;

import android.content.Context;
import android.content.SharedPreferences;

import org.nervos.neuron.util.LogUtil;

public class SharePrefUtil {

    private static final String FILE_NAME = "shared_name";
    private static final String WALLET_NAME = "wallet_name";
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public static void putCurrentWalletName(String name){
        putString(WALLET_NAME, name);
    }

    public static String getCurrentWalletName() {
        return getString(WALLET_NAME);
    }

    public static void deleteWalletName() {
        putCurrentWalletName(null);
    }


}
