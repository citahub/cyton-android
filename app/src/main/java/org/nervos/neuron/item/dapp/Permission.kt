package org.nervos.neuron.item.dapp

import org.nervos.neuron.constant.NeuronDAppCallback

/**
 * Created by BaojunCZ on 2018/11/5.
 */
class Permission(status: Int, errorCode: Int, errorMsg: String, result: Boolean) :
        BaseNeuronDAppCallback(status, errorCode, errorMsg) {

    constructor(result: Boolean) : this(NeuronDAppCallback.SUCCESS_CODE, 0, "", result)

    var info: Permission

    init {
        info = Permission()
        info.result = result
    }

    inner class Permission {
        var result: Boolean? = null
    }
}
