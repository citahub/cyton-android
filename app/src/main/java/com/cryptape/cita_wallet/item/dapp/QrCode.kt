package com.cryptape.cita_wallet.item.dapp

import com.cryptape.cita_wallet.constant.CytonDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class QrCode(status: Int, errorCode: Int, errorMsg: String, base64: String) :
        BaseCytonDAppCallback(status, errorCode, errorMsg) {

    constructor(result: String) : this(CytonDAppCallback.SUCCESS_CODE, 0, "", result)

    var info: Data

    init {
        info = Data()
        info.result = base64
    }

    inner class Data {
        var result: String? = null
    }
}
