package org.nervos.neuron.item.NeuronDApp

import org.nervos.neuron.constant.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class FileToBase64Item(status: Int, errorCode: Int, errorMsg: String, base64: String) :
        BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

    constructor(base64: String) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", base64)

    var info: Data

    init {
        info = Data()
        info.data = base64
    }

    inner class Data {
        var data: String? = null
    }
}
