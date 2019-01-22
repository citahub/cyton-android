package com.cryptape.cita_wallet.util;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BaojunCZ on 2018/8/29.
 */
public class KeyStoreUtil {
    public static boolean isKeyStore(String msg) {
        try {
            JSONObject object = new JSONObject(msg);
            if (TextUtils.isEmpty(object.getString("address"))) return false;
            if (TextUtils.isEmpty(object.getString("id"))) return false;
            if (TextUtils.isEmpty(object.getString("version"))) return false;
            JSONObject crypto = object.getJSONObject("crypto");
            if (TextUtils.isEmpty(crypto.getString("cipher"))) return false;
            if (TextUtils.isEmpty(crypto.getString("ciphertext"))) return false;
            if (TextUtils.isEmpty(crypto.getString("kdf"))) return false;
            if (TextUtils.isEmpty(crypto.getString("mac"))) return false;
            JSONObject cipherparams = crypto.getJSONObject("cipherparams");
            if (TextUtils.isEmpty(cipherparams.getString("iv"))) return false;
            JSONObject kdfparams = crypto.getJSONObject("kdfparams");
            if (TextUtils.isEmpty(kdfparams.getString("dklen"))) return false;
            if (TextUtils.isEmpty(kdfparams.getString("n"))) return false;
            if (TextUtils.isEmpty(kdfparams.getString("p"))) return false;
            if (TextUtils.isEmpty(kdfparams.getString("r"))) return false;
            if (TextUtils.isEmpty(kdfparams.getString("salt"))) return false;
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
