package com.cryptape.cita_wallet.activity.transfer;

import com.cryptape.cita.protocol.core.methods.response.AppSendTransaction;

import com.cryptape.cita_wallet.item.Wallet;
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

    void updateWalletData(Wallet wallet);

    void startUpdateEthGasPrice();

    void updateEthGasPriceSuccess(BigInteger gasPrice);

    void updateEthGasPriceFail(Throwable throwable);

    void initTransferEditValue();

    void initTransferFeeView();

    void updateCITAQuota(String quotaFee);

    void transferCITASuccess(AppSendTransaction appSendTransaction);

    void transferCITAFail(Throwable e);

    void transferEtherSuccess(EthSendTransaction ethSendTransaction);

    void transferEtherFail(String message);

}
