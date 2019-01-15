package com.cryptape.cita_wallet.activity;

import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.service.http.WalletService;
import com.cryptape.cita_wallet.util.Blockies;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.WalletTextWatcher;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.view.button.CommonButton;

/**
 * Created by duanyytop on 2018/5/20
 */
public class ChangePasswordActivity extends NBaseActivity {

    private AppCompatEditText oldPasswordEdit;
    private AppCompatEditText newPasswordEdit;
    private AppCompatEditText newRePasswordEdit;
    private TextView walletNameText;
    private CircleImageView walletPhoto;
    private CommonButton button;

    private Wallet wallet;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_change_password;
    }

    @Override
    protected void initView() {
        oldPasswordEdit = findViewById(R.id.edit_wallet_password);
        newPasswordEdit = findViewById(R.id.edit_wallet_new_password);
        newRePasswordEdit = findViewById(R.id.edit_wallet_new_repassword);
        walletNameText = findViewById(R.id.wallet_name_text);
        walletPhoto = findViewById(R.id.wallet_photo);
        button = findViewById(R.id.change_password_button);
    }

    @Override
    protected void initData() {
        checkWalletStatus();
        wallet = DBWalletUtil.getCurrentWallet(this);
        walletNameText.setText(wallet.name);
        walletPhoto.setImageBitmap(Blockies.createIcon(wallet.address));
    }

    @Override
    protected void initAction() {
        button.setOnClickListener(v -> {
            if (TextUtils.isEmpty(oldPasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.old_password_not_null, Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(newPasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.new_password_not_null, Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(newRePasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.new_password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!TextUtils.equals(newPasswordEdit.getText().toString().trim(),
                    newRePasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.password_not_same, Toast.LENGTH_SHORT).show();
            } else if (!WalletService.checkPassword(mActivity, oldPasswordEdit.getText().toString().trim(), wallet)) {
                Toast.makeText(mActivity, R.string.old_password_error, Toast.LENGTH_SHORT).show();
            } else if (TextUtils.equals(newPasswordEdit.getText().toString().trim(),
                    oldPasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.old_new_password_same, Toast.LENGTH_SHORT).show();
            } else if (!NumberUtil.isPasswordOk(newPasswordEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.password_weak, Toast.LENGTH_SHORT).show();
            } else {
                DBWalletUtil.updateWalletPassword(mActivity, wallet.name,
                        oldPasswordEdit.getText().toString().trim(),
                        newPasswordEdit.getText().toString());
                Toast.makeText(mActivity, R.string.update_password_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean isWalletValid() {
        return check1 && check2 && check3;
    }

    private boolean check1 = false, check2 = false, check3 = false;

    private void checkWalletStatus() {
        oldPasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                if (!TextUtils.isEmpty(oldPasswordEdit.getText().toString().trim()) && oldPasswordEdit.getText().toString().length() >= 8) {
                    check1 = true;
                }
                button.setClickAble(isWalletValid());
            }
        });
        newPasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                if (!TextUtils.isEmpty(newPasswordEdit.getText().toString().trim()) && newPasswordEdit.getText().toString().length() >= 8) {
                    check2 = true;
                }
                button.setClickAble(isWalletValid());
            }
        });
        newRePasswordEdit.addTextChangedListener(new WalletTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                super.onTextChanged(charSequence, i, i1, i2);
                if (!TextUtils.isEmpty(newRePasswordEdit.getText().toString().trim()) && newRePasswordEdit.getText().toString().length() >= 8) {
                    check3 = true;
                }
                button.setClickAble(isWalletValid());
            }
        });
    }

}
