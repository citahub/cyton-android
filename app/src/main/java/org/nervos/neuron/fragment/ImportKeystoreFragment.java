package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.ConfirmMnemonicActivity;
import org.nervos.neuron.activity.ImportFingerTipActivity;
import org.nervos.neuron.activity.ImportWalletActivity;
import org.nervos.neuron.activity.MainActivity;
import org.nervos.neuron.activity.QrCodeActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.FingerPrint.FingerPrintController;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.nervos.neuron.fragment.wallet.view.WalletsFragment;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.QRUtils.CodeUtils;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.view.button.CommonButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportKeystoreFragment extends NBaseFragment {

    private static final int REQUEST_CODE = 0x01;
    private AppCompatEditText keystoreEdit;
    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private CommonButton importButton;
    private ImageView scanImage;

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_import_keystore;
    }

    @Override
    protected void initView() {
        super.initView();
        keystoreEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_keystore);
        walletNameEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_name);
        passwordEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_password);
        importButton = (CommonButton) findViewById(R.id.import_keystore_button);
        scanImage = (ImageView) findViewById(R.id.wallet_scan);
    }

    @Override
    protected void initData() {
        super.initData();
        checkWalletStatus();
        if (!TextUtils.isEmpty(ImportWalletActivity.KeyStore)) {
            keystoreEdit.setText(ImportWalletActivity.KeyStore);
            keystoreEdit.setSelection(ImportWalletActivity.KeyStore.length());
            ImportWalletActivity.KeyStore = "";
        }
    }

    @Override
    protected void initAction() {
        importButton.setOnClickListener(view -> {
            if (DBWalletUtil.checkWalletName(getContext(), walletNameEdit.getText().toString())) {
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
                return;
            }
            cachedThreadPool.execute(() -> generateAndSaveWallet());
        });
        scanImage.setOnClickListener(v -> AndPermission.with(getActivity())
                .runtime().permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                    intent.putExtra(QrCodeActivity.SHOW_RIGHT, false);
                    startActivityForResult(intent, REQUEST_CODE);
                })
                .onDenied(permissions -> PermissionUtil.showSettingDialog(getActivity(), permissions))
                .start());
    }


    private void generateAndSaveWallet() {
        passwordEdit.post(() -> showProgressBar(R.string.wallet_importing));
        WalletEntity walletEntity;
        try {
            walletEntity = WalletEntity.fromKeyStore(passwordEdit.getText().toString().trim(),
                    keystoreEdit.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            passwordEdit.post(() -> {
                ImportWalletActivity.track("1", false, "");
                dismissProgressBar();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }
        if (walletEntity == null || DBWalletUtil.checkWalletAddress(getContext(), walletEntity.getCredentials().getAddress())) {
            passwordEdit.post(() -> {
                dismissProgressBar();
                Toast.makeText(getContext(), R.string.wallet_address_exist, Toast.LENGTH_SHORT).show();
                ImportWalletActivity.track("1", false, "");
            });
            return;
        }
        WalletItem walletItem = WalletItem.fromWalletEntity(walletEntity);
        walletItem.name = walletNameEdit.getText().toString().trim();
        walletItem = DBWalletUtil.addOriginTokenToWallet(getContext(), walletItem);
        DBWalletUtil.saveWallet(getContext(), walletItem);
        SharePrefUtil.putCurrentWalletName(walletItem.name);
        ImportWalletActivity.track("1", true, walletEntity.getAddress());
        passwordEdit.post(() -> {
            Toast.makeText(getContext(), R.string.wallet_export_success, Toast.LENGTH_SHORT).show();
            dismissProgressBar();
            if (FingerPrintController.getInstance(getActivity()).isSupportFingerprint() && !SharePrefUtil.getBoolean(ConstUtil.FINGERPRINT, false) &&
                    !SharePrefUtil.getBoolean(ConstUtil.FINGERPRINT_TIP, false)) {
                Intent intent = new Intent(getActivity(), ImportFingerTipActivity.class);
                startActivity(intent);
                SharePrefUtil.putBoolean(ConstUtil.FINGERPRINT_TIP, true);
            } else {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_TAG, WalletsFragment.TAG);
                getActivity().startActivity(intent);
            }
            EventBus.getDefault().post(new TokenRefreshEvent());
            getActivity().finish();
        });
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
                importButton.setClickAble(isWalletValid());
            }
        });
        passwordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim());
                importButton.setClickAble(isWalletValid());
            }
        });
        keystoreEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(keystoreEdit.getText().toString().trim());
                importButton.setClickAble(isWalletValid());
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
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) return;
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                        case CodeUtils.STRING_KEYSTORE:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            keystoreEdit.setText(result);
                            break;
                        default:
                            Toast.makeText(getActivity(), R.string.keystore_error, Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    QrCodeActivity.track("3", false);
                    Toast.makeText(getActivity(), R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
