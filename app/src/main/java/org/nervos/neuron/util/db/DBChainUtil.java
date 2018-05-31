package org.nervos.neuron.util.db;

import android.content.Context;
import android.util.Log;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.nervos.neuron.item.ChainItem;

import java.util.ArrayList;
import java.util.List;

public class DBChainUtil extends DBUtil {

    private static final String DB_CHAIN = "db_chain";
    public static final int ETHEREUM_ID = -1;
    private static final String ETHEREUM_NAME = "以太坊mainnet";


    public static List<ChainItem> getAllChain(Context context) {
        List<ChainItem> chainItemList = new ArrayList<>();
        try {
            DB db = DBFactory.open(context, DB_CHAIN);
            String[] keys = db.findKeys(DB_PREFIX);
            for(String key: keys) {
                ChainItem chainItem = db.getObject(key, ChainItem.class);
                chainItem.chainId = Integer.parseInt(getDbOrigin(key));
                chainItemList.add(chainItem);
            }
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return chainItemList;
    }

    public static ChainItem getChain(Context context, int chainId) {
        try {
            DB db = DBFactory.open(context, DB_CHAIN);
            ChainItem chainItem = db.getObject(getDbKey(String.valueOf(chainId)), ChainItem.class);
            db.close();
            return chainItem;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
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
            db.put(getDbKey(String.valueOf(chainItem.chainId)), chainItem);
            db.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void initChainData(Context context) {
        saveChain(context, new ChainItem(ETHEREUM_ID, ETHEREUM_NAME));
    }

}
