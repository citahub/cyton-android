package org.nervos.neuron.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.AppItem;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.AppChainRpcService;
import org.nervos.neuron.service.EthRpcService;
import org.nervos.neuron.service.NeuronSubscriber;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.SaveAppChainPendingItemUtils;
import org.nervos.neuron.util.SensorDataTrackUtils;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.dialog.DAppAdvanceSetupDialog;
import org.nervos.neuron.view.dialog.TransferDialog;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.web3j.utils.Convert.Unit.GWEI;

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
        if (mTransactionInfo.isEthereum()) {
            mTvPayFeeTitle.setText(R.string.gas_price);
        } else {
            mTvTotalFee.setText(NumberUtil.getDecimal8ENotation(
                    mTransactionInfo.getDoubleValue() + mTransactionInfo.getDoubleQuota())
                    + getNativeToken());
            mTvPayFee.setText(NumberUtil.getDecimal8ENotation(mTransactionInfo.getDoubleQuota())
                    + getNativeToken());
            mTvPayFeeTitle.setText(R.string.quota);
        }
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
        showProgressCircle();
        if (mTransactionInfo.isEthereum()) {
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
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasPrice) {
                mTransactionInfo.gasPrice = gasPrice.toString(16);
                setEthGasPrice();
            }
        });
    }

    private void getEtherGasLimit() {
        EthRpcService.getEthGasLimit(mTransactionInfo)
                .subscribe(new NeuronSubscriber<BigInteger>() {
                    public void onError(Throwable e) {
                        mTransactionInfo.gasLimit = ConstUtil.GAS_ERC20_LIMIT.toString(16);
                        setEthGasPrice();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(BigInteger gasLimit) {
                        mTransactionInfo.gasLimit = gasLimit.toString(16);
                        setEthGasPrice();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setEthGasPrice() {
        if (TextUtils.isEmpty(mTransactionInfo.gasLimit) || TextUtils.isEmpty(mTransactionInfo.gasPrice)) return;

        dismissProgressCircle();
        mTvPayFee.setText(NumberUtil.getDecimal8ENotation(mTransactionInfo.getGas()) + getNativeToken());
        mTvTotalFee.setText(NumberUtil.getDecimal8ENotation(
                mTransactionInfo.getDoubleValue() + mTransactionInfo.getGas()) + getNativeToken());
        CurrencyItem currencyItem = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(ConstUtil.ETH, currencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) return;
                        try {
                            String mCurrencyPrice = NumberUtil.getDecimalValid_2(
                                    mTransactionInfo.getGas() * Double.parseDouble(price));
                            mTvPayFee.setText(
                                    NumberUtil.getDecimal8ENotation(
                                            mTransactionInfo.getGas()) + getNativeToken()
                                            + "≈" + currencyItem.getSymbol() + mCurrencyPrice);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void initBalance() {
        ChainItem chainItem = DBChainUtil.getChain(mActivity, mTransactionInfo.chainId);
        if (chainItem == null) return;
        mTokenItem = new TokenItem(chainItem);
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
                            "", mWalletItem.address, ConstUtil.ETH, "1");
                    transferEth(password, progressBar);
                } else {
                    SensorDataTrackUtils.transferAccount(mTokenItem.symbol, "",
                            "", mWalletItem.address, mTokenItem.chainName, "1");
                    transferAppChain(password, progressBar);
                }
            }
        });

        String fee = NumberUtil.getDecimal8ENotation(mTransactionInfo.isEthereum()?
                mTransactionInfo.getGas() : mTransactionInfo.getDoubleQuota()) + getNativeToken();

        mTransferDialog.setConfirmData(mWalletItem.address, mTransactionInfo.to,
                NumberUtil.getDecimal8ENotation(mTransactionInfo.getDoubleValue())
                        + getNativeToken(), fee);
    }

    private void transferEth(String password, ProgressBar progressBar) {
        Observable.just(mTransactionInfo.gasPrice)
                .flatMap(new Func1<String, Observable<BigInteger>>() {
                    @Override
                    public Observable<BigInteger> call(String gasPrice) {
                        if (TextUtils.isEmpty(mTransactionInfo.gasPrice)
                                || "0".equals(mTransactionInfo.gasPrice)) {
                            return EthRpcService.getEthGasPrice();
                        } else {
                            return Observable.just(Numeric.toBigInt(gasPrice));
                        }
                    }
                }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger gasPrice) {
                return EthRpcService.transferEth(mTransactionInfo.to,
                        mTransactionInfo.getDoubleValue(), gasPrice,
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
        AppChainRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(
                mTransactionInfo.chainId));
        SaveAppChainPendingItemUtils.setNativeToken(mActivity, mTransactionInfo.chainId,
                mWalletItem.address.toLowerCase(), mTransactionInfo.to.toLowerCase(), "0");
        AppChainRpcService.transferAppChain(mActivity, mTransactionInfo.to,
                mTransactionInfo.getDoubleValue(),
                mTransactionInfo.data, mTransactionInfo.getLongQuota(),
                (int) mTransactionInfo.chainId, password)
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
        if (!TextUtils.isEmpty(ethSendTransaction.getTransactionHash())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(new Gson().toJson(ethSendTransaction.getTransactionHash()));
        } else if (ethSendTransaction.getError() != null &&
                !TextUtils.isEmpty(ethSendTransaction.getError().getMessage())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, ethSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(new Gson().toJson(ethSendTransaction.getError()));
        } else {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        }
    }

    /**
     * handle nervos transfer result
     *
     * @param appSendTransaction result of nervos transaction
     */
    private void handleTransfer(AppSendTransaction appSendTransaction) {
        mTransferDialog.setButtonClickAble(true);
        if (!TextUtils.isEmpty(appSendTransaction.getSendTransactionResult().getHash())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, R.string.operation_success, Toast.LENGTH_SHORT).show();
            gotoSignSuccess(new Gson().toJson(appSendTransaction.getSendTransactionResult()));
        } else if (appSendTransaction.getError() != null &&
                !TextUtils.isEmpty(appSendTransaction.getError().getMessage())) {
            mTransferDialog.dismiss();
            Toast.makeText(mActivity, appSendTransaction.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
            gotoSignFail(new Gson().toJson(appSendTransaction.getError()));
        } else {
            Toast.makeText(mActivity, R.string.operation_fail, Toast.LENGTH_SHORT).show();
            gotoSignFail(getCommonError());
        }
    }

    private String getNativeToken() {
        if (mTransactionInfo.isEthereum()) {
            return ConstUtil.ETH;
        } else {
            ChainItem chainItem = DBChainUtil.getChain(mActivity, mTransactionInfo.chainId);
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
                String gasPriceDefault = NumberUtil.getDecimalValid_2(
                        Convert.fromWei(Numeric.toBigInt(mTransactionInfo.gasPrice).toString(), GWEI).doubleValue());
                DAppAdvanceSetupDialog dialog = new DAppAdvanceSetupDialog(mActivity, new DAppAdvanceSetupDialog.OnOkClickListener() {
                    @Override
                    public void onOkClick(View v, String gasPrice) {
                        if (TextUtils.isEmpty(gasPrice)) {
                            Toast.makeText(mActivity, R.string.input_correct_gas_price_tip, Toast.LENGTH_SHORT).show();
                        } else if(Double.parseDouble(gasPrice) < Double.parseDouble(gasPriceDefault)) {
                            Toast.makeText(mActivity,
                                    String.format(getString(R.string.gas_price_too_low), gasPriceDefault),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mTransactionInfo.gasPrice = Convert.toWei(gasPrice, GWEI).toBigInteger().toString(16);
                            setEthGasPrice();
                        }
                    }
                });
                dialog.setTransactionData(mTransactionInfo.data);
                dialog.setGasPriceDefault(gasPriceDefault);
                dialog.setGasFeeDefault(mTransactionInfo.gasLimit, gasPriceDefault, mTransactionInfo.getGas());
                dialog.show();
                break;
            default:
                break;
        }
    }
}
