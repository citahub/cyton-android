package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;

import com.cryptape.cita.protocol.core.methods.response.TransactionReceipt;

import org.nervos.neuron.item.transaction.RpcTransaction;
import org.nervos.neuron.util.db.CITATransactionsUtil;

import java.math.BigInteger;

import rx.Observable;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class CITATransactionService{

    public static void checkTransactionStatus(Context context) {
        Observable.from(CITATransactionsUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<RpcTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(RpcTransaction item) {
                        TransactionReceipt receipt = CITARpcService.getTransactionReceipt(item.hash);

                        if (receipt == null && new BigInteger(item.validUntilBlock).compareTo(CITARpcService.getBlockNumber()) < 0) {
                            item.status = RpcTransaction.FAILED;
                            CITATransactionsUtil.update(context, item);
                        }
                        if (receipt != null) {
                            if (!TextUtils.isEmpty(receipt.getErrorMessage())) {
                                item.status = RpcTransaction.FAILED;
                                CITATransactionsUtil.update(context, item);
                            } else {
                                CITATransactionsUtil.delete(context, item);
                            }
                        }
                    }
                });
    }

}
