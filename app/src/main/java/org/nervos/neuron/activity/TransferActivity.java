package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.CitaRpcService;
import org.nervos.neuron.R;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.service.EthErc20RpcService;
import org.nervos.neuron.service.EthNativeRpcService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;

public class TransferActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;
    public static final String EXTRA_TOKEN = "extra_token";
    private static final int MAX_FEE = 100;
    private static final int DEFAULT_FEE = 20;    // default seek progress is 20

    private TextView walletAddressText;
    private TextView walletNameText;
    private ImageView scanImage;
    private TextView feeText;
    private AppCompatEditText receiveAddressEdit;
    private AppCompatEditText transferValueEdit;
    private AppCompatButton nextActionButton;
    private AppCompatSeekBar feeSeekBar;
    private CircleImageView photoImage;

    private WalletItem walletItem;
    private TokenItem tokenItem;
    private BottomSheetDialog sheetDialog;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private double unitGas = 0.0;
    private String tokenUnit = "eth";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        walletItem = DBWalletUtil.getCurrentWallet(this);
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);

        tokenUnit = tokenItem.chainId < 0? "eth":"";

        CitaRpcService.init(mActivity, CitaRpcService.NODE_IP);
        initView();
        initListener();
        initGasInfo();

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
        photoImage = findViewById(R.id.wallet_photo);

        feeSeekBar.setMax(MAX_FEE);
        feeSeekBar.setProgress(DEFAULT_FEE);

        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
    }

    private void initGasInfo() {
        showProgressCircle();
        EthNativeRpcService.getEthGas().subscribe(new Subscriber<Double>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNext(Double gas) {
                unitGas = gas * DEFAULT_FEE / MAX_FEE;
                feeText.setText(NumberUtil.getDecimal_6(gas) + tokenUnit);
                dismissProgressCircle();
            }
        });
    }

    private void initListener() {
        scanImage.setOnClickListener((view) -> {
            AndPermission.with(mActivity)
                .runtime().permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(mActivity, QrCodeActivity.class);
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
                sheetDialog = new BottomSheetDialog(TransferActivity.this);
                sheetDialog.setCancelable(false);
                sheetDialog.setContentView(getConfirmTransferView(sheetDialog));
                sheetDialog.show();
            }
        });
        feeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                feeText.setText(NumberUtil.getDecimal_6(progress * unitGas));
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
                String value = transferValueEdit.getText().toString().trim();
                if (tokenItem.chainId < 0) {
                    if (EthNativeRpcService.ETH.equals(tokenItem.symbol)) {
                        transferEth(value, progressBar);
                    } else {
                        Log.d("wallet", "erc20 token: " + tokenItem.symbol);
                        transferEthErc20(value, progressBar);
                    }
                } else {
                    try {
                        if (TextUtils.isEmpty(tokenItem.contractAddress)) {
                            transferCitaToken(Double.valueOf(value), progressBar);
                        } else {
                            transferCitaErc20(Double.valueOf(value), progressBar);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return view;
    }


    /**
     * transfer origin token of cita
     * @param value  transfer value
     * @param progressBar
     */
        private void transferCitaToken(double value, ProgressBar progressBar) throws Exception {
        CitaRpcService.transferNervos(receiveAddressEdit.getText().toString().trim(), value)
            .subscribe(new Subscriber<org.nervos.web3j.protocol.core.methods.response.EthSendTransaction>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(TransferActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                }
                @Override
                public void onNext(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction) {
                    Toast.makeText(TransferActivity.this, "转账成功", Toast.LENGTH_SHORT).show();
                    Log.d("wallet", "transaction hash: " + ethSendTransaction.getSendTransactionResult().getHash());
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
//                    finish();
                }
            });
    }


    /**
     * transfer erc20 token of cita
     * @param value  transfer value
     * @param progressBar
     */
    private void transferCitaErc20(double value, ProgressBar progressBar) throws Exception {
        CitaRpcService.transferErc20(tokenItem, tokenItem.contractAddress,
                receiveAddressEdit.getText().toString().trim(), value)
            .subscribe(new Subscriber<org.nervos.web3j.protocol.core.methods.response.EthSendTransaction>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(TransferActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                }
                @Override
                public void onNext(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction) {
                    Toast.makeText(TransferActivity.this, "转账成功", Toast.LENGTH_SHORT).show();
                    Log.d("wallet", "transaction hash: " + ethSendTransaction.getSendTransactionResult().getHash());
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
//                    finish();
                }
            });
    }


    /**
     * transfer origin token of ethereum
     * @param value
     * @param progressBar
     */
    private void transferEth(String value, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        EthNativeRpcService.transferEth(receiveAddressEdit.getText().toString().trim(),Double.valueOf(value))
            .subscribe(new Subscriber<EthSendTransaction>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(TransferActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    Toast.makeText(TransferActivity.this,
                            "转账成功", Toast.LENGTH_SHORT).show();
                    Log.d("wallet", "transaction hash: " + ethSendTransaction.getTransactionHash());
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
//                    finish();
                }
            });
    }


    /**
     * transfer origin token of ethereum
     * @param value
     * @param progressBar
     */
    private void transferEthErc20(String value, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        EthErc20RpcService.transferErc20(tokenItem, receiveAddressEdit.getText().toString().trim(),Double.valueOf(value))
            .subscribe(new Subscriber<EthSendTransaction>() {
                @Override
                public void onCompleted() {

                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(TransferActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    Toast.makeText(TransferActivity.this,
                            "转账成功", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
//                    finish();
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) return;
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
