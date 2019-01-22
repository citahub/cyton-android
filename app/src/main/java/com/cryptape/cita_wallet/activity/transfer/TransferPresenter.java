package com.cryptape.cita_wallet.activity.transfer;

import android.app.Activity;
import android.text.TextUtils;

import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;

import com.cryptape.cita_wallet.R;
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
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.ether.EtherUtil;
import com.cryptape.cita_wallet.util.url.HttpCITAUrls;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscriber;

import static com.cryptape.cita_wallet.activity.transfer.TransferActivity.EXTRA_ADDRESS;
import static com.cryptape.cita_wallet.activity.transfer.TransferActivity.EXTRA_TOKEN;

/**
 * Created by duanyytop on 2018/11/4
 */
public class TransferPresenter {


    private Activity mActivity;
    private TransferView mTransferView;

    private Token mToken;
    private double mTokenPrice = 0;
    private Wallet mWallet;
    private Currency mCurrency;

    private BigInteger mGasPrice, mGasLimit, mGas;
    private BigInteger mQuota, mQuotaLimit, mQuotaPrice;
    private String mData;
    private double mTokenBalance, mNativeTokenBalance, mTransferFee;

    public TransferPresenter(Activity activity, TransferView transferView) {
        mActivity = activity;
        mTransferView = transferView;

        init();
    }

    public void init() {

        EthRpcService.init(mActivity);
        CITARpcService.init(mActivity, HttpCITAUrls.CITA_NODE_URL);

        initTokenItem();
        getAddressData();
        initWalletData();
        getTokenBalance();
        initTransferFee();

    }

    private void getTokenBalance() {
        WalletService.getBalanceWithToken(mActivity, mToken).subscribe(new CytonSubscriber<Double>() {
            @Override
            public void onNext(Double balance) {
                mTokenBalance = balance;
                mTransferView.updateAnyTokenBalance(balance);
            }
        });

        WalletService.getBalanceWithNativeToken(mActivity, mToken).subscribe(new Subscriber<Double>() {
            @Override
            public void onNext(Double balance) {
                mNativeTokenBalance = balance;
                mTransferView.updateNativeTokenBalance(balance);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onCompleted() {

            }
        });

    }

    private void getAddressData() {
        String address = mActivity.getIntent().getStringExtra(EXTRA_ADDRESS);
        mTransferView.updaterReceiveAddress(address);
    }

    private void initTokenItem() {
        mToken = mActivity.getIntent().getParcelableExtra(EXTRA_TOKEN);
        mTransferView.updateTitleData(mToken.symbol + " " + mActivity.getString(R.string.title_transfer));
    }

    private void initWalletData() {
        mWallet = DBWalletUtil.getCurrentWallet(mActivity);
        mTransferView.updateWalletData(mWallet);
    }

    private void initTransferFee() {
        if (EtherUtil.isEther(mToken)) {
            initEthGasInfo();
            mTransferView.initTransferEditValue();
            initTokenPrice();
        } else {
            initCITAQuota();
        }
    }

    private void initEthGasInfo() {
        mGasLimit = EtherUtil.isNative(mToken) ? ConstantUtil.QUOTA_TOKEN : ConstantUtil.QUOTA_ERC20;
        mTransferView.startUpdateEthGasPrice();
        EthRpcService.getEthGasPrice().subscribe(new CytonSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mTransferView.updateEthGasPriceFail(e);
            }

            @Override
            public void onNext(BigInteger gasPrice) {
                mGasPrice = gasPrice;
                updateGasInfo();
                mTransferView.updateEthGasPriceSuccess(gasPrice);
            }
        });
    }

    /**
     * get the price of token
     */
    private void initTokenPrice() {
        mCurrency = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(ConstantUtil.ETH, mCurrency.getName())
                .subscribe(new CytonSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price))
                            return;
                        try {
                            mTokenPrice = Double.parseDouble(price);
                            mTransferView.initTransferFeeView();
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void initCITAQuota() {
        mQuotaLimit = TextUtils.isEmpty(getTokenItem().contractAddress) ? ConstantUtil.QUOTA_TOKEN : ConstantUtil.QUOTA_ERC20;
        CITARpcService.getQuotaPrice(mWallet.address)
                .subscribe(new CytonSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String quotaPrice) {
                        mQuotaPrice = new BigInteger(quotaPrice);
                        initQuotaFee();
                        mTransferView.initTransferFeeView();
                    }
                });

    }

    private void initQuotaFee() {
        mQuota = mQuotaLimit.multiply(mQuotaPrice);
        mTransferFee = NumberUtil.getEthFromWei(mQuota);
        mTransferView.updateCITAQuota(NumberUtil.getDecimalValid_8(mTransferFee) + getFeeTokenUnit());
    }

    public void updateQuotaLimit(BigInteger quotaLimit) {
        mQuotaLimit = quotaLimit;
        initQuotaFee();
    }

    public BigInteger getQuotaLimit() {
        return mQuotaLimit;
    }

    public void initGasLimit(AppTransaction appTransaction) {
        EthRpcService.getEthGasLimit(appTransaction)
                .subscribe(new CytonSubscriber<BigInteger>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(BigInteger gasLimit) {
                        mGasLimit = gasLimit.multiply(ConstantUtil.GAS_LIMIT_PARAMETER);
                        updateGasInfo();
                    }
                });
    }


    public void handleTransferAction(String password, String transferValue, String receiveAddress) {
        if (EtherUtil.isEther(mToken)) {
            if (ConstantUtil.ETH.equals(mToken.symbol)) {
                transferEth(password, transferValue, receiveAddress);
            } else {
                transferEthErc20(password, transferValue, receiveAddress);
            }
        } else {
            if (isNativeToken()) {
                transferCITAToken(password, transferValue, receiveAddress.toLowerCase());
            } else {
                transferCITAErc20(password, transferValue, receiveAddress.toLowerCase());
            }
        }
    }

    /**
     * transfer origin token of ethereum
     *
     * @param value
     */
    private void transferEth(String password, String value, String receiveAddress) {
        EthRpcService.transferEth(mActivity, receiveAddress, value, mGasPrice, mGasLimit, mData, password)
                .subscribe(new CytonSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        mTransferView.transferCITAFail(e);
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        if (ethSendTransaction.hasError()) {
                            mTransferView.transferEtherFail(ethSendTransaction.getError().getMessage());
                        } else {
                            mTransferView.transferEtherSuccess(ethSendTransaction);
                        }
                    }
                });
    }


    /**
     * transfer origin token of ethereum
     *
     * @param value
     */
    private void transferEthErc20(String password, String value, String receiveAddress) {
        EthRpcService.transferErc20(mActivity, mToken, receiveAddress, value, mGasPrice, mGasLimit, password)
                .subscribe(new CytonSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        mTransferView.transferEtherFail(e.getMessage());
                    }

                    @Override
                    public void onNext(EthSendTransaction ethSendTransaction) {
                        if (ethSendTransaction.hasError()) {
                            mTransferView.transferEtherFail(ethSendTransaction.getError().getMessage());
                        } else {
                            mTransferView.transferEtherSuccess(ethSendTransaction);
                        }
                    }
                });
    }

    /**
     * transfer origin token of nervos
     *
     * @param transferValue transfer value
     */
    private void transferCITAToken(String password, String transferValue, String receiveAddress) {
        Chain item = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mToken.getChainId());
        if (item == null)
            return;
        CITARpcService.setHttpProvider(item.httpProvider);
        CITARpcService.transferCITA(mActivity, receiveAddress, transferValue, mData, mQuotaLimit.longValue(),
                new BigInteger(mToken.getChainId()), password)
                .subscribe(new CytonSubscriber<AppSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        mTransferView.transferCITAFail(e);
                    }

                    @Override
                    public void onNext(AppSendTransaction appSendTransaction) {
                        mTransferView.transferCITASuccess(appSendTransaction);
                    }
                });
    }


    /**
     * transfer erc20 token of nervos
     *
     * @param transferValue
     */
    private void transferCITAErc20(String password, String transferValue, String receiveAddress) {
        Chain item = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mToken.getChainId());
        if (item == null)
            return;
        try {
            CITARpcService.transferErc20(mActivity, mToken, receiveAddress, transferValue, mQuotaLimit.longValue(),
                    Numeric.toBigInt(mToken.getChainId()), password)
                    .subscribe(new CytonSubscriber<AppSendTransaction>() {
                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            mTransferView.transferCITAFail(e);
                        }

                        @Override
                        public void onNext(AppSendTransaction appSendTransaction) {
                            mTransferView.transferCITASuccess(appSendTransaction);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateGasInfo() {
        mGas = mGasPrice.multiply(mGasLimit);
        mTransferFee = NumberUtil.getEthFromWei(mGas);
        mTransferView.initTransferFeeView();
    }

    /**
     * @param gasPrice wei
     */
    public void updateGasPrice(BigInteger gasPrice) {
        mGasPrice = gasPrice;
        updateGasInfo();
    }

    public void updateGasLimit(BigInteger gasLimit) {
        mGasLimit = gasLimit;
        updateGasInfo();
    }

    public void updateData(String data) {
        mData = data;
    }

    public String getData() {
        return mData;
    }

    public Token getTokenItem() {
        return mToken;
    }

    public Wallet getWalletItem() {
        return mWallet;
    }

    public BigInteger getGasLimit() {
        return mGasLimit;
    }

    public boolean isTransferFeeEnough() {
        return mNativeTokenBalance - mTransferFee >= 0;
    }

    /**
     * Check whether transfer value is bigger than balance of wallet
     *
     * @return
     */
    public boolean checkTransferValueMoreBalance(String transferValue) {
        if (isNativeToken()) {
            return Double.parseDouble(transferValue) > (mNativeTokenBalance - mTransferFee);
        } else {
            return Double.parseDouble(transferValue) > mTokenBalance;
        }
    }

    public String balanceSubFee() {
        if (isNativeToken()) {
            return NumberUtil.getDecimalValid_8(new BigDecimal(mNativeTokenBalance).subtract(new BigDecimal(mTransferFee)).doubleValue());
        } else {
            return NumberUtil.getDecimalValid_8(mTokenBalance);
        }
    }

    public String getTransferFee() {
        if (mTokenPrice > 0 && EtherUtil.isMainNet()) {
            return NumberUtil.getDecimalValid_8(mTransferFee) + getFeeTokenUnit()
                    + " ≈ " + mCurrency.getSymbol() + " "
                    + NumberUtil.getDecimalValid_2(mTransferFee * mTokenPrice);
        } else {
            return NumberUtil.getDecimalValid_8(mTransferFee) + getFeeTokenUnit();
        }
    }

    public BigInteger getGasPrice() {
        return mGasPrice;
    }

    public boolean isEthERC20() {
        return Numeric.toBigInt(mToken.getChainId()).compareTo(BigInteger.ZERO) < 0
                && !TextUtils.isEmpty(mToken.contractAddress);
    }

    public boolean isNativeToken() {
        return TextUtils.isEmpty(mToken.contractAddress);
    }

    public boolean isEther() {
        return Numeric.toBigInt(mToken.getChainId()).compareTo(BigInteger.ZERO) < 0;
    }

    public String getFeeTokenUnit() {
        if (EtherUtil.isEther(mToken)) {
            return " " + ConstantUtil.ETH;
        } else {
            Chain chain = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mToken.getChainId());
            return chain == null || TextUtils.isEmpty(chain.tokenSymbol) ? "" : " " + chain.tokenSymbol;
        }
    }

}
