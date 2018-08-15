package org.nervos.neuron.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.nervos.neuron.R;


public class AuthFingerDialog extends Dialog implements View.OnClickListener {
    private TextView btn_ok;

    public AuthFingerDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_anth_finger);
        btn_ok = findViewById(R.id.btn);
        setCanceledOnTouchOutside(false);
        initAction();
    }

    private void initAction() {
        btn_ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn) {
            dismiss();
        }
    }
}
