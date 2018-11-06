package org.nervos.neuron.item.NeuronDApp

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class FileToBase64Item(status: String, errorCode: String, errorMsg: String, base64: String) :
        BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

    var info: Data

    init {
        info = Data()
        info.data = base64
    }

    inner class Data {
        var data: String? = null
    }
}
