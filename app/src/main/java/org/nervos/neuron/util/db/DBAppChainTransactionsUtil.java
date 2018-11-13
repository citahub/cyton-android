package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.transaction.TransactionItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class DBAppChainTransactionsUtil extends DBUtil {

    private static final String DB_APPCHAIN = "db_appchain_transaction";

    private static final String TOKEN = "token";

    /**
     * @param context
     * @param item
     */
    public static void save(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.chainId + tokenType + item.hash), item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void delete(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.del(getDbKey(item.chainId + tokenType + item.hash));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void update(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.chainId + tokenType + item.hash), item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }


    /**
     * get all local transactions of every chain
     *
     * @param context
     * @return
     */
    public static List<TransactionItem> getAllTransactions(Context context) {
        synchronized (dbObject) {
            List<TransactionItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_APPCHAIN);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    list.add(db.getObject(key, TransactionItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }


    /**
     * get all local transactions with chainId and contract address
     *
     * @param context
     * @return
     */
    public static List<TransactionItem> getAllTransactionsWithToken(Context context, String chainId, String contractAddress) {
        synchronized (dbObject) {
            List<TransactionItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_APPCHAIN);
                String tokenType = TextUtils.isEmpty(contractAddress) ? TOKEN : contractAddress;
                String[] keys = db.findKeys(getDbKey(chainId + tokenType));
                for (String key : keys) {
                    list.add(db.getObject(key, TransactionItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

}
