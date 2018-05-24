package org.nervos.neuron.util;

import android.content.Context;
import android.util.Log;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;

import java.util.ArrayList;
import java.util.List;

public class DBChainUtil {

    private static final String DB_CHAIN_PREFIX = "neuron-";
    private static final String DB_CHAIN = "db_chain";
    public static final String ETHEREUM_ID = "-1";
    private static final String ETHEREUM_NAME = "以太坊mainnet";


    public static List<ChainItem> getAllChain(Context context) {
        List<ChainItem> chainItemList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_CHAIN);
            String[] keys = db.findKeys(DB_CHAIN_PREFIX);
            Log.d("keys", "keys: " + keys[0]);
            for(String key: keys) {
                ChainItem chainItem = db.getObject(key, ChainItem.class);
                chainItem.chainId = getDbChainId(key);
                chainItemList.add(chainItem);
            }
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return chainItemList;
    }


    public static List<String> getAllChainName(Context context) {
        List<String> chainNameList = new ArrayList<>();
        List<ChainItem> chainItemList = getAllChain(context);

        for(ChainItem chainItem : chainItemList) {
            chainNameList.add(chainItem.name);
        }
        return chainNameList;
    }


    public static void saveChain(Context context, ChainItem chainItem){
        try {
            DB db = DBFactory.open(context, DB_CHAIN);
            db.put(getDbChainKey(chainItem.chainId), chainItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void initChainData(Context context) {
        saveChain(context, new ChainItem(ETHEREUM_ID, ETHEREUM_NAME));
    }


    private static String getDbChainKey(String chainId) {
        return DB_CHAIN_PREFIX + chainId;
    }

    private static String getDbChainId(String key) {
        return key.substring(DB_CHAIN_PREFIX.length());
    }

}
