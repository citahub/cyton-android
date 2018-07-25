package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PayTokenActivity extends BaseActivity {

    public static final String EXTRA_HEX_HASH = "extra_hex_hash";
    public static final String EXTRA_PAY_ERROR = "extra_pay_error";

    private TransactionInfo transactionInfo;
    private WalletItem walletItem;
    private BottomSheetDialog sheetDialog;
    private AppItem appItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_token);

        EthRpcService.init(this);

        initData();
        initView();
        initListener();
    }

    private void initData() {
        String payload = getIntent().getStringExtra(AppWebActivity.EXTRA_PAYLOAD);
        if (!TextUtils.isEmpty(payload)) {
            transactionInfo = new Gson().fromJson(payload, TransactionInfo.class);
        }
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        appItem = getIntent().getParcelableExtra(AppWebActivity.EXTRA_CHAIN);
    }


    private void initView() {
        TextView walletNameText = findViewById(R.id.wallet_name);
        TextView walletAddressText = findViewById(R.id.wallet_address);
        TextView payNameText = findViewById(R.id.pay_owner);
        TextView payAddressText = findViewById(R.id.pay_address);
        TextView payAmountText = findViewById(R.id.pay_amount);
        TextView paySumText = findViewById(R.id.pay_sum);
        TextView payDataText = findViewById(R.id.pay_data);
        CircleImageView photoImage = findViewById(R.id.wallet_photo);

        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        payNameText.setText(appItem.entry);
        payDataText.setText(transactionInfo.data);
        payAddressText.setText(transactionInfo.to);
        if (transactionInfo.isEthereum()) {
            payAmountText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()));
            paySumText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()
                    + transactionInfo.getGas()));
            if (TextUtils.isEmpty(transactionInfo.gasPrice) || "0".equals(transactionInfo.gasPrice)) {
                showProgressCircle();
                EthRpcService.getEthGasPrice().subscribe(new Subscriber<BigInteger>() {
                    @Override
                    public void onCompleted() {
                        dismissProgressCircle();
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        dismissProgressCircle();
                    }
                    @Override
                    public void onNext(BigInteger gasPrice) {
                        transactionInfo.gasPrice = gasPrice.toString(16);
                        paySumText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()
                                + transactionInfo.getGas()));
                    }
                });
            }
        } else {
            payAmountText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()));
            paySumText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()
                    + transactionInfo.getQuota()));
        }

        findViewById(R.id.sign_hex_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.pay_data_left_line).setVisibility(View.VISIBLE);
                findViewById(R.id.pay_data_right_line).setVisibility(View.GONE);
                payDataText.setText(transactionInfo.data);
            }
        });

        findViewById(R.id.sign_utf8_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.pay_data_left_line).setVisibility(View.GONE);
                findViewById(R.id.pay_data_right_line).setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(transactionInfo.data) &&
                        Numeric.containsHexPrefix(transactionInfo.data)) {
                    payDataText.setText(NumberUtil.hexToUtf8(transactionInfo.data));
                }
            }
        });

    }

    private void initListener() {
        findViewById(R.id.pay_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(AppWebActivity.RESULT_CODE_CANCEL);
                finish();
            }
        });
        findViewById(R.id.pay_approve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog = new BottomSheetDialog(mActivity);
                sheetDialog.setCancelable(false);
                sheetDialog.setContentView(getConfirmTransferView(sheetDialog));
                sheetDialog.show();
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
        toAddress.setText(transactionInfo.to);
        valueText.setText(NumberUtil.getDecimal_6(transactionInfo.getValue()));
        feeConfirmText.setText(NumberUtil.getDecimal_6(transactionInfo.isEthereum()?
                transactionInfo.getGas():transactionInfo.getQuota()));
        view.findViewById(R.id.close_layout).setOnClickListener(v -> sheetDialog.dismiss());
        view.findViewById(R.id.transfer_confirm_button).setOnClickListener(v ->
                showPasswordConfirmView(progressBar));
        return view;
    }


    private void showPasswordConfirmView(ProgressBar progressBar) {
        SimpleDialog simpleDialog = new SimpleDialog(mActivity);
        simpleDialog.setTitle(R.string.input_password_hint);
        simpleDialog.setMessageHint(R.string.input_password_hint);
        simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
        simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
            @Override
            public void onOkClick() {
                if (TextUtils.isEmpty(simpleDialog.getMessage())) {
                    Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
                } else if (!AESCrypt.checkPassword(simpleDialog.getMessage(), walletItem)) {
                    Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
                } else {
                    simpleDialog.dismiss();
                    progressBar.setVisibility(View.VISIBLE);
                    if (transactionInfo.isEthereum()) {
                        transferEth(simpleDialog.getMessage(), progressBar);
                    } else {
                        transferNervos(simpleDialog.getMessage(), progressBar);
                    }
                }
            }
        });
        simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
        simpleDialog.show();
    }


    private void transferEth(String password, ProgressBar progressBar) {
        Observable.just(transactionInfo.gasPrice)
            .flatMap(new Func1<String, Observable<BigInteger>>() {
                @Override
                public Observable<BigInteger> call(String gasPrice) {
                    if (TextUtils.isEmpty(transactionInfo.gasPrice)
                            || "0".equals(transactionInfo.gasPrice)) {
                        return EthRpcService.getEthGasPrice();
                    } else {
                        return Observable.just(Numeric.toBigInt(gasPrice));
                    }
                }
            }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
                @Override
                public Observable<EthSendTransaction> call(BigInteger gasPrice) {
                    return EthRpcService.transferEth(transactionInfo.to,
                            transactionInfo.getValue(), gasPrice, password);
                }
            }).subscribeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<EthSendTransaction>() {
                @Override
                public void onCompleted() {
                    progressBar.setVisibility(View.GONE);
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                    gotoSignFail(e.getMessage());
                }
                @Override
                public void onNext(EthSendTransaction ethSendTransaction) {
                    handleTransfer(ethSendTransaction);
                }
            });
    }

    private void transferNervos(String password, ProgressBar progressBar) {
        NervosRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(transactionInfo.chainId));
        NervosRpcService.transferNervos(transactionInfo.to, transactionInfo.getValue(),
                transactionInfo.data, password)
            .subscribe(new Subscriber<org.nervos.web3j.protocol.core.methods.response.EthSendTransaction>() {
                @Override
                public void onCompleted() {
                    progressBar.setVisibility(View.GONE);
                }
                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    gotoSignFail(e.getMessage());
                }
                @Override
                public void onNext(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction ethSendTransaction) {
                    handleTransfer(ethSendTransaction);
                }
            });
    }

    /**
     * handle ethereum transfer result
     * @param ethSendTransaction   result of ethereum transaction
     */
    private void handleTransfer(EthSendTransaction ethSendTransaction) {
        if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            sheetDialog.dismiss();
            Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(ethSendTransaction.getTransactionHash());
        } else if (ethSendTransaction.getError() != null &&
                !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())){
            sheetDialog.dismiss();
            Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(ethSendTransaction.getError().getMessage());
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getString(R.string.transfer_fail));
        }
    }

    /**
     * handle nervos transfer result
     * @param nervosSendTransaction   result of nervos transaction
     */
    private void handleTransfer(org.nervos.web3j.protocol.core.methods.response.EthSendTransaction nervosSendTransaction) {
        if (!TextUtils.isEmpty(nervosSendTransaction.getSendTransactionResult().getHash())) {
            sheetDialog.dismiss();
            Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(nervosSendTransaction.getSendTransactionResult().getHash());
        } else if (nervosSendTransaction.getError() != null &&
                !TextUtils.isEmpty(nervosSendTransaction.getError().getMessage())){
            sheetDialog.dismiss();
            Toast.makeText(mActivity, nervosSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(nervosSendTransaction.getError().getMessage());
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getString(R.string.transfer_fail));
        }
    }


    private void gotoSignSuccess(String hexHash) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_HEX_HASH, hexHash);
        setResult(AppWebActivity.RESULT_CODE_SUCCESS, intent);
        finish();
    }

    private void gotoSignFail(String error) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PAY_ERROR, error);
        setResult(AppWebActivity.RESULT_CODE_FAIL, intent);
        finish();
    }

}
