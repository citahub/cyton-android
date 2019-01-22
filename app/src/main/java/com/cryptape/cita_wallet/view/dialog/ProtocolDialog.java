package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;

/**
 * Created by BaojunCZ on 2018/9/17.
 */
public class ProtocolDialog extends Dialog {

    private ImageView checkBox;
    private TextView ok;
    private Context context;
    private boolean checked = false;

    public ProtocolDialog(@NonNull Context context) {
        super(context, R.style.DefaultDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_protocol);
        checkBox = findViewById(R.id.iv_check);
        ok = findViewById(R.id.tv_ok);
        checkBox.setOnClickListener(view -> {
            checked = !checked;
            if (checked) {
                checkBox.setImageResource(R.drawable.circle_selected);
                ok.setTextColor(context.getResources().getColor(R.color.font_link));
            } else {
                checkBox.setImageResource(R.drawable.circle_unselect);
                ok.setTextColor(context.getResources().getColor(R.color.font_title_third));
            }
        });
        ok.setOnClickListener(view -> {
            if (checked) {
                SharePrefUtil.putBoolean(ConstantUtil.PROTOCOL, true);
                dismiss();
            } else {
                Toast.makeText(context, context.getString(R.string.protocol_error_toast), Toast.LENGTH_SHORT).show();
            }
        });
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
