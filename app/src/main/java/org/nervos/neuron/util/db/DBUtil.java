package org.nervos.neuron.util.db;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

public class DBUtil {

    static final String DB_PREFIX = "neuron-";

    static final Kryo kryo = new Kryo();

    static {
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
    }

    static String getDbKey(String origin) {
        return DB_PREFIX + origin;
    }

    static String getDbOrigin(String key) {
        return key.substring(DB_PREFIX.length());
    }

}
