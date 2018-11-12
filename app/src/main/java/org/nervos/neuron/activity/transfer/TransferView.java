package org.nervos.neuron.activity.transfer;

import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.neuron.item.WalletItem;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.math.BigInteger;

/**
 * Created by duanyytop on 2018/11/4
 */
public interface TransferView {

    void updateAnyTokenBalance(Double balance);

    void updateNativeTokenBalance(Double balance);

    void updaterReceiveAddress(String address);

    void updateTitleData(String title);

    void updateWalletData(WalletItem walletItem);

    void startUpdateEthGasPrice();

    void updateEthGasPriceSuccess(BigInteger gasPrice);

    void updateEthGasPriceFail(Throwable throwable);

    void initTransferEditValue();

    void initTransferFeeView();

    void updateAppChainQuota(String quotaFee);

    void transferAppChainSuccess(AppSendTransaction appSendTransaction);

    void transferAppChainFail(Throwable e);

    void transferEtherSuccess(EthSendTransaction ethSendTransaction);

    void transferEtherFail(String message);

}
