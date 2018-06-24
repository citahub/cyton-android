package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBWalletUtil;

public class ChangePasswordActivity extends BaseActivity {

    private AppCompatEditText oldPasswordEdit;
    private AppCompatEditText newPasswordEdit;
    private AppCompatEditText newRePasswordEdit;
    private TextView walletNameText;

    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        walletItem = DBWalletUtil.getCurrentWallet(this);

        initView();
    }

    private void initView() {
        oldPasswordEdit = findViewById(R.id.edit_wallet_password);
        newPasswordEdit = findViewById(R.id.edit_wallet_new_password);
        newRePasswordEdit = findViewById(R.id.edit_wallet_new_repassword);
        walletNameText = findViewById(R.id.wallet_name_text);

        walletNameText.setText(walletItem.name);

        findViewById(R.id.change_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(oldPasswordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, R.string.old_password_not_null, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newPasswordEdit.getText().toString().trim())){
                    Toast.makeText(mActivity, R.string.new_password_not_null, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newRePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, R.string.new_password_not_null, Toast.LENGTH_SHORT).show();
                } else if (!TextUtils.equals(newPasswordEdit.getText().toString().trim(),
                        newRePasswordEdit.getText().toString().trim())) {
                    Toast.makeText(mActivity, R.string.password_not_same, Toast.LENGTH_SHORT).show();
                } else if (!AESCrypt.checkPassword(oldPasswordEdit.getText().toString().trim(), walletItem)) {
                    Toast.makeText(mActivity, R.string.old_password_error, Toast.LENGTH_SHORT).show();
                } else {
                    DBWalletUtil.updateWalletPassword(mActivity, walletItem.name,
                            oldPasswordEdit.getText().toString().trim(),
                            newPasswordEdit.getText().toString());
                    Toast.makeText(mActivity, R.string.update_password_success, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
