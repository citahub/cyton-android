package com.cryptape.cita_wallet.service.http;

import android.content.Context;

import com.cryptape.cita_wallet.item.response.EthTransactionStatus;
import com.cryptape.cita_wallet.item.transaction.RpcTransaction;
import com.cryptape.cita_wallet.util.db.DBEtherTransactionUtil;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import rx.Observable;

/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherTransactionService {

    private static final long ETH_BLOCK_DIFF = 200;

    public static void checkTransactionStatus(Context context) {
        EthRpcService.initNodeUrl();
        Observable.from(DBEtherTransactionUtil.getAllTransactions(context))
                .subscribe(new CytonSubscriber<RpcTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(RpcTransaction item) {
                        EthTransactionStatus response = HttpService.getEthTransactionStatus(item.hash);
                        if (response != null) {
                           if (response.result.status == 0) {
                               item.status = RpcTransaction.FAILED;
                               DBEtherTransactionUtil.update(context, item);
                           } else if (exceedTwelveBlock(item)) {
                               DBEtherTransactionUtil.delete(context, item);
                           }
                        } else if (EthRpcService.getBlockNumber().subtract(new BigInteger(item.blockNumber)).longValue() > ETH_BLOCK_DIFF) {
                           item.status = RpcTransaction.FAILED;
                           DBEtherTransactionUtil.update(context, item);
                        }
                    }
                });
    }


    private static final int TwelveBlockNumber = 12;
    private static boolean exceedTwelveBlock(RpcTransaction item) {
        return CITARpcService.getBlockNumber()
                .subtract(Numeric.toBigInt(item.blockNumber)).longValue() > TwelveBlockNumber;
    }


}
