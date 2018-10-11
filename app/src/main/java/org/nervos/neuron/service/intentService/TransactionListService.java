package org.nervos.neuron.service.intentService;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nervos.neuron.service.CITATransactionService;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class TransactionListService extends IntentService {

    public static CITATransactionService.CheckImpl impl;

    public TransactionListService() {
        super("TransactionListService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        CITATransactionService.checkResult(this, impl);
    }
}
