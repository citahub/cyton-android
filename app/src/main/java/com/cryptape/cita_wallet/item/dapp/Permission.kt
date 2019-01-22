package com.cryptape.cita_wallet.item.dapp

import com.cryptape.cita_wallet.constant.CytonDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class Permission(status: Int, errorCode: Int, errorMsg: String, result: Boolean) :
        BaseCytonDAppCallback(status, errorCode, errorMsg) {

    constructor(result: Boolean) : this(CytonDAppCallback.SUCCESS_CODE, 0, "", result)

    var info: Permission

    init {
        info = Permission()
        info.result = result
    }

    inner class Permission {
        var result: Boolean? = null
    }
}
