package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.nervos.neuron.R;

public class BackupMnemonicActivity extends AppCompatActivity{

    private TextView mnemonicText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_mnemonic);

        mnemonicText = findViewById(R.id.mnemonic_text);
        mnemonicText.setText(getIntent().getStringExtra(CreateWalletActivity.EXTRA_MNEMONIC));
    }
}
