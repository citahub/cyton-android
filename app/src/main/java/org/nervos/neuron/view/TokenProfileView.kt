package org.nervos.neuron.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.view_token_profile.view.*
import okhttp3.Request
import org.nervos.neuron.R
import org.nervos.neuron.activity.SimpleWebActivity
import org.nervos.neuron.item.EthErc20TokenInfoItem
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.service.http.HttpService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.TokenLogoUtil
import org.nervos.neuron.util.ether.EtherUtil
import org.nervos.neuron.util.url.HttpUrls
import org.web3j.crypto.Keys
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by BaojunCZ on 2018/11/29.
 */
class TokenProfileView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var mTokenItem: TokenItem? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_token_profile, this)
    }

    fun init(item: TokenItem) {
        mTokenItem = item
        initData()
    }

    private fun initData() {
        if (!TextUtils.isEmpty(mTokenItem!!.contractAddress)) {
            mTokenItem!!.contractAddress = handleAddress(mTokenItem!!.contractAddress)
        }
        if (EtherUtil.isEther(mTokenItem!!)) {
            if (!TextUtils.isEmpty(mTokenItem!!.contractAddress)) {
                getDescribe(mTokenItem!!.contractAddress) { item ->
                    visibility = View.VISIBLE
                    tv_token_symbol.text = item.symbol
                    tv_token_des_first.text = item.overView.zh
                    setDesSecondLine(item.overView.zh)
                    TokenLogoUtil.setLogo(mTokenItem!!, context, iv_token_logo)
                    getPrice(context, mTokenItem!!) { tv_price.text = it }
                    setOnClickListener { SimpleWebActivity.gotoSimpleWeb(context, String.format(HttpUrls.TOKEN_ERC20_DETAIL, mTokenItem!!.contractAddress)) }
                }
            } else {
                visibility = View.VISIBLE
                tv_token_des_first.setText(R.string.ETH_Describe)
                tv_token_symbol.text = ConstantUtil.ETH
                setDesSecondLine(resources.getString(R.string.ETH_Describe))
                getPrice(context, mTokenItem!!) { tv_price.text = it }
                setOnClickListener { SimpleWebActivity.gotoSimpleWeb(context, String.format(HttpUrls.TOKEN_DETAIL, ConstantUtil.ETHEREUM)) }
            }
        } else {
            visibility = View.GONE
        }
    }

    /**
     *  calculate second line start index after setting first line
     */
    private fun setDesSecondLine(describe: String) {
        tv_token_des_first.viewTreeObserver.addOnGlobalLayoutListener {
            val layout = tv_token_des_first.layout
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
                tv_token_des_second.text = secondText
            }
        }
    }

    /**
     * require describe info
     */
    private fun getDescribe(address: String, method: (EthErc20TokenInfoItem) -> Unit) {
        Observable.just(address)
                .flatMap {
                    val url = String.format(HttpUrls.TOKEN_DESC, it)
                    val request = Request.Builder().url(url).build()
                    val call = HttpService.getHttpClient().newCall(request)
                    val res = call.execute().body()!!.string()
                    if (!TextUtils.isEmpty(res)) {
                        val item = Gson().fromJson(res, EthErc20TokenInfoItem::class.java)
                        Observable.just(item)
                    } else {
                        Observable.just(null)
                    }
                }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NeuronSubscriber<EthErc20TokenInfoItem>() {
                    override fun onNext(item: EthErc20TokenInfoItem?) {
                        if (item != null) {
                            method(item)
                        }
                    }
                })
    }

    /**
     * get token price
     */
    private fun getPrice(context: Context, mTokenItem: TokenItem, method: (String) -> Unit) {
        if (EtherUtil.isEther(mTokenItem)) {
            TokenService.getCurrency(mTokenItem.symbol, CurrencyUtil.getCurrencyItem(context).name)
                    .subscribe(object : NeuronSubscriber<String>() {
                        override fun onNext(s: String) {
                            if (!TextUtils.isEmpty(s)) {
                                method(CurrencyUtil.getCurrencyItem(context).symbol + " " + CurrencyUtil.formatCurrency(s.toDouble()))
                            } else {
                                method("")
                            }
                        }
                    })
        }

    }

    private fun handleAddress(address: String): String {
        return if (AddressUtil.isAddressValid(address)) {
            Keys.toChecksumAddress(address)
        } else {
            address
        }
    }

}
