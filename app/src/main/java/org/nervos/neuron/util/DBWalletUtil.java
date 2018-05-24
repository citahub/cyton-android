package org.nervos.neuron.util;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBWalletUtil {

    private static final String DB_WALLET_PREFIX = "neuron-";
    private static final String DB_WALLET = "db_wallet";
    private static final String DB_TOKEN = "db_token";
    private static final String DB_CHAIN = "db_chain";
    private static final String DB_TRANSATION = "db_transaction";

    public static WalletItem getWallet(Context context, String walletName) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbWalletKey(walletName), WalletItem.class);
            db.close();
            return walletItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WalletItem getCurrentWallet(Context context) {
        return getWallet(context, SharePrefUtil.getWalletName());
    }

    public static List<String> getAllWalletName(Context context) {
        List<String> walletList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            String[] keys = db.findKeys(DB_WALLET_PREFIX);
            db.close();
            for(String key: keys) {
                walletList.add(key.substring(DB_WALLET_PREFIX.length()));
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return walletList;
    }

    public static void saveWallet(Context context, WalletItem walletItem){
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.put(getDbWalletKey(walletItem.name), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void updateWalletPassword(Context context, String name, String password) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbWalletKey(name), WalletItem.class);
            walletItem.password = password;
            db.put(getDbWalletKey(name), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void updateWalletName(Context context, String name, String newName) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            WalletItem walletItem = db.getObject(getDbWalletKey(name), WalletItem.class);
            db.del(getDbWalletKey(name));
            walletItem.name = newName;
            db.put(getDbWalletKey(newName), walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWallet(Context context, String name) {
        try {
            DB db = DBFactory.open(context, DB_WALLET);
            db.del(getDbWalletKey(name));
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private static String getDbWalletKey(String name) {
        return DB_WALLET_PREFIX + name;
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
