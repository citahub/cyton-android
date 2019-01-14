package com.cryptape.cita_wallet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.dialog.listener.OnDialogOKClickListener;
import com.cryptape.cita_wallet.util.ScreenUtils;

/**
 * Created by BaojunCZ on 2018/8/28.
 */
public class ToastSingleButtonDialog extends Dialog {

    public static final String DIALOG_TITLE = "title";
    public static final String DIALOG_INFO = "info";
    public static final String DIALOG_OK_BTN = "ok";
    public static final String DIALOG_CANCEL_BTN = "cancel";

    private TextView messageText, okText, titleText;
    private JSONObject jsonInfo = null;
    private String info = null;
    private Context context;
    private OnDialogOKClickListener okClickListener = null;

    private ToastSingleButtonDialog(@NonNull Context context) {
        super(context, R.style.DefaultDialog);
        this.context = context;
    }

    public static ToastSingleButtonDialog getInstance(@NonNull Context context, String msg) {
        ToastSingleButtonDialog dialog = new ToastSingleButtonDialog(context);
        dialog.show();
        dialog.setMsg(msg);
        return dialog;
    }

    public static ToastSingleButtonDialog getInstance(@NonNull Context context, String title, String msg) {
        ToastSingleButtonDialog dialog = new ToastSingleButtonDialog(context);
        dialog.show();
        dialog.setMsg(title, msg);
        return dialog;
    }

    /**
     * @param context
     * @param msg     object.put("info", "14124124");
     *                object.put("ok", "OK");
     * @return
     */
    public static ToastSingleButtonDialog getInstance(@NonNull Context context, JSONObject msg) {
        ToastSingleButtonDialog dialog = new ToastSingleButtonDialog(context);
        dialog.show();
        dialog.setMsg(msg);
        return dialog;
    }


    public void setOnCancelClickListener(OnDialogOKClickListener okClickListener) {
        this.okClickListener = okClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_single_toast);

        initView();
        initAction();
    }

    private void initView() {
        messageText = findViewById(R.id.tv_msg);
        okText = findViewById(R.id.tv_ok);
        titleText = findViewById(R.id.tv_title);
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = (int) (ScreenUtils.getScreenWidth(context) * 0.8);
        getWindow().setAttributes(p);
    }

    private void initData() {
        if (jsonInfo != null) {
            String title = jsonInfo.optString(DIALOG_TITLE);
            if (!TextUtils.isEmpty(title)) titleText.setText(title);
            String info = jsonInfo.optString(DIALOG_INFO);
            if (!TextUtils.isEmpty(info)) messageText.setText(info);
            String ok = jsonInfo.optString(DIALOG_OK_BTN);
            if (!TextUtils.isEmpty(ok)) okText.setText(ok);
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
    }

    public void setMsg(String info) {
        this.info = info;
        initData();
    }

    public void setMsg(String title, String msg) {
        try {
            JSONObject object = new JSONObject();
            object.put(DIALOG_TITLE, title);
            object.put(DIALOG_INFO, msg);
            this.jsonInfo = object;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initData();
    }

    public void setMsg(JSONObject jsonObject) {
        this.jsonInfo = jsonObject;
        initData();
    }

}
