package com.cryptape.cita_wallet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.activity.ImportWalletActivity;
import com.cryptape.cita_wallet.activity.QrCodeActivity;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.WalletTextWatcher;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.permission.PermissionUtil;
import com.cryptape.cita_wallet.util.permission.RuntimeRationale;
import com.cryptape.cita_wallet.util.qrcode.CodeUtils;
import com.cryptape.cita_wallet.util.qrcode.QRResultCheck;
import com.cryptape.cita_wallet.view.button.CommonButton;
import org.web3j.utils.Numeric;

import java.util.Objects;

/**
 * Created by duanyytop on 2018/5/8
 */
public class ImportPrivateKeyFragment extends NBaseFragment {

    private static final int REQUEST_CODE = 0x01;
    private AppCompatEditText mEtPrivateKey;
    private AppCompatEditText mEtWalletName;
    private AppCompatEditText mEtPassword;
    private AppCompatEditText mEtRePassword;
    private CommonButton mCbImport;
    private ImportWalletPresenter presenter;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_import_private_key;
    }

    @Override
    protected void initView() {
        mEtPrivateKey = (AppCompatEditText) findViewById(R.id.edit_wallet_private_key);
        mEtWalletName = (AppCompatEditText) findViewById(R.id.edit_wallet_name);
        mEtPassword = (AppCompatEditText) findViewById(R.id.edit_wallet_password);
        mEtRePassword = (AppCompatEditText) findViewById(R.id.edit_wallet_repassword);
        mCbImport = (CommonButton) findViewById(R.id.import_private_key_button);
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
        if (!TextUtils.isEmpty(ImportWalletActivity.PrivateKey)) {
            mEtPrivateKey.setText(ImportWalletActivity.PrivateKey);
            mEtPrivateKey.setSelection(ImportWalletActivity.PrivateKey.length());
            ImportWalletActivity.PrivateKey = "";
        }
    }

    @Override
    protected void initAction() {
        mCbImport.setOnClickListener(view -> {
            if (!QRResultCheck.isPrivateKey(mEtPrivateKey.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.wrong_private_key, Toast.LENGTH_SHORT).show();
            } else if (!NumberUtil.isPasswordOk(mEtPassword.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_weak, Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.equals(mEtPassword.getText().toString().trim(), mEtRePassword.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_not_same, Toast.LENGTH_SHORT).show();
            } else if (DBWalletUtil.checkWalletName(getContext(), mEtWalletName.getText().toString())) {
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            } else {
                presenter.importPrivateKey(Numeric.toBigInt(mEtPrivateKey.getText().toString().trim())
                        , mEtPassword.getText().toString().trim(), mEtWalletName.getText().toString().trim());
            }
        });
        findViewById(R.id.wallet_scan).setOnClickListener(v -> AndPermission.with(getActivity())
                .runtime()
                .permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                })
                .onDenied(permissions -> PermissionUtil.showSettingDialog(getActivity(), permissions))
                .start());
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3 && check4;
    }

    private boolean check1 = false, check2 = false, check3 = false, check4 = false;

    private void checkWalletStatus() {
        mEtWalletName.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check1 = !TextUtils.isEmpty(mEtWalletName.getText().toString().trim());
                mCbImport.setClickAble(isWalletValid());
            }
        });
        mEtPassword.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(mEtPassword.getText().toString().trim()) &&
                        mEtPassword.getText().toString().trim().length() >= 8;
                mCbImport.setClickAble(isWalletValid());
            }
        });

        mEtRePassword.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(mEtRePassword.getText().toString().trim()) &&
                        mEtRePassword.getText().toString().trim().length() >= 8;
                mCbImport.setClickAble(isWalletValid());
            }
        });
        mEtPrivateKey.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check4 = !TextUtils.isEmpty(mEtPrivateKey.getText().toString().trim());
                mCbImport.setClickAble(isWalletValid());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                        case CodeUtils.STRING_PRIVATE_KEY:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            mEtPrivateKey.setText(result);
                            break;
                        default:
                            Toast.makeText(getActivity(), R.string.private_key_error, Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
