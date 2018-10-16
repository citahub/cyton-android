package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.CITATransactionDBItem;
import org.nervos.neuron.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class DBCITATransactionsUtil extends DBUtil {
    private static final String DB_CITA = "db_cita_transaction";
    private static final String DB_PRE_CITA = DB_PREFIX + "cita-";

    private static final String DB_PRE_PENDING_HALF = DB_PRE_CITA + "pending-";
    private static final String DB_PRE_FAILED_HALF = DB_PRE_CITA + "failed-";
    private static final String DB_PRE_TOKEN_HALF = "native-";
    private static final String DB_PRE_CHAIN_HALF = "chain-";

    private static final String DB_PRE_PENDING = DB_PRE_PENDING_HALF + DB_PRE_TOKEN_HALF + DB_PRE_CHAIN_HALF;
    private static final String DB_PRE_FAILED = DB_PRE_FAILED_HALF + DB_PRE_TOKEN_HALF + DB_PRE_CHAIN_HALF;


    public static void save(Context context, boolean pending, CITATransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String pendingKey;
                if (pending) {
                    pendingKey = DB_PRE_PENDING;
                    item.status = 2;
                } else {
                    pendingKey = DB_PRE_FAILED;
                    item.status = 0;
                }
                pendingKey = (pendingKey + item.hash).replace("chain", item.chain);
                if (item.isNativeToken) {
                    db.put(pendingKey, item);
                } else {
                    pendingKey = pendingKey.replace("native", item.contractAddress);
                    db.put(pendingKey, item);
                }
//                LogUtil.d("save>>>" + pendingKey);
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
                    db.del((DB_PRE_PENDING + item.hash).replace("chain", item.chain));
                } else {
                    db.del((DB_PRE_PENDING + item.hash).replace("chain", item.chain).replace("native", item.contractAddress));
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
                    key = (DB_PRE_PENDING + item.hash).replace("chain", item.chain);
                } else {
                    key = (DB_PRE_PENDING + item.hash).replace("chain", item.chain).replace("native", item.contractAddress);
                }
                if (db.exists(key)) {
                    db.del(key);
                }
                if (item.isNativeToken) {
                    key = (DB_PRE_PENDING + item.hash).replace("chain", item.chain);
                } else {
                    key = (DB_PRE_PENDING + item.hash).replace("chain", item.chain).replace("native", item.contractAddress);
                }
                item.status = 0;
                db.put(key, item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }


    /**
     * get all pending transaction (native & token)
     * 获取所有本地保存的交易数据，轮训使用
     *
     * @param context
     * @param pending true:pending false: failed
     * @param type    0:all 1:native 2:token
     * @return
     */
    public static List<CITATransactionDBItem> getAll(Context context, boolean pending, int type, String contractAddress) {
        synchronized (dbObject) {
            List<CITATransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String pendingKey = pending ? DB_PRE_PENDING_HALF : DB_PRE_PENDING_HALF;
                String query;
                switch (type) {
                    case 0:
                    default:
                        query = DB_PRE_CITA;
                        break;
                    case 1:
                        query = pendingKey + DB_PRE_TOKEN_HALF;
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
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

    /**
     * get all pending transaction in one chain to display(native & token)
     * 获取某条链上的交易数据，展示使用
     *
     * @param context
     * @param chain   chain ip
     * @param type    true:native fasle:token
     * @return
     */
    public static List<CITATransactionDBItem> getChainAll(Context context, String chain, boolean type, String contractAddress) {
        synchronized (dbObject) {
            List<CITATransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String queryPending = DB_PRE_PENDING.replace("chain", chain);
                String queryFailed = DB_PRE_FAILED.replace("chain", chain);
                if (!type) {
                    queryPending = queryPending.replace("native", contractAddress);
                    queryFailed = queryFailed.replace("native", contractAddress);
                }
                String[] keysPending = db.findKeys(queryPending);
                for (String key : keysPending) {
                    list.add(db.getObject(key, CITATransactionDBItem.class));
                }
                String[] keysFailed = db.findKeys(queryFailed);
                for (String key : keysFailed) {
                    list.add(db.getObject(key, CITATransactionDBItem.class));
                }
                db.close();
//                LogUtil.d("show queryPending>>>" + queryPending);
            } catch (SnappydbException e) {
                handleException(db, e);
            }
//            LogUtil.d("show>>>" + list.size());
            return list;
        }
    }

}
