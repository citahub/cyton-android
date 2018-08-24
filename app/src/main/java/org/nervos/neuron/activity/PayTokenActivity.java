package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.dialog.SimpleDialog;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
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
    private TextView walletNameText, walletAddressText, payNameText, payAmountUnitText;
    private TextView payAddressText, payAmountText, payFeeText, payDataText, payFeeUnitText;
    private CircleImageView photoImage;

    private double mBalance = 0.0f;
    private TokenItem tokenItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_token);

        initData();
        initView();
        initRemoteData();
        initAction();
    }

    private void initData() {
        EthRpcService.init(this);

        String payload = getIntent().getStringExtra(AppWebActivity.EXTRA_PAYLOAD);
        if (!TextUtils.isEmpty(payload)) {
            transactionInfo = new Gson().fromJson(payload, TransactionInfo.class);
        }
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        appItem = getIntent().getParcelableExtra(AppWebActivity.EXTRA_CHAIN);
    }


    @SuppressLint("SetTextI18n")
    private void initView() {
        walletNameText = findViewById(R.id.wallet_name);
        walletAddressText = findViewById(R.id.wallet_address);
        payNameText = findViewById(R.id.pay_owner);
        payAddressText = findViewById(R.id.pay_address);
        payAmountText = findViewById(R.id.pay_amount);
        payFeeText = findViewById(R.id.pay_fee);
        payDataText = findViewById(R.id.pay_data);
        photoImage = findViewById(R.id.wallet_photo);
        payAmountUnitText = findViewById(R.id.pay_amount_unit);
        payFeeUnitText = findViewById(R.id.pay_fee_unit);

        payDataText.setMovementMethod(ScrollingMovementMethod.getInstance());
        walletNameText.setText(walletItem.name);
        walletAddressText.setText(walletItem.address);
        photoImage.setImageBitmap(Blockies.createIcon(walletItem.address));
        payNameText.setText(appItem.entry);
        payDataText.setText(transactionInfo.data);
        payAddressText.setText(transactionInfo.to);

        payAmountUnitText.setText(getNativeToken());
        payFeeUnitText.setText(getNativeToken());

        if (transactionInfo.isEthereum()) {
            payAmountText.setText(NumberUtil.getDecimal_10(transactionInfo.getValue()));
            payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
        } else {
            payAmountText.setText(NumberUtil.getDecimal_10(transactionInfo.getValue()));
            payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleQuota()));
        }

    }

    private void initRemoteData() {
        initBalance();
        if (transactionInfo.isEthereum() &&
                (TextUtils.isEmpty(transactionInfo.gasPrice) || "0".equals(transactionInfo.gasPrice))) {
            showEtherGas();
        }
    }

    private void showEtherGas() {
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasPrice) {
                transactionInfo.gasPrice = gasPrice.toString(16);
                payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
            }
        });
    }

    private void initBalance() {
        ChainItem chainItem = DBChainUtil.getChain(mActivity, transactionInfo.chainId);
        if (chainItem == null) return ;
        tokenItem = new TokenItem(chainItem);
        WalletService.getBalanceWithToken(mActivity, tokenItem).subscribe(new Subscriber<Double>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {

            }
            @Override
            public void onNext(Double balance) {
                mBalance = balance;
            }
        });
    }

    private void initAction() {
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
                if (mBalance < (transactionInfo.isEthereum()?
                        transactionInfo.getGas() : transactionInfo.getDoubleQuota())) {
                    String toastMessage = tokenItem == null ? getString(R.string.token) : tokenItem.symbol;
                    Toast.makeText(mActivity, String.format(getString(R.string.balance_not_enough),
                            toastMessage), Toast.LENGTH_SHORT).show();
                    return;
                }
                sheetDialog = new BottomSheetDialog(mActivity);
                sheetDialog.setCancelable(false);
                sheetDialog.setContentView(getConfirmTransferView(sheetDialog));
                sheetDialog.show();
            }
        });

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

    @SuppressLint("SetTextI18n")
    private View getConfirmTransferView(BottomSheetDialog sheetDialog) {
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_transfer, null);
        TextView toAddress = view.findViewById(R.id.to_address);
        TextView fromAddress = view.findViewById(R.id.from_address);
        TextView valueText = view.findViewById(R.id.transfer_value);
        TextView feeConfirmText = view.findViewById(R.id.transfer_fee);
        ProgressBar progressBar = view.findViewById(R.id.transfer_progress);

        fromAddress.setText(walletItem.address);
        toAddress.setText(transactionInfo.to);
        valueText.setText(NumberUtil.getDecimal_10(transactionInfo.getValue()) + getNativeToken());
        if (transactionInfo.isEthereum()) {
            feeConfirmText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()) + getNativeToken());
        } else {
            feeConfirmText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleQuota()) + getNativeToken());
        }
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
                            transactionInfo.getValue(), gasPrice, ConstUtil.GAS_ERC20_LIMIT, password);
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
                transactionInfo.data, transactionInfo.getLongQuota(), (int)transactionInfo.chainId, password)
                .subscribe(new Subscriber<AppSendTransaction>() {
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
                    public void onNext(AppSendTransaction appSendTransaction) {
                        handleTransfer(appSendTransaction);
                    }
                });
    }

    /**
     * handle ethereum transfer result
     *
     * @param ethSendTransaction result of ethereum transaction
     */
    private void handleTransfer(EthSendTransaction ethSendTransaction) {
        if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            sheetDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(ethSendTransaction.getTransactionHash());
        } else if (ethSendTransaction.getError() != null &&
                !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
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
     *
     * @param appSendTransaction result of nervos transaction
     */
    private void handleTransfer(AppSendTransaction appSendTransaction) {
        if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            sheetDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(appSendTransaction.getSendTransactionResult().getHash());
        } else if (appSendTransaction.getError() != null &&
                !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            sheetDialog.dismiss();
            Toast.makeText(mActivity, appSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(appSendTransaction.getError().getMessage());
        } else {
            Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getString(R.string.transfer_fail));
        }
    }

    private String getNativeToken() {
        if (transactionInfo.isEthereum()) {
            return ConstUtil.ETH;
        } else {
            ChainItem chainItem = DBChainUtil.getChain(mActivity, transactionInfo.chainId);
            return chainItem == null? "": " " + chainItem.tokenSymbol;
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
