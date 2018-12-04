package org.nervos.neuron.activity

import org.nervos.neuron.R

import kotlinx.android.synthetic.main.activity_advance_setup.*
import org.nervos.neuron.item.transaction.TransactionInfo
import org.web3j.utils.Convert

/**
 * Created by duanyytop on 2018/12/4.
 */
class AdvanceSetupActivity : NBaseActivity() {

    companion object {
        var EXTRA_ADVANCE_SETUP = "extra_advance_setup"
    }

    private var mTransactionInfo: TransactionInfo? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_advance_setup
    }

    override fun initView() {
        mTransactionInfo = intent?.getParcelableExtra(EXTRA_ADVANCE_SETUP)
        edit_gas_price.setText(Convert.fromWei(mTransactionInfo!!.gasPrice, Convert.Unit.GWEI).toDouble().toString())
    }

    override fun initData() {
    }

    override fun initAction() {

    }


}
