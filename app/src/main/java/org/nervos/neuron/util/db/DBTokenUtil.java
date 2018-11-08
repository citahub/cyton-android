package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.TokenItem;

import java.util.ArrayList;
import java.util.List;

public class DBTokenUtil extends DBUtil {

    private static final String DB_TOKEN = "db_token";

    public static void saveToken(Context context, TokenItem tokenItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_TOKEN);
                db.put(getDbKey(tokenItem.name), tokenItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static boolean checkTokenExist(Context context, TokenItem tokenItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_TOKEN);
                String[] keys = db.findKeys(getDbKey(tokenItem.name));
                db.close();
                return keys.length > 0;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return false;
        }
    }

    public static List<TokenItem> getAllTokens(Context context) {
        synchronized (dbObject) {
            List<TokenItem> tokenList = new ArrayList<>();
            try {
                db = openDB(context, DB_TOKEN);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    tokenList.add(db.getObject(key, TokenItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return tokenList;
        }
    }


}
