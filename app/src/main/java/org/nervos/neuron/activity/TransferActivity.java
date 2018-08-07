package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.R;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.crypto.AESCrypt;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;

public class TransferActivity extends NBaseActivity {

    private static final int REQUEST_CODE_SCAN = 0x01;
    public static final String EXTRA_TOKEN = "extra_token";
    private static final String tokenUnit = "eth";
    private static final int MAX_QUOTA_SEEK = 1000;
    private static final int DEFAULT_QUOTA_SEEK = 8;
    private static final int MAX_FEE = 100;

    private TextView walletAddressText, walletNameText, feeSeekText, feeValueText, balanceText;
    private SwitchCompat setupSwitch;
    private ImageView scanImage;
    private AppCompatEditText receiveAddressEdit, transferValueEdit;
    private AppCompatButton nextActionButton;
    private CircleImageView photoImage;
    private AppCompatSeekBar feeSeekBar;
    private RelativeLayout feeSeekBarLayout;
    private LinearLayout advancedSetupLayout, gasEditLayout, quotaEditLayout;
    private EditText customGasPriceEdit, customGasEdit, customQuotaEdit, payHexDataEdit;

    private WalletItem walletItem;
    private TokenItem tokenItem;
    private BottomSheetDialog confirmDialog, passwordDialog;
    private BigInteger mGasPrice, mGasUnit, mGasLimit = BigInteger.ZERO, mQuota, mQuotaUnit, mGas;
    private boolean isGasPriceOk = false, isGasLimitOk = false;
    private String transactionHexData;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_transfer;
    }

    @Override
    protected void initView() {
        scanImage = findViewById(R.id.transfer_address_scan);
        nextActionButton = findViewById(R.id.next_action_button);
        walletAddressText = findViewById(R.id.wallet_address);
        walletNameText = findViewById(R.id.wallet_name);
        feeSeekText = findViewById(R.id.fee_seek_text);
        balanceText = findViewById(R.id.wallet_balance_text);
        feeValueText = findViewById(R.id.fee_value_text);
        receiveAddressEdit = findViewById(R.id.transfer_address);
        transferValueEdit = findViewById(R.id.transfer_value);
        feeSeekBar = findViewById(R.id.fee_seek_bar);
        photoImage = findViewById(R.id.wallet_photo);
        setupSwitch = findViewById(R.id.advanced_setup);
        feeSeekBarLayout = findViewById(R.id.fee_seek_bar_layout);
        advancedSetupLayout = findViewById(R.id.advanced_setup_layout);
        customGasPriceEdit = findViewById(R.id.custom_gas_price);
        customGasEdit = findViewById(R.id.custom_gas);
        customQuotaEdit = findViewById(R.id.custom_quota);
        payHexDataEdit = findViewById(R.id.pay_hex_data);
        gasEditLayout = findViewById(R.id.gas_layout);
        quotaEditLayout = findViewById(R.id.quota_layout);

    }

    @Override
    protected void initData() {
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);
        EthRpcService.init(mActivity);
        NervosRpcService.init(mActivity, HttpUrls.NERVOS_NODE_IP);
        walletItem = DBWalletUtil.getCurrentWallet(this);
        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        if (isETH()) {
            feeSeekBar.setMax(MAX_FEE);
            initGasInfo();
        } else {
            feeSeekBar.setMax(MAX_QUOTA_SEEK);
            initQuota();
        }
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
                dismissProgressCircle();
            }
            @Override
            public void onNext(BigInteger gasPrice) {
                dismissProgressCircle();
                mGasPrice = gasPrice;
                mGasUnit = gasPrice.multiply(ConstUtil.GAS_MIN_LIMIT);
                mGasLimit = ConstUtil.ETH.equalsIgnoreCase(tokenItem.symbol)?
                        ConstUtil.GAS_LIMIT : ConstUtil.GAS_ERC20_LIMIT;
                mGas = mGasLimit.multiply(mGasPrice);

                feeSeekBar.setProgress(mGasLimit.divide(ConstUtil.GAS_MIN_LIMIT).intValue());
                feeSeekText.setText(NumberUtil.getDecimal_8(NumberUtil.getEthFromWei(mGas)) + tokenUnit);
                feeValueText.setText(feeSeekText.getText());
            }
        });
    }

    private void initQuota() {
        mQuota = TextUtils.isEmpty(tokenItem.contractAddress)?
                ConstUtil.QUOTA_TOKEN : ConstUtil.QUOTA_ERC20;
        mQuotaUnit = ConstUtil.QUOTA_TOKEN.divide(BigInteger.valueOf(DEFAULT_QUOTA_SEEK));
        feeSeekBar.setProgress(mQuota.divide(mQuotaUnit).intValue());

        feeSeekText.setText(String.valueOf(NumberUtil.getEthFromWei(mQuota)));
        feeValueText.setText(feeSeekText.getText());
    }

    @Override
    protected void initAction() {
        scanImage.setOnClickListener((view) -> {
            AndPermission.with(mActivity)
                .runtime().permission(Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(permissions -> {
                    Intent intent = new Intent(mActivity, QrCodeActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SCAN);
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
                confirmDialog = new BottomSheetDialog(mActivity);
                confirmDialog.setCancelable(false);
                confirmDialog.getWindow().setWindowAnimations(R.style.ConfirmDialog);
                confirmDialog.setContentView(getConfirmTransferView());
                confirmDialog.show();
            }
        });
        feeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress < 1? 1 : progress;
                if (isETH()) {
                    BigInteger gas = mGasUnit.multiply(BigInteger.valueOf(progress));
                    mGasLimit = mGasLimit.multiply(BigInteger.valueOf(progress));
                    feeSeekText.setText(NumberUtil.getDecimal_8(NumberUtil.getEthFromWei(gas)) + tokenUnit);
                    feeValueText.setText(feeSeekText.getText());
                } else {
                    mQuota = mQuotaUnit.multiply(BigInteger.valueOf(progress));
                    feeSeekText.setText(String.valueOf(NumberUtil.getEthFromWei(mQuota)));
                    feeValueText.setText(feeSeekText.getText());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    feeSeekBarLayout.setVisibility(View.GONE);
                    advancedSetupLayout.setVisibility(View.VISIBLE);
                    if (isETH()) {
                        advancedSetupETHFeeValue();
                    } else {
                        advancedSetupNervosFeeValue();
                    }
                } else {
                    feeSeekBarLayout.setVisibility(View.VISIBLE);
                    advancedSetupLayout.setVisibility(View.GONE);
                    feeValueText.setText(feeSeekText.getText());
                }
            }
        });

        customGasPriceEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double price = Double.parseDouble(s.toString());
                    isGasPriceOk = (price > 0);
                    mGasPrice = Convert.toWei(s.toString(), Convert.Unit.GWEI).toBigInteger();
                    advancedSetupETHFeeValue();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, R.string.input_correct_number, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        customGasEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double limit = Double.parseDouble(s.toString());
                    isGasLimitOk = (limit > 0);
                    mGasLimit = new BigInteger(s.toString());
                    advancedSetupETHFeeValue();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, R.string.input_correct_number, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        customQuotaEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    mQuota = new BigInteger(s.toString());
                    advancedSetupNervosFeeValue();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    mQuota = BigInteger.ZERO;
                    advancedSetupNervosFeeValue();
                    Toast.makeText(mActivity, R.string.input_correct_number, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean isETH() {
        return tokenItem.chainId < 0;
    }

    private void advancedSetupETHFeeValue() {
        gasEditLayout.setVisibility(View.VISIBLE);
        quotaEditLayout.setVisibility(View.GONE);
        if (isGasLimitOk && isGasPriceOk) {
            feeValueText.setText(NumberUtil.getDecimal_8(
                    NumberUtil.getEthFromWei(mGasPrice.multiply(mGasLimit))) + tokenUnit);
        } else {
            feeValueText.setText("0");
        }
    }


    private void advancedSetupNervosFeeValue() {
        gasEditLayout.setVisibility(View.GONE);
        quotaEditLayout.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(customQuotaEdit.getText())) {
            feeValueText.setText(String.valueOf(NumberUtil.getEthFromWei(mQuota)));
        } else {
            feeValueText.setText("0");
        }
    }

    /**
     * struct confirm transfer view
     * @return
     */
    private View getConfirmTransferView() {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_transfer, null);
        ProgressBar progressBar = view.findViewById(R.id.transfer_progress);

        String value = transferValueEdit.getText().toString().trim();
        double sum = Double.parseDouble(value) + NumberUtil.getEthFromWei(isETH()? mGas : mQuota);

        ((TextView)view.findViewById(R.id.from_address)).setText(walletItem.address);
        ((TextView)view.findViewById(R.id.to_address)).setText(receiveAddressEdit.getText().toString());
        ((TextView)view.findViewById(R.id.transfer_value)).setText(transferValueEdit.getText().toString());
        ((TextView)view.findViewById(R.id.transfer_fee)).setText(feeSeekText.getText().toString());
        ((TextView)view.findViewById(R.id.transfer_sum)).setText(String.valueOf(sum));

        view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });
        view.findViewById(R.id.transfer_confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordConfirmView(value, progressBar);
                confirmDialog.dismiss();
            }
        });
        return view;
    }


    
    private void showPasswordConfirmView(String value, ProgressBar progressBar) {
        passwordDialog = new BottomSheetDialog(mActivity);
        passwordDialog.setCancelable(false);
        passwordDialog.getWindow().setWindowAnimations(R.style.PasswordDialog);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_transfer_password, null);
        EditText passwordEdit = view.findViewById(R.id.wallet_password_edit);
        view.findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordDialog.dismiss();
            }
        });
        AppCompatButton sendButton = view.findViewById(R.id.transfer_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEdit.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
                } else if (!AESCrypt.checkPassword(password, walletItem)) {
                    Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    if (isETH()) {
                        if (ConstUtil.ETH.equals(tokenItem.symbol)) {
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
            }
        });
        passwordDialog.setContentView(view);
        passwordDialog.show();
    }



    /**   transfer token of etherum and appchain  */

    /**
     * transfer origin token of nervos
     * @param value  transfer value
     * @param progressBar
     */
    private void transferNervosToken(String password, double value, ProgressBar progressBar){
        transactionHexData = payHexDataEdit.getText().toString().trim();
        NervosRpcService.transferNervos(receiveAddressEdit.getText().toString().trim(), value,
                transactionHexData, password)
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
                passwordDialog.dismiss();
            }
            @Override
            public void onNext(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction) {
                progressBar.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(ethSendTransaction.getSendTransactionResult().getHash())) {
                    Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                    passwordDialog.dismiss();
                    finish();
                } else if (ethSendTransaction.getError() != null &&
                        !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
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
        NervosRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(tokenItem.chainId));
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
                    passwordDialog.dismiss();
                }
                @Override
                public void onNext(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction) {
                    progressBar.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(ethSendTransaction.getSendTransactionResult().getHash())) {
                        Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        passwordDialog.dismiss();
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
        transactionHexData = payHexDataEdit.getText().toString().trim();
        EthRpcService.transferEth(receiveAddressEdit.getText().toString().trim(),
                Double.valueOf(value), mGasPrice, mGasLimit, transactionHexData, password)
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
                    passwordDialog.dismiss();
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    progressBar.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
                        Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        passwordDialog.dismiss();
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
        EthRpcService.transferErc20(tokenItem, receiveAddressEdit.getText().toString().trim(),
                Double.valueOf(value), mGasPrice, mGasLimit, password)
            .subscribe(new Subscriber<EthSendTransaction>() {
                @Override
                public void onCompleted() {
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    passwordDialog.dismiss();
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    progressBar.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
                        Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        passwordDialog.dismiss();
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
        if (confirmDialog != null) {
            confirmDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN:
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
                break;
            default:
                break;
        }
    }
}
