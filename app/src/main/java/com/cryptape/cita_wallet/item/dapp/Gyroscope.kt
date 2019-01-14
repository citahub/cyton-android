package com.cryptape.cita_wallet.item.CytonDApp

import com.cryptape.cita_wallet.constant.CytonDAppCallback
import com.cryptape.cita_wallet.item.dapp.BaseCytonDAppCallback

/**
 * Created by BaojunCZ on 2018/11/15.
 */
class Gyroscope(status: Int,
                errorCode: Int,
                errorMsg: String,
                gyroscope: Gyroscope) : BaseCytonDAppCallback(status, errorCode, errorMsg) {

    constructor(gyroscope: Gyroscope) : this(CytonDAppCallback.SUCCESS_CODE, 0, "", gyroscope)

    var info: Gyroscope = gyroscope

    class Gyroscope(var x: String, var y: String, var z: String)
}