package org.nervos.neuron.service.http;

import android.content.Context;

import org.nervos.neuron.item.transaction.TransactionItem;
import org.nervos.neuron.util.db.DBEtherTransactionUtil;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.utils.Numeric;

import rx.Observable;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherTransactionService implements TransactionService {

    private static final long ETH_BLOCK_DIFF = 200;

    public static void checkTransactionStatus(Context context) {
        Observable.from(DBEtherTransactionUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<TransactionItem>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(TransactionItem item) {
                        EthGetTransactionReceipt receipt = EthRpcService.getTransactionReceipt(item.hash);
                        if (receipt == null && AppChainRpcService.getBlockNumber()
                                .subtract(Numeric.toBigInt(item.blockNumber)).longValue() > ETH_BLOCK_DIFF) {
                            item.status = TransactionItem.FAILED;
                            DBEtherTransactionUtil.update(context, item);
                        }
                        if (receipt != null && !receipt.hasError()) {
                            DBEtherTransactionUtil.delete(context, item);
                        }
                    }
                });
    }




}
