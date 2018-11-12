package org.nervos.neuron.activity.transfer;

import android.app.Activity;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.R;
import org.nervos.neuron.item.ChainItem;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionInfo;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.util.ether.EtherUtil;
import org.nervos.neuron.util.url.HttpAppChainUrls;
import org.nervos.neuron.service.http.NeuronSubscriber;
import org.nervos.neuron.service.http.TokenService;
import org.nervos.neuron.service.http.WalletService;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.CurrencyUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.util.SaveAppChainPendingItemUtils;
import org.nervos.neuron.util.db.DBChainUtil;
import org.nervos.neuron.util.db.DBEtherTransactionUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;
import org.nervos.neuron.util.sensor.SensorDataTrackUtils;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscriber;

import static org.nervos.neuron.activity.transfer.TransferActivity.EXTRA_ADDRESS;
import static org.nervos.neuron.activity.transfer.TransferActivity.EXTRA_TOKEN;
import static org.web3j.utils.Convert.Unit.GWEI;

/**
 * Created by duanyytop on 2018/11/4
 */
public class TransferPresenter {


    private Activity mActivity;
    private TransferView mTransferView;

    private TokenItem mTokenItem;
    private double mTokenPrice = 0;
    private WalletItem mWalletItem;
    private CurrencyItem mCurrencyItem;

    private BigInteger mGasPrice, mEthGasDefaultPrice, mGasLimit = ConstantUtil.GAS_LIMIT, mQuota, mQuotaLimit, mGas;
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
        WalletService.getBalanceWithToken(mActivity, mTokenItem).subscribe(new NeuronSubscriber<Double>() {
            @Override
            public void onNext(Double balance) {
                mTokenBalance = balance;
                mTransferView.updateAnyTokenBalance(balance);
            }
        });

        WalletService.getBalanceWithNativeToken(mActivity, mTokenItem)
                .subscribe(new Subscriber<Double>() {
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
        mTokenItem = mActivity.getIntent().getParcelableExtra(EXTRA_TOKEN);
        mTransferView.updateTitleData(mTokenItem.symbol + mActivity.getString(R.string.title_transfer));
    }

    private void initWalletData() {
        mWalletItem = DBWalletUtil.getCurrentWallet(mActivity);
        mTransferView.updateWalletData(mWalletItem);
    }

    private void initTransferFee() {
        if (isETH()) {
            initEthGasPrice();
            mTransferView.initTransferEditValue();
            initTokenPrice();
        } else {
            initAppChainQuota();
        }
    }

    private void initEthGasPrice() {
        mTransferView.startUpdateEthGasPrice();
        EthRpcService.getEthGasPrice().subscribe(new NeuronSubscriber<BigInteger>() {
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mTransferView.updateEthGasPriceFail(e);
            }
            @Override
            public void onNext(BigInteger gasPrice) {
                mEthGasDefaultPrice = mGasPrice = gasPrice;
                updateGasInfo();
                mTransferView.updateEthGasPriceSuccess(gasPrice);
            }
        });
    }

    /**
     * get the price of token
     */
    private void initTokenPrice() {
        mCurrencyItem = CurrencyUtil.getCurrencyItem(mActivity);
        TokenService.getCurrency(ConstantUtil.ETH, mCurrencyItem.getName())
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onNext(String price) {
                        if (TextUtils.isEmpty(price)) return;
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
        AppChainRpcService.getQuotaPrice(mWalletItem.address)
                .subscribe(new NeuronSubscriber<String>() {
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onNext(String quotaPrice) {
                        mQuotaLimit = TextUtils.isEmpty(getTokenItem().contractAddress) ? ConstantUtil.QUOTA_TOKEN : ConstantUtil.QUOTA_ERC20;
                        mQuota = mQuotaLimit.multiply(Numeric.toBigInt(quotaPrice));
                        mTransferFee = NumberUtil.getEthFromWei(mQuota);
                        mTransferView.updateAppChainQuota(NumberUtil.getDecimal8ENotation(mTransferFee) + getFeeTokenUnit());
                    }
                });

    }

    public void initGasLimit(TransactionInfo transactionInfo) {
        EthRpcService.getEthGasLimit(transactionInfo).subscribe(new NeuronSubscriber<BigInteger>() {
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
        if (isETH()) {
            SensorDataTrackUtils.transferAccount(mTokenItem.symbol, transferValue,
                    receiveAddress, mWalletItem.address, ConstantUtil.ETH, "2");
            if (ConstantUtil.ETH.equals(mTokenItem.symbol)) {
                transferEth(password, transferValue, receiveAddress);
            } else {
                transferEthErc20(password, transferValue, receiveAddress);
            }
        } else {
            SensorDataTrackUtils.transferAccount(mTokenItem.symbol, transferValue,
                    receiveAddress, mWalletItem.address, mTokenItem.chainName, "2");
            if (isNativeToken()) {
                transferAppChainToken(password, Double.valueOf(transferValue), receiveAddress.toLowerCase());
            } else {
                transferAppChainErc20(password, Double.valueOf(transferValue), receiveAddress.toLowerCase());
            }
        }
    }

    /**
     * transfer origin token of ethereum
     *
     * @param value
     */
    private void transferEth(String password, String value, String receiveAddress) {
        EthRpcService.transferEth(receiveAddress,
                Double.valueOf(value), mGasPrice, mGasLimit, "", password)
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
                            saveLocalTransaction(receiveAddress, value, ethSendTransaction.getTransactionHash());
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
        EthRpcService.transferErc20(mTokenItem, receiveAddress,
                Double.valueOf(value), mGasPrice, mGasLimit, password)
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
                            saveLocalTransaction(receiveAddress, value, ethSendTransaction.getTransactionHash());
                            mTransferView.transferEtherSuccess(ethSendTransaction);
                        }
                    }
                });
    }


    private void saveLocalTransaction(String receiveAddress, String value, String hash) {
        TransactionItem item = new TransactionItem(mWalletItem.address, receiveAddress, value,
                EtherUtil.getEthNodeName(), TransactionItem.PENDING, System.currentTimeMillis(), hash);
        item.blockNumber = EthRpcService.getBlockNumber().toString();
        DBEtherTransactionUtil.save(mActivity, item);
    }

    /**
     * transfer origin token of nervos
     *
     * @param transferValue transfer value
     */
    private void transferAppChainToken(String password, double transferValue, String receiveAddress) {
        AppChainRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(mTokenItem.chainId));
        SaveAppChainPendingItemUtils.setTransaction(mTokenItem.chainId, mWalletItem.address.toLowerCase(),
                receiveAddress, NumberUtil.getDecimal8ENotation(transferValue));
        AppChainRpcService.transferAppChain(mActivity, receiveAddress, transferValue,
                "", ConstantUtil.QUOTA_TOKEN.longValue(), mTokenItem.chainId, password)
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
    private void transferAppChainErc20(String password, double transferValue, String receiveAddress) {
        AppChainRpcService.setHttpProvider(SharePrefUtil.getChainHostFromId(mTokenItem.chainId));
        SaveAppChainPendingItemUtils.setTransaction(mTokenItem.chainId, mWalletItem.address.toLowerCase(),
                receiveAddress, NumberUtil.getDecimal8ENotation(transferValue));
        try {
            AppChainRpcService.transferErc20(mActivity, mTokenItem,
                    receiveAddress, transferValue, mQuotaLimit.longValue(), mTokenItem.chainId, password)
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




    public void updateGasInfo() {
        mGas = mGasPrice.multiply(mGasLimit);
        mTransferFee = NumberUtil.getEthFromWei(mGas);
        mTransferView.initTransferFeeView();
    }
    /**
     *
     * @param gasPrice  wei
     */
    public void updateGasInfo(BigInteger gasPrice) {
        mGasPrice = gasPrice;
        updateGasInfo();
    }

    public TokenItem getTokenItem() {
        return mTokenItem;
    }

    public WalletItem getWalletItem() {
        return mWalletItem;
    }

    public BigInteger getGasLimit() {
        return mGasLimit;
    }

    public boolean isTransferFeeEnough() {
        return mNativeTokenBalance - mTransferFee >= 0;
    }

    /**
     * Check whether transfer value is bigger than balance of wallet
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
            return NumberUtil.getDecimal8ENotation(new BigDecimal(mNativeTokenBalance)
                    .subtract(new BigDecimal(mTransferFee)).toString());
        } else {
            return NumberUtil.getDecimal8ENotation(mTokenBalance);
        }
    }

    public String getTransferFee() {
        if (mTokenPrice > 0) {
            return NumberUtil.getDecimal8ENotation(mTransferFee)
                    + getFeeTokenUnit() + " = " + mCurrencyItem.getSymbol()
                    + NumberUtil.getDecimalValid_2(mTransferFee * mTokenPrice);
        } else {
            return NumberUtil.getDecimal8ENotation(mTransferFee) + getFeeTokenUnit();
        }
    }

    public String getEthGasDefaultPrice() {
        return NumberUtil.getDecimalValid_2(
                Convert.fromWei(mEthGasDefaultPrice.toString(), GWEI).doubleValue());
    }

    public boolean isETH() {
        return mTokenItem.chainId < 0;
    }

    public boolean isEthERC20() {
        return mTokenItem.chainId < 0 && !TextUtils.isEmpty(mTokenItem.contractAddress);
    }

    public boolean isNativeToken() {
        return TextUtils.isEmpty(mTokenItem.contractAddress);
    }

    public String getFeeTokenUnit() {
        if (isETH()) {
            return " " + ConstantUtil.ETH;
        } else {
            ChainItem chainItem = DBChainUtil.getChain(mActivity, mTokenItem.chainId);
            return chainItem == null ? "" : " " + chainItem.tokenSymbol;
        }
    }

}
