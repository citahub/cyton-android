package org.nervos.neuron.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.nervos.neuron.R;

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
