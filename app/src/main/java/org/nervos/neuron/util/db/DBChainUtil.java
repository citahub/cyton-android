package org.nervos.neuron.util.db;

import android.content.Context;

import com.google.gson.Gson;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DBChainUtil extends DBUtil {

    private static final String DB_CHAIN = "db_chain";

    public static List<ChainItem> getAllChain(Context context) {
        synchronized (dbObject) {
            List<ChainItem> chainItemList = new ArrayList<>();
            try {
                db = openDB(context, DB_CHAIN);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    ChainItem chainItem = db.getObject(key, ChainItem.class);
                    LogUtil.d("chainItem: " + new Gson().toJson(chainItem));
                    chainItem.chainId = Integer.parseInt(getDbOrigin(key));
                    chainItemList.add(chainItem);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return chainItemList;
        }
    }

    public static ChainItem getChain(Context context, long chainId) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CHAIN);
                ChainItem chainItem = db.getObject(getDbKey(String.valueOf(chainId)), ChainItem.class);
                db.close();
                return chainItem;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return null;
        }
    }


    public static List<String> getAllChainName(Context context) {
        synchronized (dbObject) {
            List<String> chainNameList = new ArrayList<>();
            List<ChainItem> chainItemList = getAllChain(context);

            for (ChainItem chainItem : chainItemList) {
                chainNameList.add(chainItem.name);
            }
            return chainNameList;
        }
    }


    public static void saveChain(Context context, ChainItem chainItem) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CHAIN);
                db.put(getDbKey(String.valueOf(chainItem.chainId)), chainItem);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void initChainData(Context context) {
        saveChain(context, new ChainItem(ConstUtil.ETHEREUM_ID, ConstUtil.ETH_MAINNET, ConstUtil.ETH, ConstUtil.ETH));
        saveChain(context, new ChainItem(ConstUtil.CMB_CHAIN_ID, ConstUtil.CMB_CHAIN_NAME, ConstUtil.CMB_HTTP_PROVIDER, ConstUtil.CMB_TOKEN_NAME, ConstUtil.CMB_TOKEN_SYMBOL, ConstUtil.CMB_TOKEN_AVATAR));
    }

}
