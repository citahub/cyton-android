package org.nervos.neuron.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.neuron.R;
import org.nervos.neuron.constant.SensorDataCons;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.event.CloseWalletInfoEvent;
import org.nervos.neuron.event.WalletSaveEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.button.CommonButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateWalletActivity extends NBaseActivity {

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static final String MnemonicPath = "m/44'/60'/0'/0/0";
    public static final String EXTRA_MNEMONIC = "extra_mnemonic";

    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private CommonButton createWalletCbt;
    private TitleBar titleBar;

    private WalletEntity walletEntity;
    private WalletItem walletItem;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_create_wallet;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initView() {
        walletNameEdit = findViewById(R.id.edit_wallet_name);
        passwordEdit = findViewById(R.id.edit_wallet_password);
        rePasswordEdit = findViewById(R.id.edit_wallet_password_repeat);
        createWalletCbt = findViewById(R.id.commonBtn);
        titleBar = findViewById(R.id.title);
    }

    @Override
    protected void initData() {
        checkWalletStatus();
        if (getIntent() != null && getIntent().getBooleanExtra(SplashActivity.EXTRA_FIRST, false)) {
            titleBar.hideLeft();
        }

        titleBar.setOnLeftClickListener(() -> {
            startActivity(new Intent(this, AddWalletActivity.class));
            finish();
        });
    }

    @Override
    protected void initAction() {
        createWalletCbt.setOnClickListener(view -> {
            if (!TextUtils.equals(passwordEdit.getText().toString().trim(), rePasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.password_not_same, Toast.LENGTH_SHORT).show();
            } else if (!NumberUtil.isPasswordOk(passwordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.password_weak, Toast.LENGTH_SHORT).show();
            } else if (DBWalletUtil.checkWalletName(mActivity, walletNameEdit.getText().toString())) {
                Toast.makeText(mActivity, R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            } else {
                showProgressBar(getString(R.string.wallet_creating));
                cachedThreadPool.execute(() -> {
                    saveWalletInfo();
                    rePasswordEdit.post(() -> {
                        try {
                            JSONObject object = new JSONObject();
                            object.put(SensorDataCons.INSTANCE.getTAG_CREATE_WALLET_ADDRESS(), walletEntity.getAddress());
                            SensorsDataAPI.sharedInstance().track(SensorDataCons.INSTANCE.getTRACK_CREATE_WALLET(), object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dismissProgressBar();
                        Intent intent = new Intent(CreateWalletActivity.this, BackupMnemonicActivity.class);
                        intent.putExtra(EXTRA_MNEMONIC, walletEntity.getMnemonic());
                        startActivity(intent);
                    });
                });
            }
        });
    }

    /**
     * save wallet information to database and add default eth token
     */
    private void saveWalletInfo() {
        String password = rePasswordEdit.getText().toString().trim();
        walletEntity = WalletEntity.createWithMnemonic(MnemonicPath, password);
        walletItem = WalletItem.fromWalletEntity(walletEntity);
        walletItem.name = walletNameEdit.getText().toString().trim();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(WalletSaveEvent event) {
        if (walletItem != null) {
            walletItem = DBWalletUtil.addOriginTokenToWallet(mActivity, walletItem);
            DBWalletUtil.saveWallet(mActivity, walletItem);
            SharePrefUtil.putCurrentWalletName(walletItem.name);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseWalletEvent(CloseWalletInfoEvent event) {
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AddWalletActivity.class));
        finish();
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3;
    }

    private boolean check1 = false, check2 = false, check3 = false;

    private void checkWalletStatus() {
        walletNameEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check1 = !TextUtils.isEmpty(walletNameEdit.getText().toString().trim());
                createWalletCbt.setClickAble(isWalletValid());
            }
        });
        passwordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim()) &&
                        passwordEdit.getText().toString().trim().length() >= 8;
                createWalletCbt.setClickAble(isWalletValid());
            }
        });
        rePasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(rePasswordEdit.getText().toString().trim()) &&
                        rePasswordEdit.getText().toString().trim().length() >= 8;
                createWalletCbt.setClickAble(isWalletValid());
            }
        });
    }


    private static class WalletTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
