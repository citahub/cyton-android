package com.cryptape.cita_wallet.item.dapp

import com.cryptape.cita_wallet.constant.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/15.
 */
class Gyroscope(status: Int,
                errorCode: Int,
                errorMsg: String,
                gyroscope: Gyroscope) : BaseNeuronDAppCallback(status, errorCode, errorMsg) {

    constructor(gyroscope: Gyroscope) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", gyroscope)

    var info: Gyroscope = gyroscope

    class Gyroscope(var x: String, var y: String, var z: String)
}