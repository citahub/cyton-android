package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

public class DBHistoryUtil extends DBUtil {

    private static final String DB_HISTORY = "db_history";

    public static List<String> getAllHistory(Context context) {
        List<String> historyList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_HISTORY);
            String[] keys = db.findKeys(DB_PREFIX);
            for(String key: keys) {
                historyList.add(db.get(key));
            }
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return historyList;
    }


    public static void saveHistory(Context context, String url){
        try {
            DB db = DBFactory.open(context, DB_HISTORY);
            db.put(getDbKey(url), url);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

}
