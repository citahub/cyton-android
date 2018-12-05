package org.nervos.neuron.activity

import android.text.TextUtils
import org.nervos.neuron.R

import kotlinx.android.synthetic.main.activity_advance_setup.*
import org.nervos.neuron.item.CurrencyItem
import org.nervos.neuron.item.transaction.TransactionInfo
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.db.DBChainUtil
import org.web3j.utils.Convert

/**
 * Created by duanyytop on 2018/12/4.
 */
class AdvanceSetupActivity : NBaseActivity() {

    companion object {
        var EXTRA_ADVANCE_SETUP = "extra_advance_setup"
    }

    private var mFeePrice : String = ""

    private var mTransactionInfo: TransactionInfo? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_advance_setup
    }

    override fun initView() {
        mTransactionInfo = intent?.getParcelableExtra(EXTRA_ADVANCE_SETUP)

        mFeePrice = if (mTransactionInfo!!.isEthereum) {
            Convert.fromWei(mTransactionInfo!!.gasPrice, Convert.Unit.GWEI).toDouble().toString()
        } else {
            mTransactionInfo!!.doubleQuota.toString()
        }

        edit_advance_setup_gas_price.setText(String.format("%s %s", mFeePrice, getFeeUnit()))
        edit_advance_setup_gas_limit.setText(mTransactionInfo!!.gasLimit)
        advance_setup_pay_data.text = mTransactionInfo!!.data

        initTokenPrice()
    }

    override fun initData() {

    }

    override fun initAction() {

    }


    private fun initTokenPrice() {
        TokenService.getCurrency(ConstantUtil.ETH, CurrencyUtil.getCurrencyItem(mActivity).name)
                .subscribe(object : NeuronSubscriber<String>() {
                    override fun onNext(price: String) {
                        if (TextUtils.isEmpty(price)) return
                        try {
                            updateTransactionFee(price)
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                    }
                })
    }

    private fun updateTransactionFee(price: String) {
        var gasMoney = java.lang.Double.parseDouble(price) * mTransactionInfo!!.gas
        text_advance_setup_gas_fee.text =
                String.format("%s %s ≈ %s %s", mTransactionInfo!!.gas, getNativeToken(),
                        CurrencyUtil.getCurrencyItem(mActivity).symbol, gasMoney)

        text_advance_setup_gas_fee_detail.text =
                String.format("%s Limit(%s)*%s Price(%s %s)", getFeeName(),
                        mTransactionInfo!!.gasLimit, getFeeName(), mFeePrice, getFeeUnit())
    }

    private fun getNativeToken() : String {
        return if (mTransactionInfo!!.isEthereum) {
            ConstantUtil.ETH
        } else {
            DBChainUtil.getChain(mActivity, mTransactionInfo!!.chainId)!!.tokenSymbol
        }
    }

    private fun getFeeName() : String {
        return if (mTransactionInfo!!.isEthereum) "Gas" else "Quota"
    }

    private fun getFeeUnit() : String {
        return if (mTransactionInfo!!.isEthereum) "GWei" else getNativeToken()
    }

}
