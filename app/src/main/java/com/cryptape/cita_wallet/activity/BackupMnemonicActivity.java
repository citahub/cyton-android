package com.cryptape.cita_wallet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.event.CloseWalletInfoEvent;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.dialog.ToastDoubleButtonDialog;
import com.cryptape.cita_wallet.view.dialog.ToastSingleButtonDialog;

/**
 * Created by duanyytop on 2018/5/8
 */
public class BackupMnemonicActivity extends NBaseActivity {

    private TextView mnemonicText;
    private String mnemonic;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_backup_mnemonic;
    }

    @Override
    protected void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        mnemonicText = findViewById(R.id.mnemonic_text);
    }

    @Override
    protected void initData() {
        ToastSingleButtonDialog.getInstance(this, getString(R.string.forbidden_screen_shoot), getString(R.string
                .forbidden_screen_shoot_backup));
        mnemonic = getIntent().getStringExtra(CreateWalletActivity.EXTRA_MNEMONIC);
        mnemonicText.setText(mnemonic);

        TitleBar titleBar = findViewById(R.id.title);
        titleBar.setOnLeftClickListener(() -> {
            try {
                JSONObject object = new JSONObject();
                object.put(ToastSingleButtonDialog.DIALOG_INFO, getString(R.string.backup_mnemonic_back_tips));
                object.put(ToastSingleButtonDialog.DIALOG_CANCEL_BTN, getString(R.string.reject));
                ToastDoubleButtonDialog dialog = ToastDoubleButtonDialog.getInstance(this, object);
                dialog.setOnCancelClickListener(dialog1 -> {
                    dialog1.dismiss();
                    finish();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void initAction() {
        String[] mnemonicList = mnemonic.split(" ");
        findViewById(R.id.backup_next_button).setOnClickListener(v -> {
            Intent intent = new Intent(BackupMnemonicActivity.this, ConfirmMnemonicActivity.class);
            intent.putExtra(CreateWalletActivity.EXTRA_MNEMONIC, mnemonicList);
            startActivity(intent);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseWalletEvent(CloseWalletInfoEvent event) {
        finish();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(mActivity);
    }
}
