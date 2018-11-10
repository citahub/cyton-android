package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import rx.Observable;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService {

    public static void checkTransactionStatus(Context context, OnCheckResultListener listener) {
        Observable.from(DBAppChainTransactionsUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<TransactionItem>() {
                    @Override
                    public void onError(Throwable e) {
                        listener.checkFinish();
                    }
                    @Override
                    public void onNext(TransactionItem item) {
                        listener.checkFinish();
                        TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);
                        if (receipt != null) {
                            if (!TextUtils.isEmpty(receipt.getErrorMessage())) {
                                item.status = TransactionItem.FAILED;
                                DBAppChainTransactionsUtil.update(context, item);
                            } else {
                                DBAppChainTransactionsUtil.delete(context, item);
                            }
                        } else if (new BigInteger(item.validUntilBlock)
                                .compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                            item.status = TransactionItem.FAILED;
                            DBAppChainTransactionsUtil.update(context, item);
                        }
                    }
                });
    }

    public static List<TransactionItem> getTransactionList(Context context, long chainId, List<TransactionItem> list) {
        List<TransactionItem> allList = DBAppChainTransactionsUtil.getAllTransactionsWithChain(context, chainId);
        if (allList.size() > 0) {
            for (TransactionItem dbItem : allList) {
                for (TransactionItem transactionItem : list) {
                    if (!transactionItem.hash.equalsIgnoreCase(dbItem.hash)) {
                        list.add(dbItem);
                        break;
                    }
                }
            }
            Collections.sort(list, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        }
        return list;
    }

    public interface OnCheckResultListener {
        void checkFinish();
    }

}
