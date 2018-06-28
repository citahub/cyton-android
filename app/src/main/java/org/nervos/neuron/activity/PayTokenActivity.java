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
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TransactionRequest;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
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

    private TransactionRequest transactionRequest;
    private WalletItem walletItem;
    private BottomSheetDialog sheetDialog;
    private ChainItem chainItem;

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
            transactionRequest = new Gson().fromJson(payload, TransactionRequest.class);
        }
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        chainItem = getIntent().getParcelableExtra(AppWebActivity.EXTRA_CHAIN);
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
        payNameText.setText(chainItem.entry);
        if (transactionRequest.isEthereum()) {
            payAddressText.setText(transactionRequest.to);
            payAmountText.setText(NumberUtil.getDecimal_4(transactionRequest.getValue()));
            paySumText.setText(NumberUtil.getDecimal_4(transactionRequest.getValue()
                    + transactionRequest.getGas()));
            payDataText.setText(transactionRequest.data);
        } else {
            payAddressText.setText(transactionRequest.to);
            payAmountText.setText(NumberUtil.getDecimal_4(transactionRequest.getValue()));
            paySumText.setText(NumberUtil.getDecimal_4(transactionRequest.getValue()
                    + transactionRequest.getQuota()));
            payDataText.setText(transactionRequest.data);
        }

        findViewById(R.id.sign_hex_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.pay_data_left_line).setVisibility(View.VISIBLE);
                findViewById(R.id.pay_data_right_line).setVisibility(View.GONE);
                payDataText.setText(transactionRequest.data);
            }
        });

        findViewById(R.id.sign_utf8_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.pay_data_left_line).setVisibility(View.GONE);
                findViewById(R.id.pay_data_right_line).setVisibility(View.VISIBLE);
                if (Numeric.containsHexPrefix(transactionRequest.data)) {
                    payDataText.setText(NumberUtil.hexToUtf8(transactionRequest.data));
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
        toAddress.setText(transactionRequest.to);
        valueText.setText(NumberUtil.getDecimal_4(transactionRequest.getValue()));
        if (transactionRequest.isEthereum()) {
            feeConfirmText.setText(NumberUtil.getDecimal_4(transactionRequest.getGas()));
        } else {
            feeConfirmText.setText(NumberUtil.getDecimal_4(transactionRequest.getQuota()));
        }

        view.findViewById(R.id.close_layout).setOnClickListener(v -> sheetDialog.dismiss());
        view.findViewById(R.id.transfer_confirm_button).setOnClickListener(v ->
                showPasswordConfirmView(progressBar));
        return view;
    }


    private void showPasswordConfirmView(ProgressBar progressBar) {
        SimpleDialog simpleDialog = new SimpleDialog(mActivity);
        simpleDialog.setTitle(R.string.input_password_hint);
        simpleDialog.setMessageHint("password");
        simpleDialog.setEditInputType(SimpleDialog.PASSWORD);
        simpleDialog.setOnOkClickListener(new SimpleDialog.OnOkClickListener() {
            @Override
            public void onOkClick() {
                if (TextUtils.isEmpty(simpleDialog.getMessage())) {
                    Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
                    return;
                } else if (!AESCrypt.checkPassword(simpleDialog.getMessage(), walletItem)) {
                    Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
                simpleDialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
                if (transactionRequest.isEthereum()) {
                    transferEth(simpleDialog.getMessage(), progressBar);
                } else {
                    transferNervos(simpleDialog.getMessage(), progressBar);
                }

            }
        });
        simpleDialog.setOnCancelClickListener(() -> simpleDialog.dismiss());
        simpleDialog.show();
    }


    private void transferEth(String password, ProgressBar progressBar) {
        EthRpcService.getEthGasPrice()
            .flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
                @Override
                public Observable<EthSendTransaction> call(BigInteger gasPrice) {
                    return EthRpcService.transferEth(transactionRequest.to,
                            transactionRequest.getValue(), gasPrice, password);
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
            });
    }

    private void transferNervos(String password, ProgressBar progressBar) {
        NervosRpcService.transferNervos(transactionRequest.to, transactionRequest.getValue(), password)
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
                    if (!TextUtils.isEmpty(ethSendTransaction.getSendTransactionResult().getHash())) {
                        sheetDialog.dismiss();
                        DBChainUtil.saveChain(mActivity, chainItem);
                        Toast.makeText(mActivity, R.string.transfer_success, Toast.LENGTH_SHORT).show();
                        gotoSignSuccess(ethSendTransaction.getSendTransactionResult().getHash());
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
            });
    }


    private void gotoSignSuccess(String hexHash) {
        LogUtil.d("hexHash: " + hexHash);
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
