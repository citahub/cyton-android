package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.*;
import org.nervos.neuron.item.transaction.TransactionInfo;
import org.nervos.neuron.service.http.*;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.sensor.SensorDataTrackUtils;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.dialog.DAppAdvanceSetupDialog;
import org.nervos.neuron.view.dialog.TransferDialog;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.math.BigInteger;

import static org.web3j.utils.Convert.Unit.GWEI;

/**
 * Created by duanyytop on 2018/5/28
 */
public class PayTokenActivity extends NBaseActivity implements View.OnClickListener {

    public static final String EXTRA_HEX_HASH = "extra_hex_hash";
    public static final String EXTRA_PAY_ERROR = "extra_pay_error";
    private static int ERROR_CODE = -1;

    private TransactionInfo mTransactionInfo;
    private WalletItem mWalletItem;
    private TokenItem mTokenItem;
    private AppItem mAppItem;
    private TextView mTvValue, mTvSymbol, mTvPayFee, mTvPayFeeTitle, mTvTotalFee,
            mTvReceiverName, mTvReceiverWebsite, mTvReceiverAddress, mTvSenderAddress;
    private TransferDialog mTransferDialog;
    private String mEthDefaultPrice;
    private ImageView mIvArrow;
    private ChainItem mChainItem;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_pay_token;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        mTvValue = findViewById(R.id.tv_value);
        mTvSymbol = findViewById(R.id.tv_value_symbol);
        mTvPayFeeTitle = findViewById(R.id.tv_gas_price_title);
        mTvSenderAddress = findViewById(R.id.tv_sender_address);
        mTvPayFee = findViewById(R.id.tv_gas_price);
        mTvTotalFee = findViewById(R.id.tv_total_fee);
        mTvReceiverName = findViewById(R.id.tv_receriver_name);
        mTvReceiverWebsite = findViewById(R.id.tv_receiver_website);
        mTvReceiverAddress = findViewById(R.id.tv_receiver_address);
        mIvArrow = findViewById(R.id.iv_right);
    }

    @Override
    protected void initData() {
        EthRpcService.init(this);
        String payload = getIntent().getStringExtra(AppWebActivity.EXTRA_PAYLOAD);
        if (!TextUtils.isEmpty(payload)) {
            mTransactionInfo = new Gson().fromJson(payload, TransactionInfo.class);
        }
        mWalletItem = DBWalletUtil.getCurrentWallet(mActivity);
        mAppItem = getIntent().getParcelableExtra(AppWebActivity.EXTRA_CHAIN);

        initRemoteData();
        updateView();

    }

    @SuppressLint("SetTextI18n")
    private void updateView() {
        mTvValue.setText(NumberUtil.getDecimal8ENotation(mTransactionInfo.getDoubleValue()));
        mTvSymbol.setText(getNativeToken());
        mTvSenderAddress.setText(mWalletItem.address);
        mTvReceiverWebsite.setText(getIntent().getStringExtra(AppWebActivity.RECEIVER_WEBSITE));
        mTvReceiverAddress.setText(mTransactionInfo.to);
        mTvReceiverName.setText(mAppItem.name);
        initTxFeeView();

        if (mTransactionInfo.isEthereum()) {
            mTvPayFeeTitle.setText(R.string.gas_fee);
        } else {
            mTvTotalFee.setText(NumberUtil.getDecimal8ENotation(
                    mTransactionInfo.getDoubleValue() + mTransactionInfo.getDoubleQuota())
                    + getNativeToken());
            mTvPayFee.setText(NumberUtil.getDecimal8ENotation(mTransactionInfo.getDoubleQuota())
                    + getNativeToken());
            mTvPayFeeTitle.setText(R.string.quota_fee);
        }
    }

    private void initTxFeeView() {
        mIvArrow.setVisibility(mTransactionInfo.isEthereum() ? View.VISIBLE : View.INVISIBLE);
        mTvPayFee.setEnabled(mTransactionInfo.isEthereum());
    }

    @Override
    protected void initAction() {
        findViewById(R.id.btn_next).setOnClickListener(this);
        mTvPayFee.setOnClickListener(this);
        TitleBar titleBar = findViewById(R.id.title);
        titleBar.setOnLeftClickListener(() -> {
            setResult(AppWebActivity.RESULT_CODE_CANCEL);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(AppWebActivity.RESULT_CODE_CANCEL);
        finish();
    }

    private void initRemoteData() {
        initBalance();
        if (mTransactionInfo.isEthereum()) {
            showProgressCircle();
            if (TextUtils.isEmpty(mTransactionInfo.gasPrice)
                    || "0".equals(mTransactionInfo.gasPrice)) {
                getEtherGasPrice();
            }

            if (TextUtils.isEmpty(mTransactionInfo.gasLimit)
                    || "0".equals(mTransactionInfo.gasLimit)) {
                getEtherGasLimit();
            }

            setEthGasPrice();
        }
    }

    private void getEtherGasPrice() {
        EthRpcService.getEthGasPrice().subscribe(new NeuronSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                dismissProgressCircle();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasPrice) {
                mEthDefaultPrice = gasPrice.toString(16);
                mTransactionInfo.gasPrice = mEthDefaultPrice;
                setEthGasPrice();
            }
        });
    }

    private void getEtherGasLimit() {
        EthRpcService.getEthGasLimit(mTransactionInfo)
                .subscribe(new NeuronSubscriber<BigInteger>() {
                    public void onError(Throwable e) {
                        dismissProgressCircle();
                        mTransactionInfo.gasLimit = ConstantUtil.GAS_ERC20_LIMIT.toString(16);
                        setEthGasPrice();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(BigInteger gasLimit) {
                        mTransactionInfo.gasLimit = gasLimit.multiply(ConstantUtil.GAS_LIMIT_PARAMETER).toString(16);
                        setEthGasPrice();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setEthGasPrice() {
        dismissProgressCircle();
        if (TextUtils.isEmpty(mTransactionInfo.gasLimit) || TextUtils.isEmpty(mTransactionInfo.gasPrice)) return;

        mTvPayFee.setText(NumberUtil.getDecimal8ENotation(mTransactionInfo.getGas()) + getNativeToken());
        mTvTotalFee.setText(NumberUtil.getDecimal8ENotation(
                mTransactionInfo.getDoubleValue() + mTransactionInfo.getGas()) + getNativeToken());
        CurrencyItem currencyItem = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(ConstantUtil.ETH, currencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) return;
                        try {
                            String mCurrencyPrice = NumberUtil.getDecimalValid_2(
                                    mTransactionInfo.getGas() * Double.parseDouble(price));
                            mTvPayFee.setText(NumberUtil.getDecimal8ENotation(
                                    mTransactionInfo.getGas()) + getNativeToken()
                                    + "≈" + currencyItem.getSymbol() + mCurrencyPrice);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void initBalance() {
        mChainItem = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mTransactionInfo.chainId);
        if (mChainItem == null) {
            finish();
        }
        mTokenItem = new TokenItem(mChainItem);
    }

    @SuppressLint("SetTextI18n")
    private void getConfirmTransferView() {
        mTransferDialog = new TransferDialog(this, (password, progressBar) -> {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!WalletService.checkPassword(mActivity, password, mWalletItem)) {
                Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            } else {
                mTransferDialog.setButtonClickAble(false);
                progressBar.setVisibility(View.VISIBLE);
                if (mTransactionInfo.isEthereum()) {
                    SensorDataTrackUtils.transferAccount(mTokenItem.symbol, "",
                            "", mWalletItem.address, ConstantUtil.ETH, "1");
                    transferEth(password, progressBar);
                } else {
                    SensorDataTrackUtils.transferAccount(mTokenItem.symbol, "",
                            "", mWalletItem.address, mTokenItem.chainName, "1");
                    transferAppChain(password, progressBar);
                }
            }
        });

        String fee = NumberUtil.getDecimal8ENotation(mTransactionInfo.isEthereum()
                ? mTransactionInfo.getGas() : mTransactionInfo.getDoubleQuota()) + getNativeToken();

        mTransferDialog.setConfirmData(mWalletItem.address, mTransactionInfo.to,
                NumberUtil.getDecimal8ENotation(mTransactionInfo.getDoubleValue()) + getNativeToken(), fee);
    }

    private void transferEth(String password, ProgressBar progressBar) {
        Observable.just(mTransactionInfo.gasPrice)
                .flatMap(new Func1<String, Observable<BigInteger>>() {
                    @Override
                    public Observable<BigInteger> call(String gasPrice) {
                        if (TextUtils.isEmpty(gasPrice) || "0".equals(gasPrice)) {
                            return EthRpcService.getEthGasPrice();
                        } else {
                            return Observable.just(Numeric.toBigInt(gasPrice));
                        }
                    }
                }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger gasPrice) {
                return EthRpcService.transferEth(mActivity, mTransactionInfo.to,
                        mTransactionInfo.getStringValue(), gasPrice,
                        Numeric.toBigInt(mTransactionInfo.gasLimit),
                        mTransactionInfo.data, password);
            }
        }).subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        e.printStackTrace();
                        Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
                        gotoSignFail(getCommonError());
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        progressBar.setVisibility(View.GONE);
                        handleTransfer(ethSendTransaction);
                    }
                });
    }

    private void transferAppChain(String password, ProgressBar progressBar) {
        AppChainRpcService.setHttpProvider(mChainItem.httpProvider);
        AppChainRpcService.transferAppChain(mActivity, mTransactionInfo.to,
                mTransactionInfo.getStringValue(),
                mTransactionInfo.data, mTransactionInfo.getQuota().longValue(),
                Numeric.toBigInt(mTransactionInfo.chainId), password)
                .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                        gotoSignFail(getCommonError());
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
        mTransferDialog.setButtonClickAble(true);
        if (ethSendTransaction == null) {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        } else if (ethSendTransaction.getError() != null
                && !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(new Gson().toJson(ethSendTransaction.getError()));
        } else if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(new Gson().toJson(ethSendTransaction.getTransactionHash()));
        } else {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        }
    }


    /**
     * handle appchain transfer result
     *
     * @param appSendTransaction result of appchain transaction
     */
    private void handleTransfer(AppSendTransaction appSendTransaction) {
        mTransferDialog.setButtonClickAble(true);
        if (appSendTransaction == null) {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        } else if (appSendTransaction.getError() != null
                && !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, appSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(new Gson().toJson(appSendTransaction.getError()));
        } else if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(new Gson().toJson(appSendTransaction.getSendTransactionResult()));
        } else {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        }
    }


    private String getNativeToken() {
        if (mTransactionInfo.isEthereum()) {
            return ConstantUtil.ETH;
        } else {
            return mChainItem == null ? "" : " " + mChainItem.tokenSymbol;
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

    private String getCommonError() {
        return new Gson().toJson(new Response.Error(ERROR_CODE
                , getString(R.string.operation_fail)));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                if (!isFastDoubleClick())
                    getConfirmTransferView();
                break;
            case R.id.tv_gas_price:
                String ethGasPriceDefaultValue = NumberUtil.getDecimalValid_2(
                        Convert.fromWei(Numeric.toBigInt(mEthDefaultPrice).toString(), GWEI).doubleValue());
                DAppAdvanceSetupDialog dialog = new DAppAdvanceSetupDialog(mActivity, new DAppAdvanceSetupDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick(View v, String gasPrice) {
                        if (TextUtils.isEmpty(gasPrice)) {
                            Toast.makeText(mActivity, R.string.input_correct_gas_price_tip, Toast.LENGTH_SHORT).show();
                        } else if (Double.parseDouble(gasPrice) < ConstantUtil.MIN_GWEI) {
                            Toast.makeText(mActivity, R.string.gas_price_too_low, Toast.LENGTH_SHORT).show();
                        } else {
                            mTransactionInfo.gasPrice = Convert.toWei(gasPrice, GWEI).toBigInteger().toString(16);
                            setEthGasPrice();
                        }
                    }
                });
                dialog.setTransactionData(mTransactionInfo.data);
                dialog.setGasPriceDefault(ethGasPriceDefaultValue);
                String gasPriceValue = NumberUtil.getDecimalValid_2(
                        Convert.fromWei(Numeric.toBigInt(mTransactionInfo.gasPrice).toString(), GWEI).doubleValue());
                dialog.setGasFeeDefault(mTransactionInfo.gasLimit, gasPriceValue, mTransactionInfo.getGas());
                dialog.show();
                break;
            default:
                break;
        }
    }
}
