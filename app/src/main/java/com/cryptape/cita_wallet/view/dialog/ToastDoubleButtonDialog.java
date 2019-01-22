package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONObject;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.dialog.listener.OnDialogCancelClickListener;
import com.cryptape.cita_wallet.view.dialog.listener.OnDialogOKClickListener;
import com.cryptape.cita_wallet.util.ScreenUtils;

/**
 * Created by BaojunCZ on 2018/8/28.
 */
public class ToastDoubleButtonDialog extends Dialog {

    private TextView messageText, okText, cancelText;
    private JSONObject jsonInfo = null;
    private String info = null;
    private Context context;
    private OnDialogOKClickListener okClickListener = null;
    private OnDialogCancelClickListener cancelClickListener = null;

    private ToastDoubleButtonDialog(@NonNull Context context) {
        super(context, R.style.DefaultDialog);
        this.context = context;
    }

    public static ToastDoubleButtonDialog getInstance(@NonNull Context context, String msg) {
        ToastDoubleButtonDialog dialog = new ToastDoubleButtonDialog(context);
        dialog.show();
        dialog.setMsg(msg);
        return dialog;
    }

    /**
     * @param context
     * @param msg     object.put("info", "14124124");
     *                object.put("ok", "OK");
     *                object.put("cancel", "Cancel");
     * @return
     */
    public static ToastDoubleButtonDialog getInstance(@NonNull Context context, JSONObject msg) {
        ToastDoubleButtonDialog dialog = new ToastDoubleButtonDialog(context);
        dialog.show();
        dialog.setMsg(msg);
        return dialog;
    }

    public void setOnOkClickListener(OnDialogOKClickListener okClickListener) {
        this.okClickListener = okClickListener;
    }

    public void setOnCancelClickListener(OnDialogCancelClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_double_toast);

        initView();
        initAction();
    }

    private void initView() {
        messageText = findViewById(R.id.tv_msg);
        okText = findViewById(R.id.dialog_ok);
        cancelText = findViewById(R.id.dialog_cancel);
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = (int) (ScreenUtils.getScreenWidth(context) * 0.8);
        getWindow().setAttributes(p);
    }

    private void initData() {
        if (jsonInfo != null) {
            String info = jsonInfo.optString(ToastSingleButtonDialog.DIALOG_INFO);
            if (!TextUtils.isEmpty(info)) messageText.setText(info);
            String ok = jsonInfo.optString(ToastSingleButtonDialog.DIALOG_OK_BTN);
            if (!TextUtils.isEmpty(ok)) okText.setText(ok);
            String cancel = jsonInfo.optString(ToastSingleButtonDialog.DIALOG_CANCEL_BTN);
            if (!TextUtils.isEmpty(cancel)) cancelText.setText(cancel);
        }

        if (!TextUtils.isEmpty(info)) {
            messageText.setText(info);
        }
    }

    private void initAction() {
        okText.setOnClickListener(view -> {
            if (okClickListener != null) okClickListener.onClick(this);
            else dismiss();
        });
        cancelText.setOnClickListener(view -> {
            if (cancelClickListener != null) cancelClickListener.onClick(this);
            else dismiss();
        });
    }

    public void setMsg(String info) {
        this.info = info;
        initData();
    }

    public void setMsg(JSONObject jsonObject) {
        this.jsonInfo = jsonObject;
        initData();
    }

}
