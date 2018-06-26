package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
                    Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.export_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_keystore));
                intent.putExtra(Intent.EXTRA_TEXT, keystore);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getString(R.string.share_keystore)));
            }
        });
    }
}
