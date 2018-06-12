package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.event.WalletSaveEvent;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.EthNativeRpcService;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.crypto.WalletEntity;

import java.util.ArrayList;
import java.util.List;
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

        initView();
        checkWalletStatus();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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
                if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                        rePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else if (DBWalletUtil.checkWalletName(mActivity, walletNameEdit.getText().toString().trim())){
                    Toast.makeText(mActivity, "该钱包名称已存在", Toast.LENGTH_SHORT).show();
                } else {
                    showProgressBar("钱包创建中...");
                    cachedThreadPool.execute(() -> {
                        saveWalletInfo();
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
    private void saveWalletInfo(){
        walletEntity = WalletEntity.createWithMnemonic(
                passwordEdit.getText().toString().trim(), MnemonicPath);
        new Thread(){
            @Override
            public void run() {
                super.run();
                walletItem = WalletItem.fromWalletEntity(walletEntity);
                walletItem.name = walletNameEdit.getText().toString().trim();
                walletItem.password = passwordEdit.getText().toString().trim();
                List<TokenItem> tokenItemList = new ArrayList<>();
                tokenItemList.add(EthNativeRpcService.getDefaultEth(walletItem.address));
                walletItem.tokenItems = tokenItemList;
            }
        }.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(WalletSaveEvent event) {
        if (walletItem != null) {
            DBWalletUtil.saveWallet(mActivity, walletItem);
        }
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
