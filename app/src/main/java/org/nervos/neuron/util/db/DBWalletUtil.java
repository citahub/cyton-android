package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBWalletUtil extends DBUtil {

    private static final String DB_WALLET = "db_wallet";

    public static WalletItem getWallet(Context context, String walletName) {
        if (TextUtils.isEmpty(walletName)) return null;
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(walletName), WalletItem.class);
            db.close();
            return walletItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static WalletItem getCurrentWallet(Context context) {
        return getWallet(context, SharePrefUtil.getCurrentWalletName());
    }

    public static List<String> getAllWalletName(Context context) {
        List<String> walletList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            String[] keys = db.findKeys(DB_PREFIX);
            db.close();
            for(String key: keys) {
                walletList.add(getDbOrigin(key));
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return walletList;
    }

    public static void saveWallet(Context context, WalletItem walletItem){
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.put(getDbKey(walletItem.name), walletItem);
            db.close();
            SharePrefUtil.putCurrentWalletName(walletItem.name);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void updateWalletPassword(Context context, String name, String password) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
            walletItem.password = password;
            db.put(getDbKey(name), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void updateWalletName(Context context, String name, String newName) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbKey(name), WalletItem.class);
            db.del(getDbKey(name));
            walletItem.name = newName;
            db.put(getDbKey(newName), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkWalletName(Context context, String name) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            boolean isKeyExist = db.exists(getDbKey(name));
            db.close();
            return isKeyExist;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkWalletAddress(Context context, String address) {
        boolean isKeyExist = false;
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            List<String> names = getAllWalletName(context);
            for(String name: names) {
                WalletItem walletItem = getWallet(context, name);
                isKeyExist = (walletItem != null && walletItem.address.equals(address));
            }
            db.close();
            return isKeyExist;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return isKeyExist;
    }

    public static void deleteWallet(Context context, String name) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.del(getDbKey(name));
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void addTokenToWallet(Context context, String walletName, TokenItem tokenItem){
        WalletItem walletItem = getWallet(context, walletName);
        if (walletItem != null) {
            if (walletItem.tokenItems == null) {
                walletItem.tokenItems = new ArrayList<>();
            }
            walletItem.tokenItems.add(tokenItem);
            saveWallet(context, walletItem);
        }
    }

    public static List<TokenItem> getAllTokenFromWallet(Context context, String walletName) {
        return Objects.requireNonNull(getWallet(context, walletName)).tokenItems;
    }

}
