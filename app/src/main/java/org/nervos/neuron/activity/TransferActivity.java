package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
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

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.QRUtils.CodeUtils;
import org.nervos.neuron.util.SensorDataTrackUtils;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.button.CommonButton;
import org.nervos.neuron.view.dialog.TransferDialog;
import org.nervos.neuron.view.tool.NeuronTextWatcher;
import org.nervos.neuron.view.tool.OnNeuronSeekBarChangeListener;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransferActivity extends NBaseActivity {

    private static final int REQUEST_CODE_SCAN = 0x01;
    public static final String EXTRA_TOKEN = "extra_token";
    private static final int MAX_QUOTA_SEEK = 100;
    private static final int DEFAULT_QUOTA_SEEK = 1;
    private static final int MAX_FEE = 100;

    private TextView walletAddressText, walletNameText, feeSeekText, feeValueText, balanceText;
    private SwitchCompat setupSwitch;
    private ImageView scanImage;
    private AppCompatEditText receiveAddressEdit, transferValueEdit;
    private CommonButton nextActionButton;
    private CircleImageView photoImage;
    private AppCompatSeekBar feeSeekBar;
    private RelativeLayout feeSeekBarLayout;
    private LinearLayout advancedSetupLayout, gasEditLayout, quotaEditLayout;
    private EditText customGasPriceEdit, customGasEdit, customQuotaEdit, payHexDataEdit;
    private ProgressBar progressBar;

    private WalletItem walletItem;
    private TokenItem tokenItem;
    private BigInteger mGasPrice, mGasUnit, mGasLimit = BigInteger.ZERO, mQuota, mQuotaUnit, mGas;
    private boolean isGasPriceOk = false, isGasLimitOk = false;
    private String transactionHexData;
    private double mPrice = 0.0f, mBalance;
    private CurrencyItem currencyItem;
    private TitleBar titleBar;
    private double mFee;

    private TransferDialog transferDialog;

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
        titleBar = findViewById(R.id.title);

    }

    @Override
    protected void initData() {
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);
        titleBar.setTitle(tokenItem.symbol + getString(R.string.title_transfer));
        EthRpcService.init(mActivity);
        NervosRpcService.init(mActivity, HttpUrls.NERVOS_NODE_IP);
        walletItem = DBWalletUtil.getCurrentWallet(this);
        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        initBalance();
        if (isETH()) {
            feeSeekBar.setMax(MAX_FEE);
            initGasInfo();
            initPrice();
        } else {
            feeSeekBar.setMax(MAX_QUOTA_SEEK);
            initQuota();
        }
    }

    @SuppressLint("SetTextI18n")
    private void initBalance() {
        WalletService.getBalanceWithToken(mActivity, tokenItem).subscribe(new NeuronSubscriber<Double>() {
            @Override
            public void onNext(Double balance) {
                mBalance = balance;
                balanceText.setText(String.format(
                        getString(R.string.transfer_balance_place_holder),
                        NumberUtil.getDecimal8Sub(balance) + " " + tokenItem.symbol));
            }
        });
    }

    private void initGasInfo() {
        showProgressCircle();
        EthRpcService.getEthGasPrice().subscribe(new NeuronSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                dismissProgressCircle();
            }

            @Override
            public void onNext(BigInteger gasPrice) {
                dismissProgressCircle();
                mGasPrice = gasPrice;
                mGasUnit = gasPrice.multiply(ConstUtil.GAS_MIN_LIMIT);
                mGasLimit = ConstUtil.ETH.equalsIgnoreCase(tokenItem.symbol) ?
                        ConstUtil.GAS_LIMIT : ConstUtil.GAS_ERC20_LIMIT;
                mGas = mGasLimit.multiply(mGasPrice);
                feeSeekBar.setProgress(mGasLimit.divide(ConstUtil.GAS_MIN_LIMIT).intValue());

                initFeeText();
            }
        });
    }

    private void initPrice() {
        currencyItem = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(tokenItem.symbol, currencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) return;
                        try {
                            mPrice = Double.parseDouble(price);
                            initFeeText();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void initFeeText() {
        double fee = NumberUtil.getEthFromWei(mGas);
        if (fee > 0) {
            mFee = NumberUtil.getEthFromWei(mGas);
            feeSeekText.setText(NumberUtil.getDecimal8ENotation(fee) + getFeeTokenUnit());
            if (mPrice > 0 && currencyItem != null) {
                feeValueText.setText(feeSeekText.getText() + " = " +
                        currencyItem.getSymbol() + NumberUtil.getDecimalValid_2(fee * mPrice));
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void initQuota() {
        mQuota = TextUtils.isEmpty(tokenItem.contractAddress) ?
                ConstUtil.QUOTA_TOKEN : ConstUtil.QUOTA_ERC20;
        mQuotaUnit = ConstUtil.QUOTA_TOKEN.divide(BigInteger.valueOf(DEFAULT_QUOTA_SEEK));
        feeSeekBar.setProgress(mQuota.divide(mQuotaUnit).intValue());
        mFee = NumberUtil.getEthFromWei(mQuota);
        feeSeekText.setText(NumberUtil.getDecimal8ENotation(mFee) + getFeeTokenUnit());
        feeValueText.setText(feeSeekText.getText());
    }

    @Override
    protected void initAction() {

        advancedSetup();

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
            } else if (receiveAddressEdit.getText().toString().trim().equalsIgnoreCase(walletItem.address)) {
                Toast.makeText(mActivity, R.string.address_from_equal_to, Toast.LENGTH_LONG).show();
            } else if (!AddressUtil.isAddressValid(receiveAddressEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.address_error, Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(transferValueEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, R.string.transfer_amount_not_null, Toast.LENGTH_SHORT).show();
            } else if (mBalance < Double.parseDouble(transferValueEdit.getText().toString().trim())) {
                Toast.makeText(mActivity, String.format(getString(R.string.balance_not_enough),
                        tokenItem.symbol), Toast.LENGTH_SHORT).show();
            } else if (mBalance < mFee) {
                Toast.makeText(mActivity, String.format(getString(R.string.balance_not_enough_fee),
                        tokenItem.symbol), Toast.LENGTH_SHORT).show();
            } else {
                if (!isFastDoubleClick())
                    getConfirmTransferView();
            }
        });
        feeSeekBar.setOnSeekBarChangeListener(new OnNeuronSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress < 1 ? 1 : progress;
                if (isETH()) {
                    mGas = mGasUnit.multiply(BigInteger.valueOf(progress));
                    mGasLimit = mGasLimit.multiply(BigInteger.valueOf(progress));
                    initFeeText();
                } else {
                    mQuota = mQuotaUnit.multiply(BigInteger.valueOf(progress));
                    mFee = NumberUtil.getEthFromWei(mQuota);
                    feeSeekText.setText(NumberUtil.getDecimal8ENotation(mFee) + getFeeTokenUnit());
                    feeValueText.setText(feeSeekText.getText());
                }
            }
        });

    }

    /**
     * transfer advanced setup
     */
    private void advancedSetup() {
        advancedGas();
        advancedQuota();
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

    }

    /**
     * gas advanced setup
     */
    private void advancedGas() {
        customGasPriceEdit.addTextChangedListener(new NeuronTextWatcher() {
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
        });
        customGasEdit.addTextChangedListener(new NeuronTextWatcher() {
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
        });
    }

    /**
     * quota advanced setup
     */
    private void advancedQuota() {
        customQuotaEdit.addTextChangedListener(new NeuronTextWatcher() {
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
        });
    }

    private boolean isETH() {
        return tokenItem.chainId < 0;
    }

    private String getFeeTokenUnit() {
        if (isETH()) {
            return " " + ConstUtil.ETH;
        } else {
            ChainItem chainItem = DBChainUtil.getChain(mActivity, tokenItem.chainId);
            return chainItem == null ? "" : " " + chainItem.tokenSymbol;
        }
    }

    @SuppressLint("SetTextI18n")
    private void advancedSetupETHFeeValue() {
        gasEditLayout.setVisibility(View.VISIBLE);
        quotaEditLayout.setVisibility(View.GONE);
        if (isGasLimitOk && isGasPriceOk) {
            mFee = NumberUtil.getEthFromWei(mGasPrice.multiply(mGasLimit));
            feeValueText.setText(mFee + getFeeTokenUnit());
        } else {
            mFee = 0;
            feeValueText.setText(mFee + "");
        }
    }


    @SuppressLint("SetTextI18n")
    private void advancedSetupNervosFeeValue() {
        gasEditLayout.setVisibility(View.GONE);
        quotaEditLayout.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(customQuotaEdit.getText())) {
            mFee = NumberUtil.getEthFromWei(mQuota);
            feeValueText.setText(mFee + getFeeTokenUnit());
        } else {
            mFee = 0;
            feeValueText.setText(mFee + "");
        }
    }

    /**
     * struct confirm transfer view
     *
     * @return
     */
    @SuppressLint("SetTextI18n")
    private void getConfirmTransferView() {
        String value = transferValueEdit.getText().toString().trim();
        transferDialog = new TransferDialog(this, (password, progressBar) -> {
            this.progressBar = progressBar;
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!AESCrypt.checkPassword(password, walletItem)) {
                Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            } else {
                transferDialog.setButtonClickAble(false);
                progressBar.setVisibility(View.VISIBLE);
                if (isETH()) {
                    SensorDataTrackUtils.transferAccount(tokenItem.symbol, value, receiveAddressEdit.getText().toString().trim(), walletItem.address, ConstUtil.ETH, "2");
                    if (ConstUtil.ETH.equals(tokenItem.symbol)) {
                        transferEth(password, value);
                    } else {
                        transferEthErc20(password, value);
                    }
                } else {
                    SensorDataTrackUtils.transferAccount(tokenItem.symbol, value, receiveAddressEdit.getText().toString().trim(), walletItem.address, tokenItem.chainName, "2");
                    if (TextUtils.isEmpty(tokenItem.contractAddress)) {
                        transferNervosToken(password, Double.valueOf(value));
                    } else {
                        transferNervosErc20(password, Double.valueOf(value));
                    }
                }
            }
        });
        transferDialog.setConfirmData(walletItem.address, receiveAddressEdit.getText().toString(),
                value + tokenItem.symbol, feeSeekText.getText().toString());
    }

    /**
     * transfer origin token of nervos
     *
     * @param value transfer value
     */
    private void transferNervosToken(String password, double value) {
        transactionHexData = payHexDataEdit.getText().toString().trim();
        NervosRpcService.transferNervos(receiveAddressEdit.getText().toString().trim(), value,
                transactionHexData, tokenItem.chainId, password)
                .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        transferNervosError(e);
                    }

                    @Override
                    public void onNext(AppSendTransaction appSendTransaction) {
                        transferNervosNormal(appSendTransaction);
                    }
                });
    }


    /**
     * transfer erc20 token of nervos
     *
     * @param value transfer value
     */
    private void transferNervosErc20(String password, double value) {
        NervosRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(tokenItem.chainId));
        try {
            NervosRpcService.transferErc20(tokenItem, tokenItem.contractAddress,
                    receiveAddressEdit.getText().toString().trim(), value, tokenItem.chainId, password)
                    .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                        @Override
                        public void onError(Throwable e) {
                            transferNervosError(e);
                        }

                        @Override
                        public void onNext(AppSendTransaction appSendTransaction) {
                            transferNervosNormal(appSendTransaction);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transferNervosNormal(AppSendTransaction appSendTransaction) {
        progressBar.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            transferDialog.dismiss();
            finish();
        } else if (appSendTransaction.getError() != null &&
                !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            Toast.makeText(mActivity, appSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void transferNervosError(Throwable e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(TransferActivity.this,
                e.getMessage(), Toast.LENGTH_SHORT).show();
        transferDialog.dismiss();
    }


    /**
     * transfer origin token of ethereum
     *
     * @param value
     */
    private void transferEth(String password, String value) {
        transactionHexData = payHexDataEdit.getText().toString().trim();
        EthRpcService.transferEth(receiveAddressEdit.getText().toString().trim(),
                Double.valueOf(value), mGasPrice, mGasLimit, transactionHexData, password)
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        transferNervosError(e);
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        transferEthereumNormal(ethSendTransaction);
                    }
                });
    }


    /**
     * transfer origin token of ethereum
     *
     * @param value
     */
    private void transferEthErc20(String password, String value) {
        EthRpcService.transferErc20(tokenItem, receiveAddressEdit.getText().toString().trim(),
                Double.valueOf(value), mGasPrice, mGasLimit, password)
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        transferEthereumError(e);
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        transferEthereumNormal(ethSendTransaction);
                    }
                });
    }


    private void transferEthereumNormal(EthSendTransaction ethSendTransaction) {
        progressBar.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            transferDialog.dismiss();
            finish();
        } else if (ethSendTransaction.getError() != null &&
                !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
            Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void transferEthereumError(Throwable e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(TransferActivity.this,
                e.getMessage(), Toast.LENGTH_SHORT).show();
        transferDialog.dismiss();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (transferDialog != null) {
            transferDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SCAN:
                if (null == data) return;
                Bundle bundle = data.getExtras();
                if (bundle == null) return;
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                        case org.nervos.neuron.util.QRUtils.CodeUtils.STRING_ADDRESS:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            receiveAddressEdit.setText(result);
                            break;
                        default:
                            Toast.makeText(this, R.string.address_error,
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    QrCodeActivity.track("1", false);
                    Toast.makeText(TransferActivity.this, R.string.qrcode_handle_fail,
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
