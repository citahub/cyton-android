package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.util.crypto.WalletEntity;

import org.web3j.crypto.CipherException;

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

    private WalletEntity mWalletEntity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        initView();
        checkWalletStatus();
        initListener();
    }

    private void initView() {
        walletNameEdit = findViewById(R.id.edit_wallet_name);
        passwordEdit = findViewById(R.id.edit_wallet_password);
        rePasswordEdit = findViewById(R.id.edit_wallet_password_repeat);
        createWalletButton = findViewById(R.id.create_wallet_button);
    }

    private void initListener() {
        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                        rePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(CreateWalletActivity.this, "两次输入的密码不一致",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showProgressBar("钱包创建中...");
                    cachedThreadPool.execute(() -> {
                        mWalletEntity = WalletEntity.createWithMnemonic(
                                passwordEdit.getText().toString().trim(), MnemonicPath);
                        rePasswordEdit.post(() -> {
                            dismissProgressBar();
                            Intent intent = new Intent(CreateWalletActivity.this,
                                    BackupMnemonicActivity.class);
                            intent.putExtra(EXTRA_MNEMONIC, mWalletEntity.getMnemonic());
                            startActivity(intent);
                        });
                    });
                }
            }
        });

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
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim());
                setCreateButtonStatus(isWalletValid());
            }
        });
        rePasswordEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(rePasswordEdit.getText().toString().trim());
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
