package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.service.NervosRpcService;
import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.crypto.AESCrypt;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.dialog.TransferDialog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PayTokenActivity extends BaseActivity {

    public static final String EXTRA_HEX_HASH = "extra_hex_hash";
    public static final String EXTRA_PAY_ERROR = "extra_pay_error";

    private TransactionInfo transactionInfo;
    private WalletItem walletItem;
    private AppItem appItem;
    private TextView walletNameText, walletAddressText, payNameText, payAmountUnitText;
    private TextView payAddressText, payAmountText, payFeeText, payDataText, payFeeUnitText;
    private CircleImageView photoImage;
    private TransferDialog transferDialog;

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
            payAmountText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleValue()));
            payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
        } else {
            payAmountText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleValue()));
            payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleQuota()));
        }

    }

    private void initRemoteData() {
        initBalance();
        if (transactionInfo.isEthereum()) {
            if (TextUtils.isEmpty(transactionInfo.gasPrice) || "0".equals(transactionInfo.gasPrice)) {
                getEtherGasPrice();
            }

            if (TextUtils.isEmpty(transactionInfo.gasLimit) || "0".equals(transactionInfo.gasLimit)) {
                getEtherGasLimit();
            }
        }
    }

    private void getEtherGasPrice() {
        showProgressCircle();
        EthRpcService.getEthGasPrice().subscribe(new NeuronSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                dismissProgressCircle();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasPrice) {
                dismissProgressCircle();
                transactionInfo.gasPrice = gasPrice.toString(16);
                payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
            }
        });
    }

    private void getEtherGasLimit() {
        showProgressCircle();
        EthRpcService.getEthGasLimit(transactionInfo).subscribe(new NeuronSubscriber<BigInteger>() {
            public void onError(Throwable e) {
                dismissProgressCircle();
                transactionInfo.gasLimit = ConstUtil.GAS_ERC20_LIMIT.toString(16);
                payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasLimit) {
                dismissProgressCircle();
                transactionInfo.gasLimit = gasLimit.toString(16);
                payFeeText.setText(NumberUtil.getDecimal8ENotation(transactionInfo.getGas()));
            }
        });
    }

    private void initBalance() {
        ChainItem chainItem = DBChainUtil.getChain(mActivity, transactionInfo.chainId);
        if (chainItem == null) return;
        tokenItem = new TokenItem(chainItem);
        WalletService.getBalanceWithToken(mActivity, tokenItem).subscribe(new NeuronSubscriber<Double>() {
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
                if (!isFastDoubleClick())
                    getConfirmTransferView();
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
    private void getConfirmTransferView() {
        transferDialog = new TransferDialog(this, (password, progressBar) -> {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!AESCrypt.checkPassword(password, walletItem)) {
                Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            } else {
                transferDialog.setButtonClickAble(false);
                progressBar.setVisibility(View.VISIBLE);
                try {
                    JSONObject object = new JSONObject();
                    object.put("target_currency", tokenItem.symbol);
                    object.put("target_currency_number", "");
                    object.put("receive_address", "");
                    object.put("outcome_address", walletItem.address);
                    object.put("transfer_type", "1");
                    if (transactionInfo.isEthereum()) {
                        object.put("target_chain", "ETH");
                        transferEth(password, progressBar);
                    } else {
                        object.put("target_chain", tokenItem.chainName);
                        transferNervos(password, progressBar);
                    }
                    SensorsDataAPI.sharedInstance().track("transfer_accounts", object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        String fee = "";
        if (transactionInfo.isEthereum()) {
            fee = NumberUtil.getDecimal8ENotation(transactionInfo.getGas()) + getNativeToken();
        } else {
            fee = NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleQuota()) + getNativeToken();
        }
        transferDialog.setConfirmData(walletItem.address, transactionInfo.to,
                NumberUtil.getDecimal8ENotation(transactionInfo.getDoubleValue()) + getNativeToken(), fee);
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
                        transactionInfo.getDoubleValue(), gasPrice,
                        Numeric.toBigInt(transactionInfo.gasLimit),
                        transactionInfo.data, password);
            }
        }).subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(mActivity, R.string.transfer_fail, Toast.LENGTH_SHORT).show();
                        gotoSignFail(e.getMessage());
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        progressBar.setVisibility(View.GONE);
                        handleTransfer(ethSendTransaction);
                    }
                });
    }

    private void transferNervos(String password, ProgressBar progressBar) {
        NervosRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(transactionInfo.chainId));
        NervosRpcService.transferNervos(transactionInfo.to, transactionInfo.getDoubleValue(),
                transactionInfo.data, transactionInfo.getLongQuota(), (int) transactionInfo.chainId, password)
                .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        gotoSignFail(e.getMessage());
                    }

                    @Override
                    public void onNext(AppSendTransaction appSendTransaction) {
                        progressBar.setVisibility(View.GONE);
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
        transferDialog.setButtonClickAble(true);
        if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            transferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(ethSendTransaction.getTransactionHash());
        } else if (ethSendTransaction.getError() != null &&
                !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
            transferDialog.dismiss();
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
        transferDialog.setButtonClickAble(true);
        if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            transferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(appSendTransaction.getSendTransactionResult().getHash());
        } else if (appSendTransaction.getError() != null &&
                !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            transferDialog.dismiss();
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
            return chainItem == null ? "" : " " + chainItem.tokenSymbol;
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
