package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.KeyEvent;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.util.db.DBWalletUtil;

public class AddWalletActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        findViewById(R.id.create_wallet_button).setOnClickListener(v -> {
            startActivity(new Intent(mActivity, CreateWalletActivity.class));
        });

        findViewById(R.id.import_wallet_button).setOnClickListener(v ->
                startActivity(new Intent(mActivity, ImportWalletActivity.class)));

        ((TitleBar)findViewById(R.id.title)).setOnLeftClickListener(() -> goBack());

    }

    private void goBack() {
        if (DBWalletUtil.getCurrentWallet(mActivity) == null) {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAG, AppFragment.TAG);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            goBack();
        }
        return super.onKeyDown(keyCode, event);
    }
}
