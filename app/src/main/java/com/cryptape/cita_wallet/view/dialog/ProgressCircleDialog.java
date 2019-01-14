package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.cryptape.cita_wallet.R;

/**
 * Created by BaojunCZ on 2018/8/30.
 */
public class ProgressCircleDialog extends Dialog {
    public ProgressCircleDialog(@NonNull Context context) {
        super(context, R.style.ProgressDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progressbar_circle);
    }
}
