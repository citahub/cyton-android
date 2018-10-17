package org.nervos.neuron.util;

import android.content.Context;

import org.nervos.neuron.item.AppChainTransactionDBItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

/**
 * Created by BaojunCZ on 2018/10/17.
 */
public class SaveAppChainPendingItemUtils {

    private static AppChainTransactionDBItem appChainTransactionDBItem = new AppChainTransactionDBItem();

    public static void setNativeToken(Context context, long chainId, String from, String to, String value) {
        appChainTransactionDBItem.timestamp = System.currentTimeMillis();
        appChainTransactionDBItem.chain = SharePrefUtil.getChainHostFromId(chainId);
        appChainTransactionDBItem.from = from;
        appChainTransactionDBItem.to = to;
        appChainTransactionDBItem.value = value;
        appChainTransactionDBItem.chainName = DBChainUtil.getChain(context, chainId).name;
        appChainTransactionDBItem.status = 2;
        appChainTransactionDBItem.isNativeToken = true;
    }

    public static void setErc20(Context context, String contractAddress, long chainId, String from, String to, String value) {
        appChainTransactionDBItem.timestamp = System.currentTimeMillis();
        appChainTransactionDBItem.chain = SharePrefUtil.getChainHostFromId(chainId);
        appChainTransactionDBItem.from = from;
        appChainTransactionDBItem.to = to;
        appChainTransactionDBItem.value = value;
        appChainTransactionDBItem.chainName = DBChainUtil.getChain(context, chainId).name;
        appChainTransactionDBItem.status = 2;
        appChainTransactionDBItem.contractAddress = contractAddress;
        appChainTransactionDBItem.isNativeToken = false;
    }

    public static void saveItem(Context context, String hash, String validUntilBlock) {
        appChainTransactionDBItem.validUntilBlock = validUntilBlock;
        appChainTransactionDBItem.hash = hash;
        DBAppChainTransactionsUtil.save(context, true, appChainTransactionDBItem);
    }

}
