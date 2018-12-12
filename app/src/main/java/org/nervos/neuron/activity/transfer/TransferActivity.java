package org.nervos.neuron.activity.transfer;

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
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import de.hdodenhof.circleimageview.CircleImageView;
import org.greenrobot.eventbus.EventBus;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AdvanceSetupActivity;
import org.nervos.neuron.activity.NBaseActivity;
import org.nervos.neuron.activity.QrCodeActivity;
import org.nervos.neuron.event.TransferPushEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.item.transaction.TransactionInfo;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.service.http.WalletService;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.qrcode.CodeUtils;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.button.CommonButton;
import org.nervos.neuron.view.dialog.ToastDoubleButtonDialog;
import org.nervos.neuron.view.dialog.TransferDialog;
import org.nervos.neuron.view.dialog.listener.OnDialogCancelClickListener;
import org.nervos.neuron.view.dialog.listener.OnDialogOKClickListener;
import org.nervos.neuron.view.tool.NeuronTextWatcher;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigInteger;

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
    private AppCompatEditText receiveAddressEdit, transferValueEdit;
    private CommonButton nextActionButton;
    private CircleImageView photoImage;
    private ProgressBar progressBar;

    private TitleBar titleBar;
    private TransferDialog transferDialog;
    private TransactionInfo mTransactionInfo = new TransactionInfo();

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
        receiveAddressEdit = findViewById(R.id.transfer_address);
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
    public void updateWalletData(WalletItem walletItem) {
        walletAddressText.setText(walletItem.address);
        walletNameText.setText(walletItem.name);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
    }

    @Override
    public void updaterReceiveAddress(String address) {
        receiveAddressEdit.setText(address);
    }

    @Override
    public void updateAnyTokenBalance(Double balance) {
        balanceText.setText(String.format(
                getString(R.string.transfer_balance_place_holder),
                NumberUtil.getDecimal8ENotation(balance) + " " + mPresenter.getTokenItem().symbol));
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
    public void updateAppChainQuota(String quotaFee) {
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
            String receiveAddressValue = receiveAddressEdit.getText().toString().trim();
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
        receiveAddressEdit.addTextChangedListener(new NeuronTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                isAddressOk = AddressUtil.isAddressValid(s.toString());
                updateTransferEditValue();
            }
        });
        transferValueEdit.addTextChangedListener(new NeuronTextWatcher() {
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


    private TransactionInfo getTransactionInfo() {
        TransactionInfo transactionInfo = new TransactionInfo(mPresenter.getTokenItem().contractAddress, "0");
        transactionInfo.data = EthRpcService.createTokenTransferData(receiveAddressEdit.getText().toString(),
                Convert.toWei(transferValueEdit.getText().toString(), Convert.Unit.ETHER).toBigInteger());
        return transactionInfo;
    }

    private void initAdvancedSetup() {
        feeValueText.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        feeValueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, AdvanceSetupActivity.class);
                intent.putExtra(AdvanceSetupActivity.EXTRA_TRANSFER, true);
                intent.putExtra(AdvanceSetupActivity.EXTRA_NATIVE_TOKEN, mPresenter.isNativeToken());
                if (mPresenter.isEther()) {
                    mTransactionInfo.setGasLimit(mPresenter.getGasLimit());
                    mTransactionInfo.setGasPrice(mPresenter.getEthGasDefaultPrice());
                    mTransactionInfo.chainType = mPresenter.isEther() ? ConstantUtil.TYPE_ETH : ConstantUtil.TYPE_APPCHAIN;
                    mTransactionInfo.chainId = EtherUtil.getEtherId();
                } else {
                    mTransactionInfo.chainId = mPresenter.getTokenItem().getChainId();
                    mTransactionInfo.setQuota(mPresenter.isNativeToken()
                            ? ConstantUtil.QUOTA_TOKEN.toString() : ConstantUtil.QUOTA_ERC20.toString());
                }
                intent.putExtra(AdvanceSetupActivity.EXTRA_ADVANCE_SETUP, mTransactionInfo);
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
        String receiveAddress = receiveAddressEdit.getText().toString().trim();
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
                NumberUtil.getDecimal8ENotation(Double.parseDouble(transferValue)) + mPresenter.getTokenItem().symbol,
                feeValueText.getText().toString());
    }


    @Override
    public void transferAppChainSuccess(AppSendTransaction appSendTransaction) {
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
    public void transferAppChainFail(Throwable e) {
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
                        case org.nervos.neuron.util.qrcode.CodeUtils.STRING_ADDRESS:
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
            case REQUEST_CODE_TRANSACTION:
                switch (resultCode) {
                    case AdvanceSetupActivity.RESULT_TRANSACTION:
                        mTransactionInfo = data.getParcelableExtra(AdvanceSetupActivity.EXTRA_TRANSACTION);
                        mPresenter.updateData(mTransactionInfo.data);
                        if (mTransactionInfo.isEthereum()) {
                            mPresenter.updateGasLimit(mTransactionInfo.getGasLimit());
                            mPresenter.updateGasPrice(mTransactionInfo.getGasPrice());
                        } else {
                            mPresenter.updateQuotaLimit(mTransactionInfo.getQuota());
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
