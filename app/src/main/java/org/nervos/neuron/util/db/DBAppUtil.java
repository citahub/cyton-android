package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.AppItem;

import java.util.ArrayList;
import java.util.List;

public class DBAppUtil extends DBUtil {

    private static final String DB_APP = "db_app";

    public static void saveDbApp(Context context, AppItem appItem) {
        try {
            DB db = DBFactory.open(context, DB_APP);
            db.put(getDbKey(appItem.entry), appItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void deleteApp(Context context, String entry) {
        try {
            DB db = DBFactory.open(context, DB_APP);
            db.del(getDbKey(entry));
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static List<AppItem> getAllApp(Context context) {
        List<AppItem> walletList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_APP);
            String[] keys = db.findKeys(DB_PREFIX);
            db.close();
            for(String key: keys) {
                walletList.add(db.getObject(key, AppItem.class));
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return walletList;
    }

    public static boolean findApp(Context context, String entry) {
        try {
            DB db = DBFactory.open(context, DB_APP);
            String[] keys = db.findKeys(getDbKey(entry));
            db.close();
            return keys.length > 0;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static AppItem getApp(Context context, String entry) {
        try {
            DB db = DBFactory.open(context, DB_APP);
            AppItem appItem = db.getObject(getDbKey(entry), AppItem.class);
            db.close();
            return appItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }


}
