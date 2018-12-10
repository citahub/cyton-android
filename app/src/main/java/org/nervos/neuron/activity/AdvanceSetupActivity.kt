package org.nervos.neuron.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import org.nervos.neuron.R

import kotlinx.android.synthetic.main.activity_advance_setup.*
import org.nervos.neuron.item.transaction.TransactionInfo
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.db.DBChainUtil
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Created by duanyytop on 2018/12/4.
 */
class AdvanceSetupActivity : NBaseActivity() {

    companion object {
        const val EXTRA_ADVANCE_SETUP = "extra_advance_setup"
        const val EXTRA_TRANSACTION = "extra_transaction"
        const val EXTRA_TRANSFER = "extra_transfer"
        const val EXTRA_NATIVE_TOKEN = "extra_native_token"
        const val RESULT_TRANSACTION = 0x01
    }

    private var mFeePrice : String = ""
    private var isTransfer : Boolean = false
    private var isNativeToken = false

    private var mTransactionInfo: TransactionInfo? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_advance_setup
    }

    override fun initView() {
        mTransactionInfo = intent?.getParcelableExtra(EXTRA_ADVANCE_SETUP)
        isTransfer = intent.getBooleanExtra(EXTRA_TRANSFER, false)
        isNativeToken = intent.getBooleanExtra(EXTRA_NATIVE_TOKEN, false)

        if (mTransactionInfo!!.isEthereum) {
            tv_advance_setup_gas_limit_label.setText(R.string.gas_limit)
            tv_advance_setup_gas_price_label.setText(R.string.gas_price)
            tv_advance_setup_gas_fee_label.setText(R.string.gas_fee)
        } else {
            tv_advance_setup_gas_limit_label.setText(R.string.quota_limit)
            tv_advance_setup_gas_price_label.setText(R.string.quota_price)
            tv_advance_setup_gas_fee_label.setText(R.string.quota_fee)
        }

        et_advance_setup_gas_price.setText(mFeePrice)
        tv_advance_setup_price_unit.text = getFeeUnit()
        et_advance_setup_gas_limit.setText(
                if (mTransactionInfo!!.isEthereum) mTransactionInfo!!.gasLimit.toString()
                else mTransactionInfo!!.quota.toString())
        et_advance_setup_gas_price.isEnabled = mTransactionInfo!!.isEthereum
        if (isTransfer) {
            rl_advance_setup_data_layout.visibility = View.GONE
            hideDataLayout()
        } else {
            et_advance_setup_pay_data.isEnabled = false
            et_advance_setup_pay_data.hint = ""
        }
        et_advance_setup_pay_data.setText(mTransactionInfo!!.data)

        initFeeInfo()
    }

    private fun hideDataLayout() {
        var hide = if (isNativeToken) View.VISIBLE else View.GONE
        sv_advance_setup_pay_data_layout.visibility = hide
        tv_advance_setup_data_tip.visibility = hide
        tv_advance_setup_data_warning.visibility = hide
    }

    override fun initData() {
    }

    override fun initAction() {
        ll_advance_setup_sign_hex_layout.setOnClickListener {
            view_advance_setup_pay_data_left_line.visibility = View.VISIBLE
            view_advance_setup_pay_data_right_line.visibility = View.GONE
            et_advance_setup_pay_data.setText(mTransactionInfo!!.data)
        }

        ll_advance_setup_sign_utf8_layout.setOnClickListener {
            view_advance_setup_pay_data_left_line.visibility = View.GONE
            view_advance_setup_pay_data_right_line.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(mTransactionInfo!!.data) && Numeric.containsHexPrefix(mTransactionInfo!!.data)) {
                et_advance_setup_pay_data.setText(NumberUtil.hexToUtf8(mTransactionInfo!!.data))
            }
        }

        btn_advance_setup_confirm.setOnClickListener {
            if (mTransactionInfo!!.isEthereum && et_advance_setup_gas_price.text.toString().trim().toDouble() < ConstantUtil.MIN_GWEI) {
                Toast.makeText(mActivity, R.string.gas_price_too_low, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (BigInteger.valueOf(et_advance_setup_gas_limit.text.toString().trim().toLong()) < ConstantUtil.GAS_LIMIT) {
                Toast.makeText(mActivity, R.string.gas_limit_too_low, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isTransfer && isNativeToken) {
                if (!TextUtils.isEmpty(et_advance_setup_pay_data.text.toString().trim())) {
                    mTransactionInfo!!.data = et_advance_setup_pay_data.text.toString().trim()
                }
            }
            if (mTransactionInfo!!.isEthereum) {
                mTransactionInfo!!.gasLimit = BigInteger(et_advance_setup_gas_limit.text.toString().trim())
                mTransactionInfo!!.gasPrice =
                        NumberUtil.getWeiFromGWeiForBigInt(et_advance_setup_gas_price.text.toString().trim().toDouble())
            } else {
                mTransactionInfo!!.setQuota(et_advance_setup_gas_limit.text.toString().trim())
            }
            var intent = Intent()
            intent.putExtra(EXTRA_TRANSACTION, mTransactionInfo)
            setResult(RESULT_TRANSACTION, intent)
            finish()
        }
    }

    private fun initFeeInfo() {
        if (mTransactionInfo!!.isEthereum) {
            initTokenPrice()
        } else {
            requestQuotaPrice()
        }
    }

    private fun initTokenPrice() {
        TokenService.getCurrency(ConstantUtil.ETH, CurrencyUtil.getCurrencyItem(mActivity).name)
                .subscribe(object : NeuronSubscriber<String>() {
                    override fun onNext(price: String) {
                        if (TextUtils.isEmpty(price)) return
                        try {
                            updateGasPriceAndLimit(price)
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }
                })
    }

    private fun updateGasPriceAndLimit(price: String) {
        et_advance_setup_gas_price.setText(NumberUtil.getGWeiFromWeiForString(mTransactionInfo!!.gasPrice))

        var gasMoney = NumberUtil.getDecimalValid_2(price.toDouble() * mTransactionInfo!!.gas)
        tv_advance_setup_gas_fee.text =
                String.format("%s %s ≈ %s %s",
                        NumberUtil.getDecimal8ENotation(mTransactionInfo!!.gas),
                        getNativeToken(),
                        CurrencyUtil.getCurrencyItem(mActivity).symbol, gasMoney)

        tv_advance_setup_gas_fee_detail.text =
                String.format("Gas Limit(%s)*Gas Price(%s %s)",
                        mTransactionInfo!!.gasLimit.toString(),
                        NumberUtil.getGWeiFromWeiForString(mTransactionInfo!!.gasPrice), getFeeUnit())
    }



    private fun requestQuotaPrice() {
        AppChainRpcService.getQuotaPrice(mTransactionInfo!!.from)
                .subscribe(object : NeuronSubscriber<String>() {
                    override fun onNext(price: String) {
                        super.onNext(price)
                        updateQuotaPriceAndLimit(price)
                    }
                })
    }

    private fun updateQuotaPriceAndLimit(quotaPrice: String) {
        var price = NumberUtil.getDecimal8ENotation(NumberUtil.getEthFromWei(BigInteger(quotaPrice)))

        et_advance_setup_gas_price.setText(price)

        var quota = mTransactionInfo!!.quota.multiply(BigInteger(quotaPrice))

        tv_advance_setup_gas_fee.text = String.format("%s %s",
                NumberUtil.getDecimal8ENotation(NumberUtil.getEthFromWei(quota)), getNativeToken())

        tv_advance_setup_gas_fee_detail.text =
                String.format("Quota Limit(%s)*Quota Price(%s %s)",
                        mTransactionInfo!!.quota.toString(), price, getFeeUnit())
    }

    private fun getNativeToken() : String {
        return if (mTransactionInfo!!.isEthereum) {
            ConstantUtil.ETH
        } else {
            DBChainUtil.getChain(mActivity, mTransactionInfo!!.chainId)!!.tokenSymbol
        }
    }

    private fun getFeeUnit() : String {
        return if (mTransactionInfo!!.isEthereum) getString(R.string.gwei) else getNativeToken()
    }

}
