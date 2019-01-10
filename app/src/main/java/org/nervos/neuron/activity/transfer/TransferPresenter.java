package org.nervos.neuron.activity.transfer;

import android.app.Activity;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.Chain;
import org.nervos.neuron.item.Currency;
import org.nervos.neuron.item.Token;
import org.nervos.neuron.item.Wallet;
import org.nervos.neuron.item.transaction.AppTransaction;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.service.http.NeuronSubscriber;
import org.nervos.neuron.service.http.TokenService;
import org.nervos.neuron.service.http.WalletService;
import org.nervos.neuron.constant.ConstantUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.constant.url.HttpAppChainUrls;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscriber;

import static org.nervos.neuron.activity.transfer.TransferActivity.EXTRA_ADDRESS;
import static org.nervos.neuron.activity.transfer.TransferActivity.EXTRA_TOKEN;

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
        AppChainRpcService.init(mActivity, HttpAppChainUrls.APPCHAIN_NODE_URL);

        initTokenItem();
        getAddressData();
        initWalletData();
        getTokenBalance();
        initTransferFee();

    }

    private void getTokenBalance() {
        WalletService.getBalanceWithToken(mActivity, mToken).subscribe(new NeuronSubscriber<Double>() {
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
            initAppChainQuota();
        }
    }

    private void initEthGasInfo() {
        mGasLimit = EtherUtil.isNative(mToken) ? ConstantUtil.QUOTA_TOKEN : ConstantUtil.QUOTA_ERC20;
        mTransferView.startUpdateEthGasPrice();
        EthRpcService.getEthGasPrice().subscribe(new NeuronSubscriber<BigInteger>() {
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
                .subscribe(new NeuronSubscriber<String>() {
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


    private void initAppChainQuota() {
        mQuotaLimit = TextUtils.isEmpty(getTokenItem().contractAddress) ? ConstantUtil.QUOTA_TOKEN : ConstantUtil.QUOTA_ERC20;
        AppChainRpcService.getQuotaPrice(mWallet.address)
                .subscribe(new NeuronSubscriber<String>() {
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
        mTransferView.updateAppChainQuota(NumberUtil.getDecimalValid_8(mTransferFee) + getFeeTokenUnit());
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
                .subscribe(new NeuronSubscriber<BigInteger>() {
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
                transferAppChainToken(password, transferValue, receiveAddress.toLowerCase());
            } else {
                transferAppChainErc20(password, transferValue, receiveAddress.toLowerCase());
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
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        mTransferView.transferAppChainFail(e);
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
                .subscribe(new NeuronSubscriber<EthSendTransaction>() {
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
    private void transferAppChainToken(String password, String transferValue, String receiveAddress) {
        Chain item = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mToken.getChainId());
        if (item == null)
            return;
        AppChainRpcService.setHttpProvider(item.httpProvider);
        AppChainRpcService.transferAppChain(mActivity, receiveAddress, transferValue, mData, mQuotaLimit.longValue(),
                new BigInteger(mToken.getChainId()), password)
                .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                    @Override
                    public void onError(Throwable e) {
                        mTransferView.transferAppChainFail(e);
                    }

                    @Override
                    public void onNext(AppSendTransaction appSendTransaction) {
                        mTransferView.transferAppChainSuccess(appSendTransaction);
                    }
                });
    }


    /**
     * transfer erc20 token of nervos
     *
     * @param transferValue
     */
    private void transferAppChainErc20(String password, String transferValue, String receiveAddress) {
        Chain item = DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mToken.getChainId());
        if (item == null)
            return;
        try {
            AppChainRpcService.transferErc20(mActivity, mToken, receiveAddress, transferValue, mQuotaLimit.longValue(),
                    Numeric.toBigInt(mToken.getChainId()), password)
                    .subscribe(new NeuronSubscriber<AppSendTransaction>() {
                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            mTransferView.transferAppChainFail(e);
                        }

                        @Override
                        public void onNext(AppSendTransaction appSendTransaction) {
                            mTransferView.transferAppChainSuccess(appSendTransaction);
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
        if (mTokenPrice > 0) {
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
