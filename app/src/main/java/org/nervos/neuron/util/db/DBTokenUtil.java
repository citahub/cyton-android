package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.TokenItem;

public class DBTokenUtil extends DBUtil {

    private static final String DB_TOKEN = "db_token";

    public static void saveToken(Context context, TokenItem tokenItem){
        try {
            DB db = DBFactory.open(context, DB_TOKEN);
            db.put(getDbKey(String.valueOf(tokenItem.name)), tokenItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }


}
