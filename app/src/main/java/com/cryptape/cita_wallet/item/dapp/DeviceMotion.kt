package com.cryptape.cita_wallet.item.dapp

import com.cryptape.cita_wallet.constant.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/15.
 */
class DeviceMotion(status: Int,
                   errorCode: Int,
                   errorMsg: String,
                   motion: Motion) : BaseNeuronDAppCallback(status, errorCode, errorMsg) {

    constructor(motion: Motion) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", motion)

    var info: Motion = motion

    class Motion(var alpha: String, var beta: String, var gamma: String)
}