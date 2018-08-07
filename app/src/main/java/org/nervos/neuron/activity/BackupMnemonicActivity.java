package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.event.CloseWalletInfoEvent;

public class BackupMnemonicActivity extends NBaseActivity {

    private TextView mnemonicText;
    private String mnemonic;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_backup_mnemonic;
    }

    @Override
    protected void initView() {
        mnemonicText = findViewById(R.id.mnemonic_text);
    }

    @Override
    protected void initData() {
        mnemonic = getIntent().getStringExtra(CreateWalletActivity.EXTRA_MNEMONIC);
        mnemonicText.setText(mnemonic);
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

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
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
