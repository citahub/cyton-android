package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;

/**
 * Created by BaojunCZ on 2018/8/30.
 */
public class ProgressingDialog extends Dialog {

    private TextView msgTv;

    public ProgressingDialog(@NonNull Context context) {
        super(context, R.style.ProgressDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progressbar_layout);
        msgTv = findViewById(R.id.progress_bar_text);
    }

    public void setMsg(String msg) {
        msgTv.setText(msg);
    }
}
