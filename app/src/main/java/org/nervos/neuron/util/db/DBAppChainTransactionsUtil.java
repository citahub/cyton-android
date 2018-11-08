package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.AppChainTransactionDBItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class DBAppChainTransactionsUtil extends DBUtil {
    private static final String DB_APPCHAIN = "db_appchain_transaction";
    private static final String DB_PRE_APPCHAIN = DB_PREFIX + "appchain-";

    private static final String DB_PRE_PENDING_HALF = DB_PRE_APPCHAIN + "pending-";
    private static final String DB_PRE_FAILED_HALF = DB_PRE_APPCHAIN + "failed-";
    private static final String DB_PRE_TOKEN_HALF = "native-";
    private static final String DB_PRE_CHAIN_HALF = "chain-";

    private static final String DB_KEY_NATIVE = "native";
    private static final String DB_KEY_CHAIN = "chain";

    private static final String DB_PRE_PENDING = DB_PRE_PENDING_HALF + DB_PRE_TOKEN_HALF + DB_PRE_CHAIN_HALF;
    private static final String DB_PRE_FAILED = DB_PRE_FAILED_HALF + DB_PRE_TOKEN_HALF + DB_PRE_CHAIN_HALF;

    public enum TokenType {
        ALL, NATIVE, TOKEN
    }


    /**
     *
     * @param context
     * @param isPending  true:pending false: failed
     * @param item
     */
    public static void save(Context context, boolean isPending, AppChainTransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                String pendingKey = isPending ? DB_PRE_PENDING : DB_PRE_FAILED;
                item.status = isPending ? AppChainTransactionDBItem.PENDING : AppChainTransactionDBItem.FAILED;
                pendingKey = (pendingKey + item.hash).replace(DB_KEY_CHAIN, item.chain);

                if (!item.isNativeToken) pendingKey = pendingKey.replace(DB_KEY_NATIVE, item.contractAddress);
                db.put(pendingKey, item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void deletePending(Context context, AppChainTransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                if (item.isNativeToken) {
                    db.del((DB_PRE_PENDING + item.hash).replace(DB_KEY_CHAIN, item.chain));
                } else {
                    db.del((DB_PRE_PENDING + item.hash).replace(DB_KEY_CHAIN, item.chain)
                            .replace(DB_KEY_NATIVE, item.contractAddress));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void failed(Context context, AppChainTransactionDBItem item) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_APPCHAIN);
                String key = (DB_PRE_PENDING + item.hash).replace(DB_KEY_CHAIN, item.chain);
                if (!item.isNativeToken) {
                    key = key.replace(DB_KEY_NATIVE, item.contractAddress);
                }
                item.status = AppChainTransactionDBItem.FAILED;
                db.put(key, item);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }


    /**
     * get all local pending transactions to query (native & token)
     * 获取所有本地保存的交易数据，轮训使用
     *
     * @param context
     * @param isPending true:pending false: failed
     * @param type    0:all 1:native 2:token
     * @return
     */
    public static List<AppChainTransactionDBItem> getAllTransactions(Context context, boolean isPending,
                                                                     TokenType type, String contractAddress) {
        synchronized (dbObject) {
            List<AppChainTransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_APPCHAIN);
                String pendingKey = isPending ? DB_PRE_PENDING_HALF : DB_PRE_PENDING_HALF;
                String query;
                switch (type) {
                    case NATIVE:
                        query = pendingKey + DB_PRE_TOKEN_HALF;
                        break;
                    case TOKEN:
                        query = pendingKey + contractAddress;
                        break;
                    default:
                        query = DB_PRE_APPCHAIN;
                        break;
                }
                String[] keys = db.findKeys(query);
                for (String key : keys) {
                    list.add(db.getObject(key, AppChainTransactionDBItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

    /**
     * get all pending transactions in one chain to display(native & token)
     * 获取某条链上的交易数据，展示使用
     *
     * @param context
     * @param chain   chain ip
     * @param type
     * @return
     */
    public static List<AppChainTransactionDBItem> getAllTransactionWithChain(Context context, String chain,
                                                                             TokenType type, String contractAddress) {
        synchronized (dbObject) {
            List<AppChainTransactionDBItem> list = new ArrayList<>();
            try {
                db = openDB(context, DB_APPCHAIN);
                String queryPending = DB_PRE_PENDING.replace(DB_KEY_CHAIN, chain);
                String queryFailed = DB_PRE_FAILED.replace(DB_KEY_CHAIN, chain);
                if (type == TokenType.TOKEN) {
                    queryPending = queryPending.replace(DB_KEY_NATIVE, contractAddress);
                    queryFailed = queryFailed.replace(DB_KEY_NATIVE, contractAddress);
                }
                String[] keysPending = db.findKeys(queryPending);
                for (String key : keysPending) {
                    list.add(db.getObject(key, AppChainTransactionDBItem.class));
                }
                String[] keysFailed = db.findKeys(queryFailed);
                for (String key : keysFailed) {
                    list.add(db.getObject(key, AppChainTransactionDBItem.class));
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return list;
        }
    }

}
