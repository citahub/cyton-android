package org.nervos.neuron.service;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.CITATransactionDBItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBCITATransactionsUtil;
import org.nervos.neuron.util.db.DBChainUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class CITATransactionService {
    private static Observable<CITATransactionDBItem> query(Context context, boolean pending, int type, String contractAddress) {
        List<CITATransactionDBItem> list = DBCITATransactionsUtil.getAll(context, pending, type, contractAddress);
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
                        impl.checkFinish();
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

    public static List<TransactionItem> getTransactionList(Context context, boolean type, String chain, String contractAddress, List<TransactionItem> list, String from) {
        List<CITATransactionDBItem> allList = DBCITATransactionsUtil.getChainAll(context, chain, type, contractAddress);
        if (allList.size() > 0) {
            for (CITATransactionDBItem item : allList) {
                Long oldestTime;
                if (list.get(list.size() - 1).timeStamp > 0) {
                    oldestTime = list.get(list.size() - 1).timeStamp * 1000;
                } else {
                    oldestTime = list.get(list.size() - 1).timestamp;
                }
                boolean isReceive = false;
                for (TransactionItem transactionItem : list) {
                    if (transactionItem.hash.equalsIgnoreCase(item.hash)) {
                        isReceive = true;
                        break;
                    }
                }
                if (!isReceive && oldestTime < item.timestamp && from.equalsIgnoreCase(item.from)) {
                    TransactionItem transactionItem = new TransactionItem();
                    transactionItem.from = item.from;
                    transactionItem.to = item.to;
                    transactionItem.value = item.value;
                    transactionItem.chainName = item.chainName;
                    transactionItem.status = item.status;
                    transactionItem.timestamp = item.timestamp;
                    transactionItem.hash = item.hash;
                    list.add(transactionItem);
                }
            }
            Collections.sort(list, (o1, o2) -> {
                int ret = 0;
                Long x = Long.valueOf(o1.timestamp);
                Long y = Long.valueOf(o2.timestamp);
                ret = -1 * x.compareTo(y);
                return ret;
            });
        }
        return list;
    }

    public interface CheckImpl {
        void checkFinish();
    }

}
