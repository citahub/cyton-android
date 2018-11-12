package org.nervos.neuron.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.util.ConstantUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/24
 */
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
        saveChain(context, new ChainItem(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAINNET, ConstantUtil.ETH, ConstantUtil.ETH));
        saveChain(context, new ChainItem(ConstantUtil.CMB_CHAIN_ID, ConstantUtil.CMB_CHAIN_NAME, ConstantUtil.CMB_HTTP_PROVIDER, ConstantUtil.CMB_TOKEN_NAME, ConstantUtil.CMB_TOKEN_SYMBOL, ConstantUtil.CMB_TOKEN_AVATAR));
    }

}
