package com.cryptape.cita_wallet.util.db;

import android.content.Context;

import com.snappydb.SnappydbException;

import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.constant.ConstantUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/24
 */
public class DBChainUtil extends DBUtil {

    private static final String DB_CHAIN = "db_chain";

    public static List<Chain> getAllChain(Context context) {
        synchronized (dbObject) {
            List<Chain> chainList = new ArrayList<>();
            try {
                db = openDB(context, DB_CHAIN);
                String[] keys = db.findKeys(DB_PREFIX);
                for (String key : keys) {
                    Chain chain = db.getObject(key, Chain.class);
                    chain.setChainId(getDbOrigin(key));
                    chainList.add(chain);
                }
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return chainList;
        }
    }

    public static Chain getChain(Context context, String chainId) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CHAIN);
                Chain chain = db.getObject(getDbKey(chainId), Chain.class);
                db.close();
                return chain;
            } catch (SnappydbException e) {
                handleException(db, e);
            }
            return null;
        }
    }


    public static List<String> getAllChainName(Context context) {
        synchronized (dbObject) {
            List<String> chainNameList = new ArrayList<>();
            List<Chain> chainList = getAllChain(context);

            for (Chain chain : chainList) {
                chainNameList.add(chain.name);
            }
            return chainNameList;
        }
    }


    public static void saveChain(Context context, Chain chain) {
        synchronized (dbObject) {
            try {
                db = openDB(context, DB_CHAIN);
                db.put(getDbKey(chain.getChainId()), chain);
                db.close();
            } catch (SnappydbException e) {
                handleException(db, e);
            }
        }
    }

    public static void initChainData(Context context) {
        saveChain(context, new Chain(ConstantUtil.ETHEREUM_MAIN_ID, ConstantUtil.ETH_MAINNET, ConstantUtil.ETH, ConstantUtil.ETH));
        saveChain(context, new Chain(ConstantUtil.MBA_CHAIN_ID, ConstantUtil.MBA_CHAIN_NAME, ConstantUtil.MBA_HTTP_PROVIDER,
                ConstantUtil.MBA_TOKEN_NAME, ConstantUtil.MBA_TOKEN_SYMBOL, ConstantUtil.MBA_TOKEN_AVATAR));
    }

}
