package org.nervos.neuron.item.dapp

import org.nervos.neuron.constant.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class QrCode(status: Int, errorCode: Int, errorMsg: String, base64: String) :
        BaseNeuronDAppCallback(status, errorCode, errorMsg) {

    constructor(result: String) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", result)

    var info: Data

    init {
        info = Data()
        info.result = base64
    }

    inner class Data {
        var result: String? = null
    }
}
