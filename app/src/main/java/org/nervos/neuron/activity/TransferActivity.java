package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.CitaRpcService;
import org.nervos.neuron.R;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.util.PermissionUtil;
import org.nervos.neuron.util.RuntimeRationale;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.core.methods.response.EthGetTransactionReceipt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;
    private static final String CONTRACT_ADDRESS = "0x73552bc4e960a1d53013b40074569ea05b950b4d";
    private static final int DEFAULT_FEE = 20;
    private static final int MAX_FEE = 100;

    private TextView walletAddressText;
    private TextView walletNameText;
    private ImageView scanImage;
    private TextView feeText;
    private AppCompatEditText receiveAddressEdit;
    private AppCompatEditText transferValueEdit;
    private AppCompatButton nextActionButton;
    private AppCompatSeekBar feeSeekBar;

    private WalletItem walletItem;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        walletItem = DBWalletUtil.getCurrentWallet(this);

        CitaRpcService.init();
        initView();
        initListener();

    }

    private void initView() {
        scanImage = findViewById(R.id.transfer_address_scan);
        nextActionButton = findViewById(R.id.next_action_button);
        walletAddressText = findViewById(R.id.wallet_address);
        walletNameText = findViewById(R.id.wallet_name);
        feeText = findViewById(R.id.fee_text);
        receiveAddressEdit = findViewById(R.id.transfer_address);
        transferValueEdit = findViewById(R.id.transfer_value);
        feeSeekBar = findViewById(R.id.fee_seek_bar);

        feeSeekBar.setMax(MAX_FEE);
        feeSeekBar.setProgress(DEFAULT_FEE);

        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
    }

    private void initListener() {
        scanImage.setOnClickListener((view) -> {
            AndPermission.with(mActivity)
                .runtime().permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(mActivity, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                })
                .onDenied(permissions -> PermissionUtil.showSettingDialog(mActivity, permissions))
                .start();
        });

        nextActionButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(receiveAddressEdit.getText().toString().trim())) {
                Toast.makeText(TransferActivity.this, "转账地址不能为空", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(transferValueEdit.getText().toString().trim())) {
                Toast.makeText(TransferActivity.this, "转账金额不能为空", Toast.LENGTH_SHORT).show();
            } else {
                BottomSheetDialog sheetDialog = new BottomSheetDialog(TransferActivity.this);
                sheetDialog.setCancelable(false);
                sheetDialog.setContentView(getConfirmTransferView(sheetDialog));
                sheetDialog.show();
            }
        });
        feeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                feeText.setText(String.valueOf(progress/100.0));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private View getConfirmTransferView(BottomSheetDialog sheetDialog) {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_transfer, null);
        TextView toAddress = view.findViewById(R.id.to_address);
        TextView fromAddress = view.findViewById(R.id.from_address);
        TextView valueText = view.findViewById(R.id.transfer_value);
        TextView feeConfirmText = view.findViewById(R.id.transfer_fee);
        ProgressBar progressBar = view.findViewById(R.id.transfer_progress);

        fromAddress.setText(walletItem.address);
        toAddress.setText(receiveAddressEdit.getText().toString());
        valueText.setText(transferValueEdit.getText().toString());
        feeConfirmText.setText(feeText.getText().toString());

        view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
            }
        });

        view.findViewById(R.id.transfer_confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transfer(progressBar);
            }
        });
        return view;
    }

    private void transfer(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        cachedThreadPool.execute(() -> {
            String value = transferValueEdit.getText().toString().trim();
            CitaRpcService.transfer(CONTRACT_ADDRESS,
                    receiveAddressEdit.getText().toString().trim(),
                    Long.parseLong(value),
                    new CitaRpcService.OnTransferResultListener() {
                        @Override
                        public void onSuccess(EthGetTransactionReceipt receipt) {
                            Toast.makeText(TransferActivity.this,
                                    "转账成功", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    receiveAddressEdit.setText(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(TransferActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
