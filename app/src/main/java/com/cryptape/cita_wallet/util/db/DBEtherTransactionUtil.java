package com.cryptape.cita_wallet.util.db;

import android.content.Context;
import android.text.TextUtils;

import com.snappydb.SnappydbException;

import com.cryptape.cita_wallet.item.transaction.RpcTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/11/18
 */
public class DBEtherTransactionUtil extends DBUtil {

    private static final String DB_ETH_TRANSACTION = "db_eth_transaction";

    private static final String TOKEN = "token";

    public static void save(Context context, RpcTransaction item) {
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

    public static void update(Context context, RpcTransaction item) {
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

    public static void delete(Context context, RpcTransaction item) {
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


    public static List<RpcTransaction> getAllTransactions(Context context) {
        synchronized (dbObject) {
            List<RpcTransaction> rpcTransactionList = new ArrayList<>();
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    rpcTransactionList.add(db.getObject(key, RpcTransaction.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return rpcTransactionList;
        }
    }


    public static List<RpcTransaction> getAllTransactionsWithToken(Context context, String chainId, String contractAddress) {
        synchronized (dbObject) {
            List<RpcTransaction> rpcTransactionList = new ArrayList<>();
            try {
                db = openDB(context, DB_ETH_TRANSACTION);
                String tokenType = TextUtils.isEmpty(contractAddress) ? TOKEN : contractAddress;
                String[] keys = db.findKeys(getDbKey(chainId + tokenType));
                for (String key : keys) {
                    rpcTransactionList.add(db.getObject(key, RpcTransaction.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return rpcTransactionList;
        }
    }

}
