package org.nervos.neuron.util.db;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefUtil {

    private static final String FILE_NAME = "shared_name";
    private static final String CHAIN_SHARE = "chain_share";

    private static final String WALLET_NAME = "wallet_name";
    private static final String KEY_FIRST = "key_first";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences chainSharePreference;

    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        chainSharePreference = context.getSharedPreferences(CHAIN_SHARE, Context.MODE_PRIVATE);
    }

    public static boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, true);
    }

    public static void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static boolean getFirstIn() {
        return sharedPreferences.getBoolean(KEY_FIRST, true);
    }

    public static void putFirstIn(boolean value) {
        sharedPreferences.edit().putBoolean(KEY_FIRST, value).apply();
    }

    public static void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public static String getString(String key, String defaultStr) {
        return sharedPreferences.getString(key, defaultStr);
    }

    public static void putCurrentWalletName(String name) {
        putString(WALLET_NAME, name);
    }

    public static String getCurrentWalletName() {
        return getString(WALLET_NAME);
    }

    public static void deleteWalletName() {
        putCurrentWalletName(null);
    }

    public static void putChainIdAndHost(String chainId, String chainHost) {
        chainSharePreference.edit().putString(chainId, chainHost).apply();
    }

    public static String getChainHostFromId(String chainId) {
        return chainSharePreference.getString(chainId, null);
    }

    public static String getChainHostFromId(long chainId) {
        return getChainHostFromId(String.valueOf(chainId));
    }
}
