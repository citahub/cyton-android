package com.cryptape.cita_wallet.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_advance_setup.*
import com.cryptape.cita_wallet.R

import com.cryptape.cita_wallet.item.transaction.AppTransaction
import com.cryptape.cita_wallet.service.http.CITARpcService
import com.cryptape.cita_wallet.service.http.CytonSubscriber
import com.cryptape.cita_wallet.service.http.TokenService
import com.cryptape.cita_wallet.constant.ConstantUtil
import com.cryptape.cita_wallet.util.CurrencyUtil
import com.cryptape.cita_wallet.util.NumberUtil
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.ether.EtherUtil
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

    private var mAppTransaction: AppTransaction? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_advance_setup
    }

    override fun initView() {
        mAppTransaction = intent?.getParcelableExtra(EXTRA_ADVANCE_SETUP)
        isTransfer = intent.getBooleanExtra(EXTRA_TRANSFER, false)
        isNativeToken = intent.getBooleanExtra(EXTRA_NATIVE_TOKEN, false)

        if (mAppTransaction!!.isEthereum) {
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
                if (mAppTransaction!!.isEthereum) mAppTransaction!!.gasLimit.toString()
                else mAppTransaction!!.quota.toString())
        et_advance_setup_gas_price.isEnabled = mAppTransaction!!.isEthereum
        if (isTransfer) {
            rl_advance_setup_data_layout.visibility = View.GONE
            hideDataLayout()
        } else {
            et_advance_setup_pay_data.isEnabled = false
            et_advance_setup_pay_data.hint = ""
        }
        et_advance_setup_pay_data.setText(mAppTransaction!!.data)

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
            et_advance_setup_pay_data.setText(mAppTransaction!!.data)
        }

        ll_advance_setup_sign_utf8_layout.setOnClickListener {
            view_advance_setup_pay_data_left_line.visibility = View.GONE
            view_advance_setup_pay_data_right_line.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(mAppTransaction!!.data) && Numeric.containsHexPrefix(mAppTransaction!!.data)) {
                et_advance_setup_pay_data.setText(NumberUtil.hexToUtf8(mAppTransaction!!.data))
            }
        }

        btn_advance_setup_confirm.setOnClickListener {
            if (TextUtils.isEmpty(et_advance_setup_gas_price.text.toString())) {
                Toast.makeText(mActivity, R.string.input_correct_gas_price_tip, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (mAppTransaction!!.isEthereum && et_advance_setup_gas_price.text.toString().trim().toDouble() < ConstantUtil.MIN_GWEI) {
                Toast.makeText(mActivity, R.string.gas_price_too_low, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(et_advance_setup_gas_limit.text.toString())) {
                Toast.makeText(mActivity, if (mAppTransaction!!.isEthereum) R.string.input_correct_gas_limit_tip else R.string.input_correct_quota_limit_tip,
                        Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (BigInteger.valueOf(et_advance_setup_gas_limit.text.toString().trim().toLong()) < ConstantUtil.GAS_LIMIT) {
                Toast.makeText(mActivity, if (mAppTransaction!!.isEthereum) R.string.gas_limit_too_low else R.string.quota_limit_too_low,
                        Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isTransfer && isNativeToken) {
                if (!TextUtils.isEmpty(et_advance_setup_pay_data.text.toString())) {
                    if (NumberUtil.isHex(et_advance_setup_pay_data.text.toString())) {
                        mAppTransaction!!.data = et_advance_setup_pay_data.text.toString()
                    } else {
                        Toast.makeText(mActivity, R.string.input_hex_data, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } else {
                    mAppTransaction!!.data = ""
                }
            }
            if (mAppTransaction!!.isEthereum) {
                mAppTransaction!!.gasLimit = BigInteger(et_advance_setup_gas_limit.text.toString().trim())
                mAppTransaction!!.gasPrice =
                        NumberUtil.getWeiFromGWeiForBigInt(et_advance_setup_gas_price.text.toString().trim().toDouble())
            } else {
                mAppTransaction!!.setQuota(et_advance_setup_gas_limit.text.toString().trim())
            }
            var intent = Intent()
            intent.putExtra(EXTRA_TRANSACTION, mAppTransaction)
            setResult(RESULT_TRANSACTION, intent)
            finish()
        }
    }

    private fun initFeeInfo() {
        if (mAppTransaction!!.isEthereum) {
            initTokenPrice()
        } else {
            requestQuotaPrice()
        }
    }

    private fun initTokenPrice() {
        TokenService.getCurrency(ConstantUtil.ETH, CurrencyUtil.getCurrencyItem(mActivity).name)
                .subscribe(object : CytonSubscriber<String>() {
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
        et_advance_setup_gas_price.setText(NumberUtil.getGWeiFromWeiForString(mAppTransaction!!.gasPrice))

        var gasMoney = NumberUtil.getDecimalValid_2(price.toDouble() * mAppTransaction!!.gas)
        if (EtherUtil.isMainNet()) {
            tv_advance_setup_gas_fee.text =
                    String.format("%s %s ≈ %s %s",
                            NumberUtil.getDecimalValid_8(mAppTransaction!!.gas),
                            getNativeToken(),
                            CurrencyUtil.getCurrencyItem(mActivity).symbol, gasMoney)
        } else {
            tv_advance_setup_gas_fee.text =
                    String.format("%s %s",
                            NumberUtil.getDecimalValid_8(mAppTransaction!!.gas),
                            getNativeToken())
        }

        tv_advance_setup_gas_fee_detail.text =
                String.format("≈Gas Limit(%s)*Gas Price(%s %s)",
                        mAppTransaction!!.gasLimit.toString(),
                        NumberUtil.getGWeiFromWeiForString(mAppTransaction!!.gasPrice), getFeeUnit())
    }



    private fun requestQuotaPrice() {
        CITARpcService.getQuotaPrice(mAppTransaction!!.from)
                .subscribe(object : CytonSubscriber<String>() {
                    override fun onNext(price: String) {
                        super.onNext(price)
                        updateQuotaPriceAndLimit(price)
                    }
                })
    }

    private fun updateQuotaPriceAndLimit(quotaPrice: String) {
        var price = NumberUtil.getDecimalValid_8(NumberUtil.getEthFromWei(BigInteger(quotaPrice)))

        et_advance_setup_gas_price.setText(price)

        var quota = mAppTransaction!!.quota.multiply(BigInteger(quotaPrice))

        tv_advance_setup_gas_fee.text = String.format("%s %s",
                NumberUtil.getDecimalValid_8(NumberUtil.getEthFromWei(quota)), getNativeToken())

        tv_advance_setup_gas_fee_detail.text =
                String.format("Quota Limit(%s)*Quota Price(%s %s)",
                        mAppTransaction!!.quota.toString(), price, getFeeUnit())
    }

    private fun getNativeToken() : String? {
        return if (mAppTransaction!!.isEthereum) {
            ConstantUtil.ETH
        } else {
            DBWalletUtil.getChainItemFromCurrentWallet(mActivity, mAppTransaction?.chainId)?.tokenSymbol
        }
    }

    private fun getFeeUnit() : String? {
        return if (mAppTransaction!!.isEthereum) getString(R.string.gwei) else getNativeToken()
    }

}
