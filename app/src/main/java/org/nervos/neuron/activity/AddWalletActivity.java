package org.nervos.neuron.activity;

import android.content.Intent;
import android.view.KeyEvent;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.util.db.DBWalletUtil;

public class AddWalletActivity extends NBaseActivity {

    @Override
    protected int getContentLayout() {
        return R.layout.activity_add_wallet;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        findViewById(R.id.create_wallet_button).setOnClickListener(v -> {
            startActivity(new Intent(mActivity, CreateWalletActivity.class));
        });

        findViewById(R.id.import_wallet_button).setOnClickListener(v ->
                startActivity(new Intent(mActivity, ImportWalletActivity.class)));
        TitleBar titleBar = findViewById(R.id.title);
        if (getIntent().getBooleanExtra("isFirst", false)) {
            titleBar.hideLeft();
        } else {
            titleBar.showLeft();
            titleBar.setOnLeftClickListener(() -> goBack());
        }

    }

    @Override
    protected void initAction() {

    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
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
            if (!getIntent().getBooleanExtra("isFirst", false))
                goBack();
        }
        return super.onKeyDown(keyCode, event);
    }
}
