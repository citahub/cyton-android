package org.nervos.neuron.view.tokenprofile

import android.content.Context
import android.text.Layout
import android.text.TextUtils
import org.nervos.neuron.item.EthErc20TokenInfoItem
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.ether.EtherUtil
import org.web3j.crypto.Keys
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by BaojunCZ on 2018/11/29.
 */
class TokenProfilePresenter {

    fun getDescribe(address: String, method: (EthErc20TokenInfoItem) -> Unit) {
        TokenDescribeModel()[address]!!.subscribe(object : NeuronSubscriber<EthErc20TokenInfoItem>() {
            override fun onNext(item: EthErc20TokenInfoItem?) {
                if (item != null) {
                    method(item)
                }
            }
        })
    }

    fun getDesSecondText(layout: Layout, describe: String, method: (String) -> Unit) {
        val mDesText = layout.text.toString()
        val srcStr = StringBuilder(mDesText)
        val lineStr = srcStr.subSequence(layout.getLineStart(0), layout.getLineEnd(0)).toString()
        val length = lineStr.length
        if (length > 0 && describe.length > length) {
            val secondText: String = if ((length * 1.5).toInt() > describe.length) {
                describe.substring(length)
            } else {
                describe.substring(length, (length * 1.5).toInt()) + "..."
            }
            method(secondText)
        }
    }

    fun getPrice(context: Context, mTokenItem: TokenItem, method: (String) -> Unit) {
        if (EtherUtil.isEther(mTokenItem!!)) {
            TokenService.getCurrency(mTokenItem!!.symbol, CurrencyUtil.getCurrencyItem(context).name)
                    .subscribe(object : NeuronSubscriber<String>() {
                        override fun onNext(s: String) {
                            if (!TextUtils.isEmpty(s)) {
                                val price = java.lang.Double.parseDouble(s.trim { it <= ' ' })
                                val df = DecimalFormat("######0.00")
                                val format = DecimalFormat("0.####")
                                format.roundingMode = RoundingMode.FLOOR
                                method(CurrencyUtil.getCurrencyItem(context).symbol + java.lang.Double.parseDouble(df.format(price)))
                            } else {
                                method("")
                            }
                        }
                    })
        }

    }

    fun handleAddress(address: String): String {
        var mAddress = address
        if (AddressUtil.isAddressValid(address))
            mAddress = Keys.toChecksumAddress(address)
        return mAddress
    }
}