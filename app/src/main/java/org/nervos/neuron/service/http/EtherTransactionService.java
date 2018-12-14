package org.nervos.neuron.service.http;

import android.content.Context;


import org.nervos.neuron.item.response.EthTransactionStatusResponse;
import org.nervos.neuron.item.transaction.TransactionItem;
import org.nervos.neuron.util.db.DBEtherTransactionUtil;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherTransactionService implements TransactionService {

    private static final long ETH_BLOCK_DIFF = 200;

    public static void checkTransactionStatus(Context context) {
        EthRpcService.initNodeUrl();
        Observable.from(DBEtherTransactionUtil.getAllTransactions(context))
                .subscribe(new NeuronSubscriber<TransactionItem>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(TransactionItem item) {
                        EthTransactionStatusResponse response = HttpService.getEthTransactionStatus(item.hash);
                        if (response != null) {
                           if (response.result.status == 0) {
                               item.status = TransactionItem.FAILED;
                               DBEtherTransactionUtil.update(context, item);
                           } else if (exceedTwelveBlock(item)) {
                               DBEtherTransactionUtil.delete(context, item);
                           }
                        } else if (EthRpcService.getBlockNumber().subtract(new BigInteger(item.blockNumber)).longValue() > ETH_BLOCK_DIFF) {
                           item.status = TransactionItem.FAILED;
                           DBEtherTransactionUtil.update(context, item);
                        }
                    }
                });
    }


    private static final int TwelveBlockNumber = 12;
    private static boolean exceedTwelveBlock(TransactionItem item) {
        return AppChainRpcService.getBlockNumber()
                .subtract(Numeric.toBigInt(item.blockNumber)).longValue() > TwelveBlockNumber;
    }


}
