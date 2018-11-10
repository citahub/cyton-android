package org.nervos.neuron.util;

import android.content.Context;

import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;
import org.nervos.neuron.util.db.DBChainUtil;

import java.util.Objects;

/**
 * Created by BaojunCZ on 2018/10/17.
 */
public class SaveAppChainPendingItemUtils {

    private static TransactionItem transactionItem = new TransactionItem();

    public static void setTransaction(long chainId, String from, String to, String value) {
        transactionItem.setTimestamp(System.currentTimeMillis());
        transactionItem.chainId = chainId;
        transactionItem.from = from;
        transactionItem.to = to;
        transactionItem.value = value;
        transactionItem.status = TransactionItem.PENDING;
    }

    public static void saveTransactionDB(Context context, String hash, String validUntilBlock) {
        transactionItem.validUntilBlock = validUntilBlock;
        transactionItem.hash = hash;
        DBAppChainTransactionsUtil.save(context, transactionItem);
    }

}
