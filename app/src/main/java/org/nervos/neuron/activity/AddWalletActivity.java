package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.nervos.neuron.R;

public class AddWalletActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        findViewById(R.id.create_wallet_button).setOnClickListener(v ->
                startActivity(new Intent(AddWalletActivity.this, CreateWalletActivity.class)));

        findViewById(R.id.import_wallet_button).setOnClickListener(v ->
                startActivity(new Intent(AddWalletActivity.this, ImportWalletActivity.class)));

    }
}
