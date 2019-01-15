package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;

public class ExportDialog extends Dialog {

    private TextView copyBtn;
    private TextView shareBtn;
    private TextView titleText;
    private TextView messageText;

    private OnCopyClickListener onCopyClickListener;
    private OnShareClickListener onShareClickListener;

    private String titleStr;
    private String messageStr;

    public void setOnCopyClickListener(OnCopyClickListener onCopyClickListener) {
        this.onCopyClickListener = onCopyClickListener;
    }

    public void setOnCopyClickListener(String copy, OnCopyClickListener onCopyClickListener) {
        copyBtn.setText(copy);
        setOnCopyClickListener(onCopyClickListener);
    }

    public void setOnShareClickListener(OnShareClickListener onShareClickListener) {
        this.onShareClickListener = onShareClickListener;
    }

    public void setOnCancelClickListener(String share, OnShareClickListener onShareClickListener) {
        shareBtn.setText(share);
        setOnShareClickListener(onShareClickListener);
    }

    public ExportDialog(Context context) {
        super(context, R.style.InputDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_export);

        initView();
        initListener();

    }

    private void initListener() {
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCopyClickListener != null) {
                    onCopyClickListener.onCopyClick();
                }
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShareClickListener != null) {
                    onShareClickListener.onShareClick();
                }
            }
        });
    }


    private void initView() {
        copyBtn = findViewById(R.id.dialog_copy);
        shareBtn = findViewById(R.id.dialog_share);
        titleText = findViewById(R.id.dialog_title);
        messageText = findViewById(R.id.message_text);

        if (!TextUtils.isEmpty(titleStr)) {
            titleText.setText(titleStr);
        }
        if (!TextUtils.isEmpty(messageStr)) {
            messageText.setText(messageStr);
        }
    }

    public void setTitle(String title) {
        titleStr = title;
    }

    public String getMessage() {
        return messageText.getText().toString().trim();
    }

    public void setMessage(String message) {
        messageStr = message;
    }

    public interface OnCopyClickListener {
        void onCopyClick();
    }

    public interface OnShareClickListener {
        void onShareClick();
    }

}
