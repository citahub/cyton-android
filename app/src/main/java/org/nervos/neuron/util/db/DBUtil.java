package org.nervos.neuron.util.db;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class DBUtil {

    static final String DB_PREFIX = "neuron-";

    static DB db = null;

    static final Object dbObject = new Object();

    static final Kryo kryo = new Kryo();

    static {
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
    }

    static DB openDB(Context context, String dbName) throws SnappydbException {
        return DBFactory.open(context, dbName, kryo);
    }

    static String getDbKey(String origin) {
        return DB_PREFIX + origin;
    }

    static String getDbOrigin(String key) {
        return key.substring(DB_PREFIX.length());
    }

    static void handleException(DB db, SnappydbException e) {
        try {
            if (db != null && db.isOpen()) db.close();
        } catch (SnappydbException e1) {
            e1.printStackTrace();
        }
        e.printStackTrace();

    }

}
