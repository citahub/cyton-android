package org.nervos.neuron.item.NeuronDApp

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class QrCodeItem(status: String, errorCode: String, errorMsg: String, base64: String) :
        BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

    constructor(result: String) : this("1", "", "", result)

    var info: Data

    init {
        info = Data()
        info.result = base64
    }

    inner class Data {
        var result: String? = null
    }
}
