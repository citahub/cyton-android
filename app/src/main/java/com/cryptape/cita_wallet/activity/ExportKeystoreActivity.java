package com.cryptape.cita_wallet.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita_wallet.R;

/**
 * Created by duanyytop on 2018/5/31
 */
public class ExportKeystoreActivity extends BaseActivity {

    public static final String EXTRA_KEYSTORE = "extra_keystore";

    private String keystore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_keystore);
        initView();
        initListener();
    }

    private void initView() {
        keystore = getIntent().getStringExtra(EXTRA_KEYSTORE);
        TextView keystoreText = findViewById(R.id.message_text);
        keystoreText.setText(keystore);
    }

    private void initListener() {
        findViewById(R.id.export_copy).setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("keystore", keystore);
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
                Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
