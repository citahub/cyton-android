package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.neuron.item.transaction.RpcTransaction;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;
import rx.Observable;

import java.math.BigInteger;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService {

    public static void checkTransactionStatus(Context context) {
        Observable.from(DBAppChainTransactionsUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<RpcTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(RpcTransaction item) {
                        TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);

                        if (receipt == null && new BigInteger(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                            item.status = RpcTransaction.FAILED;
                            DBAppChainTransactionsUtil.update(context, item);
                        }
                        if (receipt != null) {
                            if (!TextUtils.isEmpty(receipt.getErrorMessage())) {
                                item.status = RpcTransaction.FAILED;
                                DBAppChainTransactionsUtil.update(context, item);
                            } else {
                                DBAppChainTransactionsUtil.delete(context, item);
                            }
                        }
                    }
                });
    }

}
