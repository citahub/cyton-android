package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.event.CloseWalletInfoEvent;
import org.nervos.neuron.event.WalletSaveEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.util.db.SharePrefUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateWalletActivity extends BaseActivity {

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static final String MnemonicPath = "m/44'/60'/0'/0/0";
    public static final String EXTRA_MNEMONIC = "extra_mnemonic";

    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private AppCompatButton createWalletButton;
    private TitleBar titleBar;

    private WalletEntity walletEntity;
    private WalletItem walletItem;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);
        EventBus.getDefault().register(this);
        initView();
        checkWalletStatus();
        initListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        walletNameEdit = findViewById(R.id.edit_wallet_name);
        passwordEdit = findViewById(R.id.edit_wallet_password);
        rePasswordEdit = findViewById(R.id.edit_wallet_password_repeat);
        createWalletButton = findViewById(R.id.create_wallet_button);
        titleBar = findViewById(R.id.title);
        if (getIntent() != null &&
                getIntent().getBooleanExtra(SplashActivity.EXTRA_FIRST, false)) {
            titleBar.hideLeft();
        }
    }

    private void initListener() {
        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NumberUtil.isPasswordOk(passwordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, R.string.password_weak, Toast.LENGTH_SHORT).show();
                } else if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                        rePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, R.string.password_not_same, Toast.LENGTH_SHORT).show();
                } else if (DBWalletUtil.checkWalletName(mActivity, walletNameEdit.getText().toString().trim())){
                    Toast.makeText(mActivity, R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
                } else {
                    final String password = rePasswordEdit.getText().toString().trim();
                    showProgressBar(getString(R.string.wallet_creating));
                    cachedThreadPool.execute(() -> {
                        saveWalletInfo(password);
                        rePasswordEdit.post(() -> {
                            dismissProgressBar();
                            Intent intent = new Intent(CreateWalletActivity.this,
                                    BackupMnemonicActivity.class);

                            intent.putExtra(EXTRA_MNEMONIC, walletEntity.getMnemonic());
                            startActivity(intent);
                        });
                    });
                }
            }
        });
    }

    /**
     * save wallet information to database and add default eth token
     */
    private void saveWalletInfo(String password){
        walletEntity = WalletEntity.createWithMnemonic(
                passwordEdit.getText().toString().trim(), MnemonicPath);
        new Thread(){
            @Override
            public void run() {
                super.run();
                walletItem = WalletItem.fromWalletEntity(password, walletEntity);
                walletItem.name = walletNameEdit.getText().toString().trim();
            }
        }.start();
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


    private boolean isWalletValid() {
        return check1 && check2 && check3;
    }

    private void setCreateButtonStatus(boolean status) {
        createWalletButton.setBackgroundResource(status?
                R.drawable.button_corner_blue_shape:R.drawable.button_corner_gray_shape);
        createWalletButton.setEnabled(status);
    }


    private boolean check1 = false, check2 = false, check3 = false;
    private void checkWalletStatus() {
        walletNameEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check1 = !TextUtils.isEmpty(walletNameEdit.getText().toString().trim());
                setCreateButtonStatus(isWalletValid());
            }
        });
        passwordEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim())
                        && passwordEdit.getText().toString().trim().length() >= 8;
                setCreateButtonStatus(isWalletValid());
            }
        });
        rePasswordEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(rePasswordEdit.getText().toString().trim())
                        && rePasswordEdit.getText().toString().trim().length() >= 8;
                setCreateButtonStatus(isWalletValid());
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
