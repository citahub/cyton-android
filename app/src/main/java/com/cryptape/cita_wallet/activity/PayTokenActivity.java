package com.cryptape.cita_wallet.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;
import com.google.gson.Gson;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.item.App;
import com.cryptape.cita_wallet.item.Chain;
import com.cryptape.cita_wallet.item.Currency;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.transaction.AppTransaction;
import com.cryptape.cita_wallet.service.http.CITARpcService;
import com.cryptape.cita_wallet.service.http.EthRpcService;
import com.cryptape.cita_wallet.service.http.CytonSubscriber;
import com.cryptape.cita_wallet.service.http.TokenService;
import com.cryptape.cita_wallet.service.http.WalletService;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.dialog.TransferDialog;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by duanyytop on 2018/5/28
 */
public class PayTokenActivity extends NBaseActivity implements View.OnClickListener {

    public static final String EXTRA_HEX_HASH = "extra_hex_hash";
    public static final String EXTRA_PAY_ERROR = "extra_pay_error";
    private static final int REQUEST_CODE = 0x01;
    private static int ERROR_CODE = -1;

    private AppTransaction mAppTransaction;
    private Wallet mWallet;
    private Token mToken;
    private App mApp;
    private TextView mTvValue, mTvSymbol, mTvPayFee, mTvPayFeeTitle, mTvTotalFee,
            mTvReceiverName, mTvReceiverWebsite, mTvReceiverAddress, mTvSenderAddress;
    private TransferDialog mTransferDialog;
    private Chain mChain;
    private Double mQuota;

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
            mAppTransaction = new Gson().fromJson(payload, AppTransaction.class);
        }
        mWallet = DBWalletUtil.getCurrentWallet(mActivity);
        mApp = getIntent().getParcelableExtra(AppWebActivity.EXTRA_CHAIN);

        initRemoteData();
        updateView();

    }

    @SuppressLint("SetTextI18n")
    private void updateView() {
        mTvValue.setText(NumberUtil.getDecimalValid_8(mAppTransaction.getDoubleValue()));
        mTvSymbol.setText(getNativeToken());
        mTvSenderAddress.setText(mWallet.address);
        mTvReceiverWebsite.setText(getIntent().getStringExtra(AppWebActivity.RECEIVER_WEBSITE));
        mTvReceiverAddress.setText(mAppTransaction.to);
        mTvReceiverName.setText(mApp.name);
        mTvPayFeeTitle.setText(mAppTransaction.isEthereum() ? R.string.gas_fee : R.string.quota_fee);
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
        if (mAppTransaction.isEthereum()) {
            showProgressCircle();
            if (TextUtils.isEmpty(mAppTransaction.getGasPrice().toString()) || BigInteger.ZERO.equals(mAppTransaction.getGasPrice())) {
                getEtherGasPrice();
            }

            if (TextUtils.isEmpty(mAppTransaction.getGasLimit().toString()) || BigInteger.ZERO.equals(mAppTransaction.getGasLimit())) {
                getEtherGasLimit();
            }

            setEthGasPrice();
        } else {
            getQuotaPrice();
        }
    }

    private void getEtherGasPrice() {
        EthRpcService.getEthGasPrice().subscribe(new CytonSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                dismissProgressCircle();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onNext(BigInteger gasPrice) {
                mAppTransaction.setGasPrice(gasPrice);
                setEthGasPrice();
            }
        });
    }

    private void getEtherGasLimit() {
        EthRpcService.getEthGasLimit(mAppTransaction)
                .subscribe(new CytonSubscriber<BigInteger>() {
                    public void onError(Throwable e) {
                        dismissProgressCircle();
                        mAppTransaction.setGasLimit(ConstantUtil.GAS_ERC20_LIMIT);
                        setEthGasPrice();
                    }

                    @Override
                    public void onNext(BigInteger gasLimit) {
                        mAppTransaction.setGasLimit(ConstantUtil.GAS_LIMIT_PARAMETER);
                        setEthGasPrice();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setEthGasPrice() {
        dismissProgressCircle();
        if (TextUtils.isEmpty(mAppTransaction.getGasLimit().toString()) || TextUtils.isEmpty(mAppTransaction.getGasPrice().toString())) {
            return;
        }

        mTvPayFee.setText(NumberUtil.getDecimalValid_8(mAppTransaction.getGas()) + getNativeToken());
        mTvTotalFee.setText(CurrencyUtil.fmtMicrometer(NumberUtil.getDecimalValid_8(
                mAppTransaction.getDoubleValue() + mAppTransaction.getGas())) + " " + getNativeToken());
        Currency currency = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(ConstantUtil.ETH, currency.getName())
                .subscribe(new CytonSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price))
                            return;
                        try {
                            String currencyPrice = NumberUtil.getDecimalValid_2(
                                    mAppTransaction.getGas() * Double.parseDouble(price));
                            mTvPayFee.setText(NumberUtil.getDecimalValid_8(
                                    mAppTransaction.getGas()) + " " + getNativeToken() + " "
                                    + "≈" + currency.getSymbol() + " " + currencyPrice);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void getQuotaPrice() {
        CITARpcService.getQuotaPrice(mWallet.address).subscribe(new CytonSubscriber<String>() {
            @Override
            public void onNext(String price) {
                super.onNext(price);
                mQuota = NumberUtil.getEthFromWei(mAppTransaction.getQuota().multiply(new BigInteger(price)));
                mTvTotalFee.setText(String.format("%s %s",
                        NumberUtil.getDecimalValid_8(mAppTransaction.getDoubleValue() + mQuota), getNativeToken()));
                mTvPayFee.setText(String.format("%s %s", NumberUtil.getDecimalValid_8(mQuota), getNativeToken()));
            }
        });
    }

    private void initBalance() {
        mChain = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mAppTransaction.chainId);
        if (mChain == null) {
            finish();
        }
        mToken = new Token(mChain);
    }

    @SuppressLint("SetTextI18n")
    private void getConfirmTransferView() {
        mTransferDialog = new TransferDialog(this, (password, progressBar) -> {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show();
            } else if (!WalletService.checkPassword(mActivity, password, mWallet)) {
                Toast.makeText(mActivity, R.string.password_fail, Toast.LENGTH_SHORT).show();
            } else {
                mTransferDialog.setButtonClickAble(false);
                progressBar.setVisibility(View.VISIBLE);
                if (mAppTransaction.isEthereum()) {
                    transferEth(password, progressBar);
                } else {
                    transferCITA(password, progressBar);
                }
            }
        });

        String fee = NumberUtil.getDecimalValid_8(mAppTransaction.isEthereum()
                ? mAppTransaction.getGas() : mQuota) + getNativeToken();

        mTransferDialog.setConfirmData(mWallet.address, mAppTransaction.to,
                NumberUtil.getDecimalValid_8(mAppTransaction.getDoubleValue()) + getNativeToken(), fee);
    }

    private void transferEth(String password, ProgressBar progressBar) {
        Observable.just(mAppTransaction.getGasPrice())
                .flatMap(new Func1<BigInteger, Observable<BigInteger>>() {
                    @Override
                    public Observable<BigInteger> call(BigInteger gasPrice) {
                        if (TextUtils.isEmpty(gasPrice.toString()) || BigInteger.ZERO.equals(gasPrice)) {
                            return EthRpcService.getEthGasPrice();
                        } else {
                            return Observable.just(gasPrice);
                        }
                    }
                }).flatMap(new Func1<BigInteger, Observable<EthSendTransaction>>() {
            @Override
            public Observable<EthSendTransaction> call(BigInteger gasPrice) {
                return EthRpcService.transferEth(mActivity, mAppTransaction.to,
                        mAppTransaction.getStringValue(), gasPrice,
                        mAppTransaction.getGasLimit(),
                        mAppTransaction.data, password);
            }
        }).subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new CytonSubscriber<EthSendTransaction>() {
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

    private void transferCITA(String password, ProgressBar progressBar) {
        CITARpcService.setHttpProvider(mChain.httpProvider);
        CITARpcService.transferCITA(mActivity, mAppTransaction.to,
                mAppTransaction.getStringValue(),
                mAppTransaction.data, mAppTransaction.getQuota().longValue(),
                Numeric.toBigInt(mAppTransaction.chainId), password)
                .subscribe(new CytonSubscriber<AppSendTransaction>() {
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
     * handle cita transfer result
     *
     * @param appSendTransaction result of cita transaction
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
        if (mAppTransaction.isEthereum()) {
            return ConstantUtil.ETH;
        } else {
            return mChain == null ? "" : " " + mChain.tokenSymbol;
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
                Intent intent = new Intent(mActivity, AdvanceSetupActivity.class);
                intent.putExtra(AdvanceSetupActivity.EXTRA_ADVANCE_SETUP, mAppTransaction);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                switch (resultCode) {
                    case AdvanceSetupActivity.RESULT_TRANSACTION:
                        mAppTransaction = data.getParcelableExtra(AdvanceSetupActivity.EXTRA_TRANSACTION);
                        updateView();
                        initRemoteData();
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
