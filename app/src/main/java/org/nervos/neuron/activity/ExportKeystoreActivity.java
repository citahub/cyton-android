package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;

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
        findViewById(R.id.export_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("keystore", keystore);
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(mActivity, "复制成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.export_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
