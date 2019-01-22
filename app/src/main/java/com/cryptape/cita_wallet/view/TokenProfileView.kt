package com.cryptape.cita_wallet.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.view_token_profile.view.*
import okhttp3.Request
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.SimpleWebActivity
import com.cryptape.cita_wallet.item.EthErc20Token
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.service.http.HttpService
import com.cryptape.cita_wallet.service.http.CytonSubscriber
import com.cryptape.cita_wallet.service.http.TokenService
import com.cryptape.cita_wallet.util.AddressUtil
import com.cryptape.cita_wallet.constant.ConstantUtil
import com.cryptape.cita_wallet.util.CurrencyUtil
import com.cryptape.cita_wallet.util.TokenLogoUtil
import com.cryptape.cita_wallet.util.ether.EtherUtil
import com.cryptape.cita_wallet.constant.url.HttpUrls
import org.web3j.crypto.Keys
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created by BaojunCZ on 2018/11/29.
 */
class TokenProfileView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var mToken: Token? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_token_profile, this)
    }

    fun init(item: Token) {
        mToken = item
        initData()
    }

    private fun initData() {
        if (!TextUtils.isEmpty(mToken!!.contractAddress)) {
            mToken!!.contractAddress = handleAddress(mToken!!.contractAddress)
        }
        if (EtherUtil.isEther(mToken!!)) {
            if (!TextUtils.isEmpty(mToken!!.contractAddress)) {
                getDescribe(mToken!!.contractAddress) { item ->
                    visibility = View.VISIBLE
                    tv_token_symbol.text = item.symbol
                    tv_token_des_first.text = item.overView.zh
                    setDesSecondLine(item.overView.zh)
                    TokenLogoUtil.setLogo(mToken!!, context, iv_token_logo)
                    getPrice(context, mToken!!) { tv_price.text = it }
                    setOnClickListener { SimpleWebActivity.gotoSimpleWeb(context, String.format(HttpUrls.TOKEN_ERC20_DETAIL, mToken!!.contractAddress)) }
                }
            } else {
                visibility = View.VISIBLE
                tv_token_des_first.setText(R.string.ETH_Describe)
                tv_token_symbol.text = ConstantUtil.ETH
                setDesSecondLine(resources.getString(R.string.ETH_Describe))
                getPrice(context, mToken!!) { tv_price.text = it }
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
    private fun getDescribe(address: String, method: (EthErc20Token) -> Unit) {
        Observable.just(address)
                .flatMap {
                    val url = String.format(HttpUrls.TOKEN_DESC, it)
                    val request = Request.Builder().url(url).build()
                    val call = HttpService.getHttpClient().newCall(request)
                    val res = call.execute().body()!!.string()
                    if (!TextUtils.isEmpty(res)) {
                        val item = Gson().fromJson(res, EthErc20Token::class.java)
                        Observable.just(item)
                    } else {
                        Observable.just(null)
                    }
                }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : CytonSubscriber<EthErc20Token>() {
                    override fun onNext(item: EthErc20Token?) {
                        if (item != null) {
                            method(item)
                        }
                    }
                })
    }

    /**
     * get token price
     */
    private fun getPrice(context: Context, mToken: Token, callback: (String) -> Unit) {
        if (EtherUtil.isEther(mToken)) {
            TokenService.getCurrency(mToken.symbol, CurrencyUtil.getCurrencyItem(context).name)
                    .subscribe(object : CytonSubscriber<String>() {
                        override fun onNext(s: String) {
                            if (!TextUtils.isEmpty(s)) {
                                callback(CurrencyUtil.getCurrencyItem(context).symbol + " " + CurrencyUtil.formatCurrency(s.toDouble()))
                            } else {
                                callback("")
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
