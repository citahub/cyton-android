package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.DBWalletUtil;
import org.nervos.neuron.util.SharePrefUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.CipherException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportKeystoreFragment extends BaseFragment {

    private static final int REQUEST_CODE = 0x01;
    private AppCompatEditText keystoreEdit;
    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatButton importButton;
    private ImageView scanImage;

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_keystore, container, false);
        keystoreEdit = view.findViewById(R.id.edit_wallet_keystore);
        walletNameEdit = view.findViewById(R.id.edit_wallet_name);
        passwordEdit = view.findViewById(R.id.edit_wallet_password);
        importButton = view.findViewById(R.id.import_keystore_button);
        scanImage = view.findViewById(R.id.wallet_scan);
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
            showProgressBar("钱包导入中...");
            cachedThreadPool.execute(() -> {
                generateAndSaveWallet();
                passwordEdit.post(this::dismissProgressBar);
            });
        });
        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }


    private void generateAndSaveWallet() {
        try {
            WalletEntity walletEntity = WalletEntity.fromKeyStore(passwordEdit.getText().toString().trim(),
                    keystoreEdit.getText().toString().trim());
            WalletItem walletItem = WalletItem.fromWalletEntity(walletEntity);
            walletItem.name = walletNameEdit.getText().toString().trim();
            walletItem.password = passwordEdit.getText().toString().trim();
            DBWalletUtil.saveWallet(getContext(), walletItem);
            SharePrefUtil.putWalletName(walletItem.name);
        } catch (CipherException e) {
            e.printStackTrace();
        }
    }


    private boolean isWalletValid() {
        return check1 && check2;
    }

    private void setCreateButtonStatus(boolean status) {
        importButton.setBackgroundResource(status?
                R.drawable.button_corner_blue_shape:R.drawable.button_corner_gray_shape);
        importButton.setEnabled(status);
    }


    private boolean check1 = false, check2 = false;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    keystoreEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
