package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.CITATransactionDBItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class DBCITATransactionsUtil extends DBUtil {
    private static final String DB_CITA = "db_cita_transaction";
    private static final String DB_PRE_PENDING = DB_PREFIX + "cita-pending-";
    private static final String DB_PRE_FAILED = DB_PREFIX + "cita-failed-";
    private static final String DB_PRE_NATIVE = "native-";


    public static void save(Context context, boolean pending, CITATransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String pendingKey = pending ? DB_PRE_PENDING : DB_PRE_FAILED;
                if (item.isNativeToken) {
                    db.put(pendingKey + DB_PRE_NATIVE + item.chain + "-" + item.hash, item);
                } else {
                    db.put(pendingKey + item.contractAddress + item.chain + "-" + "-" + item.hash, item);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void deletePending(Context context, CITATransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                if (item.isNativeToken) {
                    db.del(DB_PRE_PENDING + DB_PRE_NATIVE + item.chain + "-" + item.hash);
                } else {
                    db.del(DB_PRE_PENDING + item.contractAddress + "-" + item.chain + "-" + item.hash);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void failed(Context context, CITATransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String key;
                if (item.isNativeToken) {
                    key = DB_PRE_PENDING + DB_PRE_NATIVE + item.chain + "-" + item.hash;
                } else {
                    key = DB_PRE_PENDING + item.contractAddress + item.chain + "-" + "-" + item.hash;
                }
                if (db.exists(key)) {
                    db.del(key);
                }
                if (item.isNativeToken) {
                    key = DB_PRE_FAILED + DB_PRE_NATIVE + item.chain + "-" + item.hash;
                } else {
                    key = DB_PRE_FAILED + item.contractAddress + "-" + item.chain + "-" + item.hash;
                }
                db.put(key, item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    /**
     * get all pending transaction (native & token)
     *
     * @param context
     * @param pending true:pending false: failed
     * @param type    0:all 1:native 2:token
     * @return
     */
    public static List<CITATransactionDBItem> getAllPending(Context context, boolean pending, int type, String contractAddress) {
        synchronized (dbObject) {
            List<CITATransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String pendingKey = pending ? DB_PRE_PENDING : DB_PRE_FAILED;
                String query;
                switch (type) {
                    case 0:
                    default:
                        query = pendingKey;
                        break;
                    case 1:
                        query = pendingKey + DB_PRE_NATIVE;
                        break;
                    case 2:
                        query = pendingKey + contractAddress;
                        break;
                }
                String[] keys = db.findKeys(query);
                for (String key : keys) {
                    list.add(db.getObject(key, CITATransactionDBItem.class));
                }
                db.close();
                Collections.sort(list, (o1, o2) -> (o2.timestamp - o1.timestamp));
                return list;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

    /**
     * get all pending transaction (native & token)
     *
     * @param context
     * @param chain   chain ip
     * @param pending true:pending false: failed
     * @param type    0:all 1:native 2:token
     * @return
     */
    public static List<CITATransactionDBItem> getChainPending(Context context, String chain, boolean pending, int type, String contractAddress) {
        synchronized (dbObject) {
            List<CITATransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String pendingKey = pending ? DB_PRE_PENDING : DB_PRE_FAILED;
                String query;
                switch (type) {
                    case 0:
                    default:
                        query = pendingKey;
                        break;
                    case 1:
                        query = pendingKey + DB_PRE_NATIVE + chain;
                        break;
                    case 2:
                        query = pendingKey + contractAddress + chain + "-";
                        break;
                }
                String[] keys = db.findKeys(query);
                for (String key : keys) {
                    list.add(db.getObject(key, CITATransactionDBItem.class));
                }
                db.close();
                Collections.sort(list, (o1, o2) -> (o2.timestamp - o1.timestamp));
                return list;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

}
