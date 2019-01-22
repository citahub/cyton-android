package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.ScreenUtils;


public class AuthFingerDialog extends Dialog implements View.OnClickListener {
    private TextView btn_ok;
    private Context context;

    public AuthFingerDialog(Context context) {
        super(context, R.style.DefaultDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_anth_finger);
        btn_ok = findViewById(R.id.btn);
        setCanceledOnTouchOutside(false);
        initAction();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = (int) (ScreenUtils.getScreenWidth(context) * 0.8);
        getWindow().setAttributes(p);
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
