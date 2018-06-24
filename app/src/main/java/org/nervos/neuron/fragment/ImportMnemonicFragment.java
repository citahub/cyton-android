package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.MainActivity;
import org.nervos.neuron.activity.QrCodeActivity;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.CipherException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportMnemonicFragment extends BaseFragment {

    private static final int REQUEST_CODE = 0x01;

    List<String> formats;
    List<String> paths;
    String currentPath;
    AppCompatSpinner spinner;
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private AppCompatEditText mnemonicEdit;
    private AppCompatButton importButton;
    private ImageView scanImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_mnemonic, container, false);
        spinner = view.findViewById(R.id.spinner_format);
        importButton = view.findViewById(R.id.import_mnemonic_button);
        walletNameEdit = view.findViewById(R.id.edit_wallet_name);
        passwordEdit = view.findViewById(R.id.edit_wallet_password);
        rePasswordEdit = view.findViewById(R.id.edit_wallet_repassword);
        mnemonicEdit = view.findViewById(R.id.edit_wallet_mnemonic);
        scanImage = view.findViewById(R.id.wallet_scan);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        checkWalletStatus();
        initListener();
    }

    private void initView() {
        formats = Arrays.asList(getResources().getStringArray(R.array.mnemonic_format));
        paths = Arrays.asList(getResources().getStringArray(R.array.mnemonic_path));
        currentPath = paths.get(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, formats);
        spinner.setAdapter(adapter);

    }

    private void initListener() {
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NumberUtil.isPasswordOk(passwordEdit.getText().toString().trim())) {
                    Toast.makeText(getContext(), "您的密码太弱，请重新输入", Toast.LENGTH_SHORT).show();
                } else if (!TextUtils.equals(passwordEdit.getText().toString().trim(),
                        rePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(getContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else if (DBWalletUtil.checkWalletName(getContext(), walletNameEdit.getText().toString().trim())){
                    Toast.makeText(getContext(), "该钱包名称已存在", Toast.LENGTH_SHORT).show();
                } else {
                    cachedThreadPool.execute(() -> generateAndSaveWallet());
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPath = paths.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        scanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(getActivity())
                    .runtime().permission(Permission.Group.CAMERA)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                    })
                    .onDenied(permissions -> PermissionUtil.showSettingDialog(getActivity(), permissions))
                    .start();
            }
        });

    }

    private void generateAndSaveWallet() {
        passwordEdit.post(() -> showProgressBar(R.string.wallet_importing));
        WalletEntity walletEntity;
        try {
            walletEntity = WalletEntity.fromMnemonic(
                    mnemonicEdit.getText().toString().trim(), currentPath);
        } catch (CipherException e) {
            e.printStackTrace();
            passwordEdit.post(() -> {
                dismissProgressBar();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return;
        }
        if (walletEntity == null || DBWalletUtil.checkWalletAddress(getContext(), walletEntity.getAddress())) {
            passwordEdit.post(() -> {
                dismissProgressBar();
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            });
            return;
        }
        WalletItem walletItem = WalletItem.fromWalletEntity(
                passwordEdit.getText().toString().trim(), walletEntity);
        walletItem.name = walletNameEdit.getText().toString().trim();
        walletItem = DBWalletUtil.addOriginTokenToWallet(getContext(), walletItem);
        DBWalletUtil.saveWallet(getContext(), walletItem);
        SharePrefUtil.putCurrentWalletName(walletItem.name);
        passwordEdit.post(() -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAG, WalletFragment.TAG);
            startActivity(intent);
            Toast.makeText(getContext(), R.string.wallet_export_success, Toast.LENGTH_SHORT).show();
            dismissProgressBar();
        });
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
        mnemonicEdit.addTextChangedListener(new WalletTextWatcher(){
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check4 = !TextUtils.isEmpty(mnemonicEdit.getText().toString().trim());
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
                    mnemonicEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getActivity(), R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
