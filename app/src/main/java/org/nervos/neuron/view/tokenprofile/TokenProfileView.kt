package org.nervos.neuron.view.tokenprofile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_token_profile.view.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.SimpleWebActivity
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.TokenLogoUtil
import org.nervos.neuron.util.ether.EtherUtil
import org.nervos.neuron.util.url.HttpUrls

/**
 * Created by BaojunCZ on 2018/11/29.
 */
class TokenProfileView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var mTokenItem: TokenItem? = null
    private lateinit var presenter: TokenProfilePresenter

    init {
        LayoutInflater.from(context).inflate(R.layout.view_token_profile, this)
    }

    fun init(item: TokenItem) {
        mTokenItem = item
        initData()
    }

    private fun initData() {
        presenter = TokenProfilePresenter()
        if (!TextUtils.isEmpty(mTokenItem!!.contractAddress)) {
            mTokenItem!!.contractAddress = presenter.handleAddress(mTokenItem!!.contractAddress)
        }
        if (EtherUtil.isEther(mTokenItem!!)) {
            if (!TextUtils.isEmpty(mTokenItem!!.contractAddress)) {
                presenter.getDescribe(mTokenItem!!.contractAddress) { item ->
                    visibility = View.VISIBLE
                    tv_token_symbol.text = item.symbol
                    tv_token_des_first.text = item.overView.zh
                    setDesSecondLine(item.overView.zh)
                    TokenLogoUtil.setLogo(mTokenItem!!, context, iv_token_logo)
                    presenter.getPrice(context, mTokenItem!!) { tv_price.text = it }
                    setOnClickListener { SimpleWebActivity.gotoSimpleWeb(context, String.format(HttpUrls.TOKEN_ERC20_DETAIL, mTokenItem!!.contractAddress)) }
                }
            } else {
                visibility = View.VISIBLE
                tv_token_des_first.setText(R.string.ETH_Describe)
                tv_token_symbol.text = ConstantUtil.ETH
                setDesSecondLine(resources.getString(R.string.ETH_Describe))
                presenter.getPrice(context, mTokenItem!!) { tv_price.text = it }
                setOnClickListener { SimpleWebActivity.gotoSimpleWeb(context, String.format(HttpUrls.TOKEN_DETAIL, ConstantUtil.ETHEREUM)) }
            }
        } else {
            visibility = View.GONE
        }
    }

    private fun setDesSecondLine(describe: String) {
        tv_token_des_first.viewTreeObserver.addOnGlobalLayoutListener {
            presenter.getDesSecondText(tv_token_des_first.layout, describe) {
                tv_token_des_second.text = it
            }
        }
    }

}
