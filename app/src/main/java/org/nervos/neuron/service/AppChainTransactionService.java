package org.nervos.neuron.service;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.AppChainTransactionDBItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService {
    private static Observable<AppChainTransactionDBItem> query(Context context, boolean pending, int type, String contractAddress) {
        List<AppChainTransactionDBItem> list = DBAppChainTransactionsUtil.getAll(context, pending, type, contractAddress);
        return Observable
                .from(list)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread());
    }

    public static void checkResult(Context context, CheckImpl impl) {
        query(context, true, 0, "")
                .subscribe(new Subscriber<AppChainTransactionDBItem>() {
                    @Override
                    public void onCompleted() {
                        impl.checkFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        impl.checkFinish();
                    }

                    @Override
                    public void onNext(AppChainTransactionDBItem item) {
                        if (!TextUtils.isEmpty(item.chain)) {
                            try {
                                AppChainRpcService.setHttpProvider(item.chain);
                                TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);
                                if (receipt != null) {
                                    DBAppChainTransactionsUtil.deletePending(context, item);
                                } else {
                                    if (Numeric.decodeQuantity(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                                        DBAppChainTransactionsUtil.failed(context, item);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            DBAppChainTransactionsUtil.deletePending(context, item);
                        }
                    }
                });
    }

    public static List<TransactionItem> getTransactionList(Context context, boolean type, String chain, String contractAddress, List<TransactionItem> list, String from) {
        List<AppChainTransactionDBItem> allList = DBAppChainTransactionsUtil.getChainAll(context, chain, type, contractAddress);
        if (allList.size() > 0) {
            for (AppChainTransactionDBItem item : allList) {
                boolean isReceive = false;
                for (TransactionItem transactionItem : list) {
                    if (transactionItem.hash.equalsIgnoreCase(item.hash)) {
                        isReceive = true;
                        break;
                    }
                }
                if (!isReceive && list.get(list.size() - 1).getDate().compareTo(item.getDate()) < 0 && from.equalsIgnoreCase(item.from)) {
                    TransactionItem transactionItem = new TransactionItem();
                    transactionItem.from = item.from;
                    transactionItem.to = item.to;
                    transactionItem.value = item.value;
                    transactionItem.chainName = item.chainName;
                    transactionItem.status = item.status;
                    transactionItem.setTimestamp(item.timestamp);
                    transactionItem.hash = item.hash;
                    list.add(transactionItem);
                }
            }
            Collections.sort(list, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        }
        return list;
    }

    public interface CheckImpl {
        void checkFinish();
    }

}
