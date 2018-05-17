package org.nervos.neuron.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.DBUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.CipherException;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportPrivateKeyFragment extends BaseFragment {

    private AppCompatEditText privateKeyEdit;
    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private AppCompatCheckBox checkBox;
    private AppCompatButton importButton;

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_private_key, container, false);
        privateKeyEdit = view.findViewById(R.id.edit_wallet_private_key);
        walletNameEdit = view.findViewById(R.id.edit_wallet_name);
        passwordEdit = view.findViewById(R.id.edit_wallet_password);
        rePasswordEdit = view.findViewById(R.id.edit_wallet_repassword);
        checkBox = view.findViewById(R.id.wallet_checkbox);
        importButton = view.findViewById(R.id.import_private_key_button);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListener();
        checkWalletStatus();
    }


    private void initListener() {
        importButton.setOnClickListener(view -> {
            if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                    rePasswordEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            } else {
                showProgressBar("钱包导入中...");
                cachedThreadPool.execute(() -> {
                    generateAndSaveWallet();
                    passwordEdit.post(() -> {
                        dismissProgressBar();
                    });
                });
            }
        });
    }


    private void generateAndSaveWallet() {
        try {
            String privkey = privateKeyEdit.getText().toString().trim();
            WalletEntity walletEntity = WalletEntity.fromPrivateKey(Numeric.toBigInt(privkey));
            WalletItem walletItem = WalletItem.fromWalletEntity(walletEntity);
            walletItem.name = walletNameEdit.getText().toString().trim();
            walletItem.password = passwordEdit.getText().toString().trim();
            DBUtil.saveWallet(getContext(), walletItem);
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }


    private boolean isWalletValid() {
        return check1 && check2 && check3 && check4;
    }

    private void setCreateButtonStatus(boolean status) {
        importButton.setBackgroundResource(status?
                R.drawable.button_corner_blue_shape:R.drawable.button_corner_gray_shape);
        importButton.setEnabled(status);
    }


    private boolean check1 = false, check2 = false, check3 = false, check4 = false;
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

        passwordEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim());
                setCreateButtonStatus(isWalletValid());
            }
        });

        checkBox.setOnCheckedChangeListener((compoundButton, b) ->{
            check4 = b;
            setCreateButtonStatus(isWalletValid());
        } );
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
