package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.nervos.neuron.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BackupMnemonicActivity extends AppCompatActivity{

    private TextView mnemonicText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_mnemonic);

        mnemonicText = findViewById(R.id.mnemonic_text);
        String mnemonic = getIntent().getStringExtra(CreateWalletActivity.EXTRA_MNEMONIC);
        String[] mnemonicList = mnemonic.split(" ");

        mnemonicText.setText(mnemonic);

        findViewById(R.id.backup_next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BackupMnemonicActivity.this, ConfirmMnemonicActivity.class);
                intent.putExtra(CreateWalletActivity.EXTRA_MNEMONIC, mnemonicList);
                startActivity(intent);
            }
        });
    }
}
