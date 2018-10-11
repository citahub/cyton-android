package org.nervos.neuron.service;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.CITATransactionDBItem;
import org.nervos.neuron.util.db.DBCITATransactionsUtil;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class CITATransactionService {
    private static Observable<CITATransactionDBItem> query(Context context, boolean pending, int type, String contractAddress) {
        List<CITATransactionDBItem> list = DBCITATransactionsUtil.getAllPending(context, pending, type, contractAddress);
        return Observable
                .from(list)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread());
    }

    public static void checkResult(Context context, CheckImpl impl) {
        query(context, true, 0, "")
                .subscribe(new Subscriber<CITATransactionDBItem>() {
                    @Override
                    public void onCompleted() {
                        impl.checkFinish();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CITATransactionDBItem item) {
                        if (!TextUtils.isEmpty(item.chain)) {
                            try {
                                AppChainRpcService.setHttpProvider(item.chain);
                                TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);
                                if (receipt != null) {
                                    DBCITATransactionsUtil.deletePending(context, item);
                                } else {
                                    if (Numeric.decodeQuantity(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                                        DBCITATransactionsUtil.failed(context, item);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            DBCITATransactionsUtil.deletePending(context, item);
                        }
                    }
                });
    }

    public interface CheckImpl {
        void checkFinish();
    }

}
