package org.nervos.neuron.service.http;

import android.content.Context;
import org.nervos.appchain.protocol.core.methods.response.AppBlock;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;
import org.nervos.neuron.util.db.DBChainUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import rx.Observable;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService {

    public static void checkTransactionStatus(Context context, CheckImpl impl) {
        Observable.from(DBAppChainTransactionsUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<TransactionItem>() {
                    @Override
                    public void onError(Throwable e) {
                        impl.checkFinish();
                    }
                    @Override
                    public void onNext(TransactionItem item) {
                        impl.checkFinish();
                        AppChainRpcService.setHttpProvider(Objects.requireNonNull(DBChainUtil.getChain(context, item.chainId)).httpProvider);
                        AppBlock appBlock = AppChainRpcService.getAppBlock(item.hash);
                        if (appBlock != null) {
                            if (!appBlock.hasError()) {
                                item.status = TransactionItem.FAILED;
                                DBAppChainTransactionsUtil.update(context, item);
                            } else if (Numeric.decodeQuantity(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) > 0) {
                                DBAppChainTransactionsUtil.delete(context, item);
                            }
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

    public interface CheckImpl {
        void checkFinish();
    }

}
