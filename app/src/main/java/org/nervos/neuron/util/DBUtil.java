package org.nervos.neuron.util;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.WalletItem;

public class DBUtil {

    private static final String CURRENT_WALLET = "current_wallet";

    public static void setCurrentWallet(DB db, WalletItem wallet) {
        try {
            db.put(CURRENT_WALLET, wallet);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static WalletItem getCurrentWallet(Context context) {
        try {
            DB db = DBFactory.open(context);
            WalletItem walletItem = db.getObject(CURRENT_WALLET, WalletItem.class);
            db.close();
            return walletItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WalletItem getCurrentWallet(DB db) {
        try {
            return db.getObject(CURRENT_WALLET, WalletItem.class);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveWallet(Context context, WalletItem walletItem){
        try {
            DB db = DBFactory.open(context);
            db.put(walletItem.name, walletItem);
            setCurrentWallet(db, walletItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

}
