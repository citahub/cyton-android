package org.nervos.neuron.item.NeuronDApp

import org.nervos.neuron.constant.NeuronDAppCallback
import org.nervos.neuron.item.dapp.BaseNeuronDAppCallbackItem

/**
 * Created by BaojunCZ on 2018/11/15.
 */
class GyroscopeItem(status: Int,
                    errorCode: Int,
                    errorMsg: String,
                    gyroscope: Gyroscope) : BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

    constructor(gyroscope: Gyroscope) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", gyroscope)

    var info: Gyroscope = gyroscope

    class Gyroscope(var x: String, var y: String, var z: String)
}