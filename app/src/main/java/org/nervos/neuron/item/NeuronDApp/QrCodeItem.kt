package org.nervos.neuron.item.NeuronDApp

import org.nervos.neuron.util.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class QrCodeItem(status: Int, errorCode: Int, errorMsg: String, base64: String) :
        BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

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
