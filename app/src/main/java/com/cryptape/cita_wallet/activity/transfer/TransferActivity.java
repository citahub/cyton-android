package com.cryptape.cita_wallet.activity.transfer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.greenrobot.eventbus.EventBus;
import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.activity.AdvanceSetupActivity;
import com.cryptape.cita_wallet.activity.NBaseActivity;
import com.cryptape.cita_wallet.activity.QrCodeActivity;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.event.TransferPushEvent;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.transaction.AppTransaction;
import com.cryptape.cita_wallet.service.http.EthRpcService;
import com.cryptape.cita_wallet.service.http.WalletService;
import com.cryptape.cita_wallet.util.AddressUtil;
import com.cryptape.cita_wallet.util.Blockies;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.ether.EtherUtil;
import com.cryptape.cita_wallet.util.permission.PermissionUtil;
import com.cryptape.cita_wallet.util.permission.RuntimeRationale;
import com.cryptape.cita_wallet.util.qrcode.CodeUtils;
import com.cryptape.cita_wallet.view.CompressEditText;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.button.CommonButton;
import com.cryptape.cita_wallet.view.dialog.ToastDoubleButtonDialog;
import com.cryptape.cita_wallet.view.dialog.TransferDialog;
import com.cryptape.cita_wallet.view.dialog.listener.OnDialogCancelClickListener;
import com.cryptape.cita_wallet.view.dialog.listener.OnDialogOKClickListener;
import com.cryptape.cita_wallet.view.tool.CytonTextWatcher;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by duanyytop on 2018/11/4
 */
public class TransferActivity extends NBaseActivity implements TransferView {

    private static final int REQUEST_CODE_SCAN = 0x01;
    private static final int REQUEST_CODE_TRANSACTION = 0x02;
    public static final String EXTRA_TOKEN = "extra_token";
    public static final String EXTRA_ADDRESS = "extra_address";

    private TransferPresenter mPresenter;

    private TextView walletAddressText, walletNameText, feeValueText, balanceText;
    private ImageView scanImage;
    private CompressEditText mCetReceiverAddress;
    private AppCompatEditText  transferValueEdit;
    private CommonButton nextActionButton;
    private CircleImageView photoImage;
    private ProgressBar progressBar;

    private TitleBar titleBar;
    private TransferDialog transferDialog;
    private AppTransaction mAppTransaction = new AppTransaction();

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
        balanceText = findViewById(R.id.wallet_balance_text);
        feeValueText = findViewById(R.id.fee_value_text);
        mCetReceiverAddress = findViewById(R.id.cet_address);
        transferValueEdit = findViewById(R.id.transfer_value);
        photoImage = findViewById(R.id.wallet_photo);
        titleBar = findViewById(R.id.title);
    }

    @Override
    protected void initData() {
        mPresenter = new TransferPresenter(mActivity, this);
    }

    @Override
    public void updateTitleData(String title) {
        titleBar.setTitle(title);
    }

    @Override
    public void updateWalletData(Wallet wallet) {
        walletAddressText.setText(wallet.address);
        walletNameText.setText(wallet.name);
        photoImage.setImageBitmap(Blockies.createIcon(wallet.address));
    }

    @Override
    public void updaterReceiveAddress(String address) {
        mCetReceiverAddress.setText(address);
    }

    @Override
    public void updateAnyTokenBalance(Double balance) {
        balanceText.setText(String.format(getString(R.string.transfer_balance_place_holder),
                CurrencyUtil.fmtMicrometer(NumberUtil.getDecimalValid_8(balance)) + " " + mPresenter.getTokenItem().symbol));
    }

    @Override
    public void updateNativeTokenBalance(Double balance) {
        balanceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPresenter.isTransferFeeEnough()) {
                    Toast.makeText(mActivity, String.format(getString(R.string.balance_more_gas),
                            mPresenter.getFeeTokenUnit()), Toast.LENGTH_SHORT).show();
                    return;
                }
                transferValueEdit.setText(mPresenter.balanceSubFee());
            }
        });
    }

    @Override
    public void startUpdateEthGasPrice() {
        showProgressCircle();
    }

    @Override
    public void updateEthGasPriceSuccess(BigInteger gasPrice) {
        dismissProgressCircle();

    }

    @Override
    public void updateEthGasPriceFail(Throwable throwable) {
        Toast.makeText(mActivity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
        dismissProgressCircle();
    }

    @Override
    public void initTransferFeeView() {
        feeValueText.setText(mPresenter.getTransferFee());
        initAdvancedSetup();
    }

    @Override
    public void updateCITAQuota(String quotaFee) {
        feeValueText.setText(quotaFee);
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
            String receiveAddressValue = mCetReceiverAddress.getText();
            String transferValue = transferValueEdit.getText().toString().trim();
            try {
                Double.parseDouble(transferValue);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mActivity, R.string.input_correct_value_tip, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(receiveAddressValue)) {
                Toast.makeText(mActivity, R.string.transfer_address_not_null, Toast.LENGTH_SHORT).show();
            } else if (!AddressUtil.isAddressValid(receiveAddressValue)) {
                Toast.makeText(mActivity, R.string.address_error, Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(transferValue)) {
                Toast.makeText(mActivity, R.string.transfer_amount_not_null, Toast.LENGTH_SHORT).show();
            } else if (!mPresenter.isTransferFeeEnough()) {
                Toast.makeText(mActivity, String.format(getString(R.string.balance_not_enough_fee),
                        mPresenter.getFeeTokenUnit()), Toast.LENGTH_SHORT).show();
            } else if (mPresenter.checkTransferValueMoreBalance(transferValue)) {
                handleBigTransferValue();
            } else {
                getConfirmTransferView();
            }
        });
    }

    /**
     * If balance of wallet is more than gas fee, neuron will show dialog to give user two choices
     */
    private void handleBigTransferValue() {
        ToastDoubleButtonDialog dialog = ToastDoubleButtonDialog.getInstance(mActivity,
                getString(R.string.all_balance_transfer_tip));
        dialog.setOnOkClickListener(new OnDialogOKClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                transferValueEdit.setText(mPresenter.balanceSubFee());
                getConfirmTransferView();
                dialog.dismiss();
            }
        });
        dialog.setOnCancelClickListener(new OnDialogCancelClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                transferValueEdit.setText("");
                dialog.dismiss();
            }
        });
    }


    /**
     * Estimate Gas limit when address edit text and value edit text were not null
     */
    private boolean isAddressOk = false, isValueOk = false;

    @Override
    public void initTransferEditValue() {
        mCetReceiverAddress.setTextWatcher(new CytonTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                isAddressOk = AddressUtil.isAddressValid(s.toString());
                updateTransferEditValue();
            }
        });
        transferValueEdit.addTextChangedListener(new CytonTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                try {
                    if (!TextUtils.isEmpty(s)) {
                        Double.parseDouble(s.toString());
                        isValueOk = true;
                        updateTransferEditValue();
                    } else {
                        isValueOk = false;
                        Toast.makeText(mActivity, R.string.input_correct_value_tip, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    isValueOk = false;
                    Toast.makeText(mActivity, R.string.input_correct_value_tip, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateTransferEditValue() {
        if (isAddressOk && isValueOk) {
            initAdvancedSetup();
            mPresenter.initGasLimit(getTransactionInfo());
        }
    }


    private AppTransaction getTransactionInfo() {
        AppTransaction appTransaction = new AppTransaction(mPresenter.getTokenItem().contractAddress, "0");
        appTransaction.data = EthRpcService.createTokenTransferData(mCetReceiverAddress.getText(),
                Convert.toWei(transferValueEdit.getText().toString(), Convert.Unit.ETHER).toBigInteger());
        return appTransaction;
    }

    private void initAdvancedSetup() {
        feeValueText.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        feeValueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, AdvanceSetupActivity.class);
                intent.putExtra(AdvanceSetupActivity.EXTRA_TRANSFER, true);
                intent.putExtra(AdvanceSetupActivity.EXTRA_NATIVE_TOKEN, mPresenter.isNativeToken());
                mAppTransaction.chainType = mPresenter.isEther() ? ConstantUtil.TYPE_ETH : ConstantUtil.TYPE_CITA;
                mAppTransaction.data = mPresenter.getData();
                if (mPresenter.isEther()) {
                    mAppTransaction.setGasLimit(mPresenter.getGasLimit());
                    mAppTransaction.setGasPrice(mPresenter.getGasPrice());
                    mAppTransaction.chainId = EtherUtil.getEtherId();
                } else {
                    mAppTransaction.chainId = mPresenter.getTokenItem().getChainId();
                    mAppTransaction.setQuota(mPresenter.getQuotaLimit().toString());
                }
                intent.putExtra(AdvanceSetupActivity.EXTRA_ADVANCE_SETUP, mAppTransaction);
                startActivityForResult(intent, REQUEST_CODE_TRANSACTION);
            }
        });
    }


    /**
     * show confirm transfer view
     */
    private void getConfirmTransferView() {
        if (isFastDoubleClick()) return;
        String transferValue = transferValueEdit.getText().toString().trim();
        String receiveAddress = mCetReceiverAddress.getText();
        transferDialog = new TransferDialog(this, (password, progressBar) -> {
            this.progressBar = progressBar;
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!WalletService.checkPassword(mActivity, password, mPresenter.getWalletItem())) {
                Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            } else {
                transferDialog.setButtonClickAble(false);
                progressBar.setVisibility(View.VISIBLE);
                mPresenter.handleTransferAction(password, transferValue, receiveAddress);
            }
        });
        transferDialog.setConfirmData(mPresenter.getWalletItem().address, receiveAddress,
                NumberUtil.getDecimalValid_8(Double.parseDouble(transferValue)) + mPresenter.getTokenItem().symbol,
                feeValueText.getText().toString());
    }


    @Override
    public void transferCITASuccess(AppSendTransaction appSendTransaction) {
        progressBar.setVisibility(View.GONE);
        if (appSendTransaction == null) {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        } else if (appSendTransaction.getError() != null
                && !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            Toast.makeText(mActivity, appSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            Toast.makeText(TransferActivity.this, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            transferDialog.dismiss();
            EventBus.getDefault().post(new TransferPushEvent());
            finish();
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void transferCITAFail(Throwable e) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(TransferActivity.this,
                e.getMessage(), Toast.LENGTH_SHORT).show();
        transferDialog.dismiss();
    }

    @Override
    public void transferEtherSuccess(EthSendTransaction ethSendTransaction) {
        progressBar.setVisibility(View.GONE);
        if (ethSendTransaction == null) {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        } else if (ethSendTransaction.getError() != null
                && !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
            Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            transferDialog.dismiss();
            EventBus.getDefault().post(new TransferPushEvent());
            finish();
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void transferEtherFail(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
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
                        case com.cryptape.cita_wallet.util.qrcode.CodeUtils.STRING_ADDRESS:
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            mCetReceiverAddress.setText(result);
                            break;
                        default:
                            Toast.makeText(this, R.string.address_error,
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(TransferActivity.this, R.string.qrcode_handle_fail,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_TRANSACTION:
                switch (resultCode) {
                    case AdvanceSetupActivity.RESULT_TRANSACTION:
                        mAppTransaction = data.getParcelableExtra(AdvanceSetupActivity.EXTRA_TRANSACTION);
                        mPresenter.updateData(mAppTransaction.data);
                        if (mAppTransaction.isEthereum()) {
                            mPresenter.updateGasLimit(mAppTransaction.getGasLimit());
                            mPresenter.updateGasPrice(mAppTransaction.getGasPrice());
                        } else {
                            mPresenter.updateQuotaLimit(mAppTransaction.getQuota());
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

}
