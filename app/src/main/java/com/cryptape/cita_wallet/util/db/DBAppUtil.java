package com.cryptape.cita_wallet.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import com.cryptape.cita_wallet.item.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/25
 */
public class DBAppUtil extends DBUtil {

    private static final String DB_APP = "db_app";

    public static void saveDbApp(Context context, App appItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APP);
                db.put(getDbKey(appItem.entry), appItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void deleteApp(Context context, String entry) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APP);
                db.del(getDbKey(entry));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static List<App> getAllApp(Context context) {
        synchronized (dbObject) {
            List<App> appList = new ArrayList<>();
            try {
                db = openDB(context, DB_APP);
                String[] keys = db.findKeys(DB_PREFIX);
                for(String key: keys) {
                    appList.add(db.getObject(key, App.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return appList;
        }
    }

    public static boolean findApp(Context context, String entry) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APP);
                String[] keys = db.findKeys(getDbKey(entry));
                db.close();
                return keys.length > 0;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return false;
        }
    }


    public static App getApp(Context context, String entry) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APP);
                App appItem = db.getObject(getDbKey(entry), App.class);
                db.close();
                return appItem;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return null;
        }
    }


}
