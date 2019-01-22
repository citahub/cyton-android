package com.cryptape.cita_wallet.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.SnappydbException;

import com.cryptape.cita_wallet.item.transaction.RpcTransaction;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class CITATransactionsUtil extends DBUtil {

    private static final String DB_CITA = "db_cita_transaction";

    private static final String TOKEN = "token";

    /**
     * @param context
     * @param item
     */
    public static void save(Context context, RpcTransaction item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.getChainId() + tokenType + item.hash), item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void delete(Context context, RpcTransaction item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.del(getDbKey(item.getChainId() + tokenType + item.hash));
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void update(Context context, RpcTransaction item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CITA);
                String tokenType = TextUtils.isEmpty(item.contractAddress) ? TOKEN : item.contractAddress;
                db.put(getDbKey(item.getChainId() + tokenType + item.hash), item);
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
    public static List<RpcTransaction> getAllTransactions(Context context) {
        synchronized (dbObject) {
            List<RpcTransaction> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    list.add(db.getObject(key, RpcTransaction.class));
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
    public static List<RpcTransaction> getAllTransactionsWithToken(Context context, String chainId, String contractAddress) {
        synchronized (dbObject) {
            List<RpcTransaction> list = new ArrayList<>();
            try {
                db = openDB(context, DB_CITA);
                String tokenType = TextUtils.isEmpty(contractAddress) ? TOKEN : contractAddress;
                String[] keys = db.findKeys(getDbKey(chainId + tokenType));
                for (String key : keys) {
                    list.add(db.getObject(key, RpcTransaction.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }


}
