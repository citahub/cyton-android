package org.nervos.neuron.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.crypto.WalletEntity;
import org.web3j.crypto.CipherException;

public class WalletManageActivity extends BaseActivity {

    private RelativeLayout walletNameLayout;
    private TextView walletNameText;
    private TextView walletAddressText;
    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_manage);

        walletItem = DBWalletUtil.getCurrentWallet(this);

        initView();
        initListener();
    }

    private void initView() {
        walletNameLayout = findViewById(R.id.wallet_name_layout);
        walletNameText = findViewById(R.id.wallet_name_text);
        walletAddressText = findViewById(R.id.wallet_address);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
    }


    private void initListener() {
        walletNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDialog simpleDialog = new SimpleDialog(mActivity);
                simpleDialog.setTitle("修改钱包名称");
                simpleDialog.setMessageHint("输入钱包名称");
                simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick() {
                        if (simpleDialog.getMessage() == null || TextUtils.isEmpty(simpleDialog.getMessage())) {
                            Toast.makeText(mActivity, "钱包名称不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            walletNameText.setText(simpleDialog.getMessage());
                            DBWalletUtil.updateWalletName(mActivity, walletItem.name, simpleDialog.getMessage());
                            SharePrefUtil.putWalletName(simpleDialog.getMessage());
                            simpleDialog.dismiss();
                        }
                    }
                });
                simpleDialog.setOnCancelClickListener(new SimpleDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        simpleDialog.dismiss();
                    }
                });
                simpleDialog.show();
            }
        });
        findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, ChangePasswordActivity.class));
            }
        });

        findViewById(R.id.export_keystore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDialog simpleDialog = new SimpleDialog(mActivity);
                simpleDialog.setTitle("请输入密码");
                simpleDialog.setMessageHint("password");
                simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
                simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick() {
                        if (simpleDialog.getMessage() == null || TextUtils.isEmpty(simpleDialog.getMessage())) {
                            Toast.makeText(mActivity, "密码不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            generateKeystore(simpleDialog.getMessage());
                            simpleDialog.dismiss();
                        }
                    }
                });
                simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
                simpleDialog.show();
            }
        });

        findViewById(R.id.delete_wallet_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("确认删除钱包吗？").setCancelable(false)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        DBWalletUtil.deleteWallet(WalletManageActivity.this, walletItem.name);
                        dialog.dismiss();
                        Toast.makeText(mActivity, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();

            }
        });

    }

    private void generateKeystore(String password) {
        showProgressBar("正在生成...");
        new Thread(){
            @Override
            public void run() {
                super.run();
                String keystore = WalletEntity.exportKeyStore(walletItem, password);
                walletNameText.post(() -> {
                    Intent intent = new Intent(mActivity, ExportKeystoreActivity.class);
                    intent.putExtra(ExportKeystoreActivity.EXTRA_KEYSTORE, keystore);
                    startActivity(intent);
                    dismissProgressBar();
                });
            }
        }.start();

    }


}
