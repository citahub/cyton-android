package org.nervos.neuron.activity

import android.content.Intent
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import org.nervos.neuron.R

import kotlinx.android.synthetic.main.activity_advance_setup.*
import org.nervos.neuron.item.transaction.TransactionInfo
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.db.DBChainUtil
import org.web3j.utils.Numeric

/**
 * Created by duanyytop on 2018/12/4.
 */
class AdvanceSetupActivity : NBaseActivity() {

    companion object {
        const val EXTRA_ADVANCE_SETUP = "extra_advance_setup"
        const val EXTRA_TRANSACTION = "extra_transaction"
        const val RESULT_TRANSACTION = 0x01
    }

    private var mFeePrice : String = ""

    private var mTransactionInfo: TransactionInfo? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_advance_setup
    }

    override fun initView() {
        mTransactionInfo = intent?.getParcelableExtra(EXTRA_ADVANCE_SETUP)

        mFeePrice = if (mTransactionInfo!!.isEthereum) {
            NumberUtil.getGWeiFromWeiForString(mTransactionInfo!!.gasPrice)
        } else {
            mTransactionInfo!!.doubleQuota.toString()
        }

        edit_advance_setup_gas_price.setText(mFeePrice)
        advance_setup_price_unit.text = getFeeUnit()
        edit_advance_setup_gas_limit.setText(NumberUtil.hexToDecimal(mTransactionInfo!!.gasLimit))
        advance_setup_pay_data.text = mTransactionInfo!!.data

        initTokenPrice()
    }

    override fun initData() {

    }

    override fun initAction() {
        advance_setup_sign_hex_layout.setOnClickListener {
            advance_setup_pay_data_left_line.visibility = View.VISIBLE
            advance_setup_pay_data_right_line.visibility = View.GONE
            advance_setup_pay_data.movementMethod = ScrollingMovementMethod.getInstance()
            advance_setup_pay_data.text = mTransactionInfo!!.data
        }

        advance_setup_sign_utf8_layout.setOnClickListener {
            advance_setup_pay_data_left_line.visibility = View.GONE
            advance_setup_pay_data_right_line.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(mTransactionInfo!!.data) && Numeric.containsHexPrefix(mTransactionInfo!!.data)) {
                advance_setup_pay_data.text = NumberUtil.hexToUtf8(mTransactionInfo!!.data)
            }
        }

        advance_setup_confirm.setOnClickListener {
            if (mTransactionInfo!!.isEthereum) {
                mTransactionInfo!!.gasLimit = edit_advance_setup_gas_limit.text.toString().trim()
                mTransactionInfo!!.gasPrice =
                        NumberUtil.getWeiFromGWeiForHexString(edit_advance_setup_gas_price.text.toString().trim().toDouble())
            } else {
                mTransactionInfo!!.setQuota(edit_advance_setup_gas_limit.text.toString().trim())
            }
            var intent = Intent()
            intent.putExtra(EXTRA_TRANSACTION, mTransactionInfo)
            setResult(RESULT_TRANSACTION, intent)
            finish()
        }
    }

    private fun initTokenInfo() {
        if (mTransactionInfo!!.isEthereum) {
            initTokenPrice()
        } else {

        }
    }

    private fun initTokenPrice() {
        TokenService.getCurrency(ConstantUtil.ETH, CurrencyUtil.getCurrencyItem(mActivity).name)
                .subscribe(object : NeuronSubscriber<String>() {
                    override fun onNext(price: String) {
                        if (TextUtils.isEmpty(price)) return
                        try {
                            updateTransactionLimit(price)
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                    }
                })
    }

    private fun updateTransactionLimit(price: String) {
        var gasMoney = NumberUtil.getDecimalValid_2(price.toDouble() * mTransactionInfo!!.gas)
        text_advance_setup_gas_fee.text =
                String.format("%s %s ≈ %s %s",
                        NumberUtil.getDecimal8ENotation(mTransactionInfo!!.gas),
                        getNativeToken(),
                        CurrencyUtil.getCurrencyItem(mActivity).symbol, gasMoney)

        text_advance_setup_gas_fee_detail.text =
                String.format("%s Limit(%s)*%s Price(%s %s)", getFeeName(),
                        NumberUtil.hexToDecimal(mTransactionInfo!!.gasLimit),
                        getFeeName(), NumberUtil.getDecimalValid_2(mFeePrice.toDouble()), getFeeUnit())
    }

    private fun getNativeToken() : String {
        return if (mTransactionInfo!!.isEthereum) {
            ConstantUtil.ETH
        } else {
            DBChainUtil.getChain(mActivity, mTransactionInfo!!.chainId)!!.tokenSymbol
        }
    }

    private fun getFeeName() : String {
        return if (mTransactionInfo!!.isEthereum) getString(R.string.gas) else getString(R.string.quota)
    }

    private fun getFeeUnit() : String {
        return if (mTransactionInfo!!.isEthereum) getString(R.string.gwei) else getNativeToken()
    }

}
