package org.nervos.neuron.service.intentService;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nervos.neuron.service.AppChainTransactionService;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class TransactionListService extends IntentService {

    public static AppChainTransactionService.CheckImpl impl;

    public TransactionListService() {
        super("TransactionListService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppChainTransactionService.checkResult(this, impl);
    }
}
