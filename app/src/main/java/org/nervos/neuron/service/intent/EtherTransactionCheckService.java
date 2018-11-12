package org.nervos.neuron.service.intent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import org.nervos.neuron.service.http.EtherTransactionService;
import org.nervos.neuron.service.http.TransactionService;


/**
 * Created by duanyytop on 2018/11/12.
 */
public class EtherTransactionCheckService extends JobIntentService {

    public static TransactionService.OnCheckResultListener listener;

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1001;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AppChainTransactionCheckService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        EtherTransactionService.checkTransactionStatus(this, listener);
    }

}
