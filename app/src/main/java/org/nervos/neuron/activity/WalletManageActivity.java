package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.crypto.WalletEntity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.nervos.neuron.activity.MainActivity.EXTRA_TAG;

public class WalletManageActivity extends BaseActivity {

    private RelativeLayout walletNameLayout;
    private TextView walletNameText;
    private TextView walletAddressText;
    private CircleImageView photoImage;
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
        photoImage = findViewById(R.id.wallet_photo);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
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
                        } else if (DBWalletUtil.checkWalletName(mActivity, simpleDialog.getMessage())) {
                            Toast.makeText(mActivity, "钱包名称已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            walletNameText.setText(simpleDialog.getMessage());
                            DBWalletUtil.updateWalletName(mActivity, walletItem.name, simpleDialog.getMessage());
                            SharePrefUtil.putCurrentWalletName(simpleDialog.getMessage());
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
                simpleDialog.setTitle(R.string.input_password_hint);
                simpleDialog.setMessageHint("password");
                simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
                simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick() {
                        if (simpleDialog.getMessage() == null || TextUtils.isEmpty(simpleDialog.getMessage())) {
                            Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
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
                SimpleDialog deleteDialog = new SimpleDialog(mActivity);
                deleteDialog.setTitle(getString(R.string.ask_confirm_delete_wallet));
                deleteDialog.setMessageHint(getString(R.string.password));
                deleteDialog.setEditInputType(SimpleDialog.PASSWORD);
                deleteDialog.setOnCancelClickListener(() -> deleteDialog.dismiss());
                deleteDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick() {
                        if (!walletItem.password.equals(deleteDialog.getMessage())) {
                            Toast.makeText(mActivity, R.string.wallet_passworrd_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<String> names = DBWalletUtil.getAllWalletName(mActivity);
                        if (names.size() > 1) {
                            SharePrefUtil.putCurrentWalletName(names.get(names.indexOf(walletItem.name) == 0? 1:0));
                        } else if (names.size() > 0) {
                            SharePrefUtil.deleteWalletName();
                        }
                        DBWalletUtil.deleteWallet(mActivity, walletItem.name);
                        deleteDialog.dismiss();
                        Toast.makeText(mActivity, "删除成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mActivity, MainActivity.class);
                        intent.putExtra(EXTRA_TAG, AppFragment.TAG);
                        startActivity(intent);
                        finish();
                    }
                });
                deleteDialog.show();
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
