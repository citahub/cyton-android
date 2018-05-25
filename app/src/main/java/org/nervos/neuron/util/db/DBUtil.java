package org.nervos.neuron.util.db;

public class DBUtil {

    protected static final String DB_PREFIX = "neuron-";

    protected static String getDbKey(String origin) {
        return DB_PREFIX + origin;
    }

    protected static String getDbOrigin(String key) {
        return key.substring(DB_PREFIX.length());
    }

}
