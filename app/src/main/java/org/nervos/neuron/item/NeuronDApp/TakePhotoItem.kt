package org.nervos.neuron.item.NeuronDApp

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class TakePhotoItem(status: String, errorCode: String, errorMsg: String, imagePhoto: String) :
        BaseNeuronDAppCallbackItem(status, errorCode, errorMsg) {

    var info: Info

    init {
        info = Info()
        info.imagePhoto = imagePhoto
    }

    inner class Info {
        var imagePhoto: String? = null
    }
}
