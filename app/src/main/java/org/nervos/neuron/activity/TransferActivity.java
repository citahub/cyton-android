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

import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.R;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.math.BigInteger;

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

    private String tokenUnit = "eth";
    private BigInteger mGasPrice;
    private double mGas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        walletItem = DBWalletUtil.getCurrentWallet(this);
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);
        EthRpcService.init(mActivity);
        NervosRpcService.init(mActivity, ConstantUtil.NERVOS_NODE_IP);
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

        findViewById(R.id.fee_layout).setVisibility(tokenItem.chainId < 0? View.VISIBLE:View.GONE);
        feeSeekBar.setMax(MAX_FEE);
        feeSeekBar.setProgress(DEFAULT_FEE);

        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
    }

    private void initGasInfo() {
        showProgressCircle();
        EthRpcService.getEthGasPrice().subscribe(new Subscriber<BigInteger>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNext(BigInteger gasPrice) {
                mGasPrice = gasPrice;
                mGas = NumberUtil.getDoubleFromBig(gasPrice.multiply(ConstantUtil.GAS_LIMIT));
                feeText.setText(NumberUtil.getDecimal_6(mGas) + tokenUnit);
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
                Toast.makeText(mActivity, R.string.transfer_address_not_null, Toast.LENGTH_SHORT).show();
            } else if (!AddressUtil.isAddressValid(receiveAddressEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.address_error, Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(transferValueEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.transfer_amount_not_null, Toast.LENGTH_SHORT).show();
            } else {
                sheetDialog = new BottomSheetDialog(mActivity);
                sheetDialog.setCancelable(false);
                sheetDialog.setContentView(getConfirmTransferView(sheetDialog));
                sheetDialog.show();
            }
        });
        feeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                feeText.setText(NumberUtil.getDecimal_6(progress*mGas/DEFAULT_FEE) + tokenUnit);
                mGasPrice = mGasPrice.multiply(BigInteger.valueOf(progress))
                        .divide(BigInteger.valueOf(DEFAULT_FEE));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * struct confirm transfer view
     * @param sheetDialog
     * @return
     */
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
        view.findViewById(R.id.confirm_fee_layout).setVisibility(tokenItem.chainId < 0? View.VISIBLE:View.GONE);

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
                showPasswordConfirmView(value, progressBar);
            }
        });
        return view;
    }


    private void showPasswordConfirmView(String value, ProgressBar progressBar) {
        SimpleDialog simpleDialog = new SimpleDialog(mActivity);
        simpleDialog.setTitle(R.string.input_password);
        simpleDialog.setMessageHint("password");
        simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
        simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
            @Override
            public void onOkClick() {
                String password = simpleDialog.getMessage();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
                    return;
                } else if (!AESCrypt.checkPassword(password, walletItem)) {
                    Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                simpleDialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
                if (tokenItem.chainId < 0) {
                    if (ConstantUtil.ETH.equals(tokenItem.symbol)) {
                        transferEth(password, value, progressBar);
                    } else {
                        transferEthErc20(password, value, progressBar);
                    }
                } else {
                    try {
                        if (TextUtils.isEmpty(tokenItem.contractAddress)) {
                            transferNervosToken(password, Double.valueOf(value), progressBar);
                        } else {
                            transferNervosErc20(password, Double.valueOf(value), progressBar);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
        simpleDialog.show();
    }


    /**
     * transfer origin token of nervos
     * @param value  transfer value
     * @param progressBar
     */
    private void transferNervosToken(String password, double value, ProgressBar progressBar){
    NervosRpcService.transferNervos(receiveAddressEdit.getText().toString().trim(), value, password)
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
                if (!TextUtils.isEmpty(ethSendTransaction.getSendTransactionResult().getHash())) {
                    Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                    finish();
                } else if (ethSendTransaction.getError() != null &&
                        !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())){
                    Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * transfer erc20 token of nervos
     * @param value  transfer value
     * @param progressBar
     */
    private void transferNervosErc20(String password, double value, ProgressBar progressBar) throws Exception {
        NervosRpcService.transferErc20(tokenItem, tokenItem.contractAddress,
                receiveAddressEdit.getText().toString().trim(), value, password)
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
                    if (!TextUtils.isEmpty(ethSendTransaction.getSendTransactionResult().getHash())) {
                        Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        sheetDialog.dismiss();
                        finish();
                    } else if (ethSendTransaction.getError() != null &&
                            !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())){
                        Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }


    /**
     * transfer origin token of ethereum
     * @param value
     * @param progressBar
     */
    private void transferEth(String password, String value, ProgressBar progressBar) {
        EthRpcService.transferEth(receiveAddressEdit.getText().toString().trim(),
                Double.valueOf(value), mGasPrice, password)
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
                    if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
                        Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        sheetDialog.dismiss();
                        finish();
                    } else if (ethSendTransaction.getError() != null &&
                            !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())){
                        LogUtil.d(ethSendTransaction.getError().getMessage());
                        Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }


    /**
     * transfer origin token of ethereum
     * @param value
     * @param progressBar
     */
    private void transferEthErc20(String password, String value, ProgressBar progressBar) {
        EthRpcService.transferErc20(tokenItem,
            receiveAddressEdit.getText().toString().trim(), Double.valueOf(value), mGasPrice, password)
            .subscribe(new Subscriber<EthSendTransaction>() {
                @Override
                public void onCompleted() {
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sheetDialog.dismiss();
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
                        Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        sheetDialog.dismiss();
                        finish();
                    } else if (ethSendTransaction.getError() != null &&
                            !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())){
                        Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sheetDialog != null) {
            sheetDialog.dismiss();
        }
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
                    Toast.makeText(TransferActivity.this, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
