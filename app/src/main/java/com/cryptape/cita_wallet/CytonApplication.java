package com.cryptape.cita_wallet;

import android.app.Application;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import com.cryptape.cita_wallet.service.http.EthRpcService;
import com.cryptape.cita_wallet.util.crypto.AESCrypt;
import com.cryptape.cita_wallet.util.crypto.WalletEntity;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;

/**
 * Created by duanyytop on 2018/4/17
 */
public class CytonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ZXingLibrary.initDisplayOpinion(this);
        WalletEntity.initWalletMnemonic(this);
        SharePrefUtil.init(this);
        EthRpcService.init(this);
        AESCrypt.init(this);
    }
}
