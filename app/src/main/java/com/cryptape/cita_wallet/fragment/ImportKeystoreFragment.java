package com.cryptape.cita_wallet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.runtime.PermissionRequest;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.activity.ImportWalletActivity;
import com.cryptape.cita_wallet.activity.QrCodeActivity;
import com.cryptape.cita_wallet.util.WalletTextWatcher;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.permission.PermissionUtil;
import com.cryptape.cita_wallet.util.permission.RuntimeRationale;
import com.cryptape.cita_wallet.util.qrcode.CodeUtils;
import com.cryptape.cita_wallet.view.button.CommonButton;

import java.util.Objects;

/**
 * Created by duanyytop on 2018/5/8
 */
public class ImportKeystoreFragment extends NBaseFragment {

    private static final int REQUEST_CODE = 0x01;
    private AppCompatEditText mEtKeystore;
    private AppCompatEditText mEtWalletName;
    private AppCompatEditText mEtPassword;
    private CommonButton mIbtImport;
    private ImportWalletPresenter presenter;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_import_keystore;
    }

    @Override
    protected void initView() {
        mEtKeystore = (AppCompatEditText) findViewById(R.id.edit_wallet_keystore);
        mEtWalletName = (AppCompatEditText) findViewById(R.id.edit_wallet_name);
        mEtPassword = (AppCompatEditText) findViewById(R.id.edit_wallet_password);
        mIbtImport = (CommonButton) findViewById(R.id.import_keystore_button);
    }

    @Override
    protected void initData() {
        presenter = new ImportWalletPresenter(Objects.requireNonNull(getActivity()), show -> {
            if (show) {
                showProgressBar();
            } else {
                dismissProgressBar();
            }
            return null;
        });
        checkWalletStatus();
        if (!TextUtils.isEmpty(ImportWalletActivity.KeyStore)) {
            mEtKeystore.setText(ImportWalletActivity.KeyStore);
            mEtKeystore.setSelection(ImportWalletActivity.KeyStore.length());
            ImportWalletActivity.KeyStore = "";
        }
    }

    @Override
    protected void initAction() {
        mIbtImport.setOnClickListener(view -> {
            if (DBWalletUtil.checkWalletName(getContext(), mEtWalletName.getText().toString())) {
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
                return;
            }
            presenter.importKeystore(mEtKeystore.getText().toString().trim(), mEtPassword.getText().toString().trim()
                    , mEtWalletName.getText().toString().trim());
        });
        findViewById(R.id.wallet_scan).setOnClickListener(v -> {
            PermissionRequest request = AndPermission.with(getActivity()).runtime().permission(Permission.Group.CAMERA);
            request.rationale(new RuntimeRationale()).onGranted(permissions -> {
                Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                intent.putExtra(QrCodeActivity.SHOW_RIGHT, false);
                startActivityForResult(intent, REQUEST_CODE);
            });
            request.onDenied(permissions -> PermissionUtil.showSettingDialog(getActivity(), permissions));
            request.start();
        });
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3;
    }

    private boolean check1 = false, check2 = false, check3 = false;

    private void checkWalletStatus() {
        mEtWalletName.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check1 = !TextUtils.isEmpty(mEtWalletName.getText().toString().trim());
                mIbtImport.setClickAble(isWalletValid());
            }
        });
        mEtPassword.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(mEtPassword.getText().toString().trim());
                mIbtImport.setClickAble(isWalletValid());
            }
        });
        mEtKeystore.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(mEtKeystore.getText().toString().trim());
                mIbtImport.setClickAble(isWalletValid());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) return;
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                        case CodeUtils.STRING_KEYSTORE:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            mEtKeystore.setText(result);
                            break;
                        default:
                            Toast.makeText(getActivity(), R.string.keystore_error, Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
