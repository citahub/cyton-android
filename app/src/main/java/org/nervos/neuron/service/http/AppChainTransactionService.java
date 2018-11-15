package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.neuron.item.transaction.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;

import java.math.BigInteger;

import rx.Observable;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService implements TransactionService {

    public static void checkTransactionStatus(Context context) {
        Observable.from(DBAppChainTransactionsUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<TransactionItem>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(TransactionItem item) {
                        TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);

                        if (receipt == null && new BigInteger(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                            item.status = TransactionItem.FAILED;
                            DBAppChainTransactionsUtil.update(context, item);
                        }
                        if (receipt != null && TextUtils.isEmpty(receipt.getErrorMessage())) {
                            DBAppChainTransactionsUtil.delete(context, item);
                        }
                    }
                });
    }

}
