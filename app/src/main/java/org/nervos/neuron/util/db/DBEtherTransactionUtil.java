package org.nervos.neuron.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.transaction.TransactionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/11/18
 */
public class DBEtherTransactionUtil extends DBUtil {

    private static final String DB_ETH_TRANSACTION = "db_eth_transaction";

    private static final String TOKEN = "token";

    public static void save(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.getChainId() + tokenType + item.hash), item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void update(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.getChainId() + tokenType + item.hash), item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void delete(Context context, TransactionItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.del(getDbKey(item.getChainId() + tokenType + item.hash));
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
                    transactionItemList.add(db.getObject(key, TransactionItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return transactionItemList;
        }
    }


    public static List<TransactionItem> getAllTransactionsWithToken(Context context, String chainId, String contractAddress) {
        synchronized (dbObject) {
            List<TransactionItem> transactionItemList = new ArrayList<>();
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String tokenType = TextUtils.isEmpty(contractAddress) ? TOKEN : contractAddress;
                String[] keys = db.findKeys(getDbKey(chainId + tokenType));
                for (String key : keys) {
                    transactionItemList.add(db.getObject(key, TransactionItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return transactionItemList;
        }
    }

}
