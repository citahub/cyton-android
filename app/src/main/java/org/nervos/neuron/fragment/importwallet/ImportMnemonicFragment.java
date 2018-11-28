package org.nervos.neuron.fragment.importwallet;

import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.widget.Toast;
import org.nervos.neuron.R;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.WalletTextWatcher;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.button.CommonButton;

import java.util.Arrays;
import java.util.List;

/**
 * Created by duanyytop on 2018/5/8
 */
public class ImportMnemonicFragment extends NBaseFragment {

    List<String> paths;

    private AppCompatEditText walletNameEdit;
    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;
    private AppCompatEditText mnemonicEdit;
    private CommonButton importButton;
    private ImportWalletPresenter presenter;

    private boolean check1 = false, check2 = false, check3 = false, check4 = false;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_import_mnemonic;
    }

    @Override
    protected void initView() {
        importButton = (CommonButton) findViewById(R.id.import_mnemonic_button);
        walletNameEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_name);
        passwordEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_password);
        rePasswordEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_repassword);
        mnemonicEdit = (AppCompatEditText) findViewById(R.id.edit_wallet_mnemonic);
    }

    @Override
    protected void initData() {
        paths = Arrays.asList(getResources().getStringArray(R.array.mnemonic_path));
        presenter = new ImportWalletPresenter(getActivity(), show -> {
            if (show) showProgressBar();
            else dismissProgressBar();
            return null;
        });
    }

    @Override
    protected void initAction() {
        importButton.setOnClickListener(v -> {
            if (!NumberUtil.isPasswordOk(passwordEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_weak, Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.equals(passwordEdit.getText().toString().trim(), rePasswordEdit.getText().toString().trim())) {
                Toast.makeText(getContext(), R.string.password_not_same, Toast.LENGTH_SHORT).show();
            } else if (DBWalletUtil.checkWalletName(getContext(), walletNameEdit.getText().toString())) {
                Toast.makeText(getContext(), R.string.wallet_name_exist, Toast.LENGTH_SHORT).show();
            } else {
                generateAndSaveWallet();
            }
        });
        checkWalletStatus();
    }

    private void generateAndSaveWallet() {
        showProgressBar(R.string.wallet_importing);
        presenter.importMnemonic(mnemonicEdit.getText().toString().trim(),
                passwordEdit.getText().toString().trim(), paths.get(0), walletNameEdit.getText().toString().trim());
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3 && check4;
    }

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
                check2 = !TextUtils.isEmpty(passwordEdit.getText().toString().trim()) &&
                        passwordEdit.getText().toString().trim().length() >= 8;
                importButton.setClickAble(isWalletValid());
            }
        });
        rePasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check3 = !TextUtils.isEmpty(rePasswordEdit.getText().toString().trim()) &&
                        rePasswordEdit.getText().toString().trim().length() >= 8;
                importButton.setClickAble(isWalletValid());
            }
        });
        mnemonicEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                check4 = !TextUtils.isEmpty(mnemonicEdit.getText().toString().trim());
                importButton.setClickAble(isWalletValid());
            }
        });
    }

}
