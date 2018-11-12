package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.TransactionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/11/18
 */
public class DBEtherTransactionUtil extends DBUtil {

    private static final String DB_ETH_TRANSACTION = "db_eth_transaction";

    public static void save(Context context, TransactionItem transactionItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                db.put(getDbKey(transactionItem.hash), transactionItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void update(Context context, TransactionItem transactionItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                db.put(getDbKey(transactionItem.hash), transactionItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void delete(Context context, String hash) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                db.del(getDbKey(hash));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }


    public static List<TransactionItem> getAllTransactions(Context context) {
        synchronized (dbObject) {
            List<TransactionItem> transactionItemList = new ArrayList<>();
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    TransactionItem transactionItem = db.getObject(key, TransactionItem.class);
                    transactionItem.hash = getDbOrigin(key);
                    transactionItemList.add(transactionItem);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return transactionItemList;
        }
    }


    public static List<TransactionItem> getAllTransactionsWithChain(Context context, String chainId) {
        synchronized (dbObject) {
            List<TransactionItem> transactionItemList = new ArrayList<>();
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String[] keys = db.findKeys(getDbKey(chainId));
                for (String key : keys) {
                    TransactionItem transactionItem = db.getObject(key, TransactionItem.class);
                    transactionItem.hash = getDbOrigin(key);
                    transactionItemList.add(transactionItem);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return transactionItemList;
        }
    }

}
