package org.nervos.neuron.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.ExportDialog;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.fragment.WalletFragment;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.DBUtil;
import org.nervos.neuron.util.SharePrefUtil;
import org.nervos.neuron.util.crypto.WalletEntity;

public class WalletManageActivity extends AppCompatActivity {

    private RelativeLayout walletNameLayout;
    private TextView walletNameText;
    private WalletItem walletItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_manage);

        walletItem = DBUtil.getCurrentWallet(this);

        initView();
        initListener();
    }

    private void initView() {
        walletNameLayout = findViewById(R.id.wallet_name_layout);
        walletNameText = findViewById(R.id.wallet_name_text);

        walletNameText.setText(walletItem.name);
    }


    private void initListener() {
        walletNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDialog simpleDialog = new SimpleDialog(WalletManageActivity.this);
                simpleDialog.setTitle("修改钱包名称");
                simpleDialog.setMessageHint("输入钱包名称");
                simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick() {
                        if (simpleDialog.getMessage() == null || TextUtils.isEmpty(simpleDialog.getMessage())) {
                            Toast.makeText(WalletManageActivity.this, "钱包名称不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            walletNameText.setText(simpleDialog.getMessage());
                            DBUtil.updateWalletName(WalletManageActivity.this, walletItem.name, simpleDialog.getMessage());
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
                startActivity(new Intent(WalletManageActivity.this, ChangePasswordActivity.class));
            }
        });

        findViewById(R.id.export_keystore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExportDialog exportDialog = new ExportDialog(WalletManageActivity.this);
                exportDialog.setTitle("导出");
                exportDialog.setMessage(WalletEntity.exportKeyStore(walletItem));
                exportDialog.setOnCopyClickListener(new ExportDialog.OnCopyClickListener() {
                    @Override
                    public void onCopyClick() {
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("keystore", exportDialog.getMessage());
                        if (cm != null) {
                            cm.setPrimaryClip(mClipData);
                            Toast.makeText(WalletManageActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                exportDialog.setOnShareClickListener(new ExportDialog.OnShareClickListener() {
                    @Override
                    public void onShareClick() {

                    }
                });
                exportDialog.show();
            }
        });

        findViewById(R.id.delete_wallet_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WalletManageActivity.this);
                builder.setTitle("确认删除钱包吗？").setCancelable(false)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        DBUtil.deleteWallet(WalletManageActivity.this, walletItem.name);
                        dialog.dismiss();
                        Toast.makeText(WalletManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                }).create().show();

            }
        });

    }


}
