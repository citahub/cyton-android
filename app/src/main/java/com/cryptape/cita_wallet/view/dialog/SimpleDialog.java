package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.ScreenUtils;

public class SimpleDialog extends Dialog {

    public static final int PASSWORD = 0;
    public static final int TEXT = 1;

    private Context context;
    private TextView okBtn;
    private TextView cancelBtn;
    private TextView titleText;
    private AppCompatEditText messageEdit;
    private String titleStr;
    private int titleResId;
    private String hintStr;
    private int type = TEXT;

    private OnOkClickListener onOkClickListener;
    private OnCancelClickListener onCancelClickListener;

    public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    public void setOnOkClickListener(String ok, OnOkClickListener onOkClickListener) {
        okBtn.setText(ok);
        setOnOkClickListener(onOkClickListener);
    }

    public void setOnCancelClickListener(OnCancelClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    public void setOnCancelClickListener(String cancel, OnOkClickListener onOkClickListener) {
        cancelBtn.setText(cancel);
        setOnOkClickListener(onOkClickListener);
    }

    public SimpleDialog(Context context) {
        super(context, R.style.DefaultDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_simple);

        initView();
        initListener();

    }

    private void initListener() {
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onOkClickListener != null) {
                    onOkClickListener.onOkClick();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCancelClickListener != null) {
                    onCancelClickListener.onCancelClick();
                }
            }
        });
    }


    private void initView() {
        okBtn = findViewById(R.id.dialog_ok);
        cancelBtn = findViewById(R.id.dialog_cancel);
        titleText = findViewById(R.id.dialog_title);
        messageEdit = findViewById(R.id.dialog_edit_message);
        if (type == PASSWORD) {
            messageEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (!TextUtils.isEmpty(titleStr)) {
            titleText.setText(titleStr);
        } else {
            titleText.setText(titleResId);
        }
        if (!TextUtils.isEmpty(hintStr)) {
            messageEdit.setHint(hintStr);
        }

        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = (int)(ScreenUtils.getScreenWidth(context) * 0.8);
        getWindow().setAttributes(p);
    }

    public void setTitle(String title) {
        titleStr = title;
    }

    public void setTitle(@StringRes int titleResId) {
        this.titleResId = titleResId;
    }

    public void setMessageHint(String hint) {
        hintStr = hint;
    }

    public void setMessageHint(@StringRes int hint) {
        hintStr = context.getString(hint);
    }

    public String getMessage() {
        return messageEdit.getText().toString();
    }

    public void setEditInputType(int type) {
        this.type = type;
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public interface OnOkClickListener {
        void onOkClick();
    }

}
