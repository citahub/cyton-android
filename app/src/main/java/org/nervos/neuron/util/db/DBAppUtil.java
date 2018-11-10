package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.AppItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/25
 */
public class DBAppUtil extends DBUtil {

    private static final String DB_APP = "db_app";

    public static void saveDbApp(Context context, AppItem appItem) {
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

    public static List<AppItem> getAllApp(Context context) {
        synchronized (dbObject) {
            List<AppItem> walletList = new ArrayList<>();
            try {
                db = openDB(context, DB_APP);
                String[] keys = db.findKeys(DB_PREFIX);
                db.close();
                for (String key : keys) {
                    walletList.add(db.getObject(key, AppItem.class));
                }
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return walletList;
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


    public static AppItem getApp(Context context, String entry) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APP);
                AppItem appItem = db.getObject(getDbKey(entry), AppItem.class);
                db.close();
                return appItem;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return null;
        }
    }


}
