package org.nervos.neuron.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_transaction_detail.*
import org.nervos.neuron.R
import org.nervos.neuron.item.transaction.TransactionItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.SharePicUtils
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import org.nervos.neuron.view.TitleBar
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class TransactionDetailActivity : NBaseActivity() {

    companion object {
        val TRANSACTION_DETAIL = "TRANSACTION_DETAIL"
    }

    private var walletItem: WalletItem? = null
    private var transactionItem: TransactionItem? = null
    private var title: TitleBar? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_transaction_detail
    }

    override fun initView() {
        title = findViewById(R.id.title)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity)
        transactionItem = intent.getParcelableExtra(TRANSACTION_DETAIL)

        tv_transaction_number.text = transactionItem!!.hash
        tv_transaction_sender.text = transactionItem!!.from
        tv_transaction_receiver.text = if ("0x" == transactionItem!!.to) resources.getString(R.string.contract_create) else transactionItem!!.to
        if (!TextUtils.isEmpty(transactionItem!!.gasPrice)) {
            tv_chain_name.text = ConstantUtil.ETH_MAINNET
            val gasPriceBig = BigInteger(transactionItem!!.gasPrice)
            val gasUsedBig = BigInteger(transactionItem!!.gasUsed)
            tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(gasPriceBig.multiply(gasUsedBig)) + transactionItem!!.nativeSymbol
            tv_transaction_gas_price.text = Convert.fromWei(gasPriceBig.toString(), Convert.Unit.GWEI).toString() + " " + ConstantUtil.GWEI
            val value = (if (transactionItem!!.from.equals(walletItem!!.address, ignoreCase = true)) "-" else "+") + transactionItem!!.value
            transaction_amount.text = value
            tv_token_unit.text = transactionItem!!.symbol
            tv_transaction_blockchain_no!!.text = transactionItem!!.blockNumber
        } else {
            tv_chain_name.text = transactionItem!!.chainName
            val value = (if (transactionItem!!.from.equals(walletItem!!.address, ignoreCase = true)) "-" else "+") + transactionItem!!.value
            transaction_amount.text = value
            tv_transaction_gas.text = transactionItem!!.symbol
            tv_transaction_gas_price.visibility = View.GONE
            tv_transaction_gas_price_title.visibility = View.GONE

            try {
                val blockNumber = Integer.parseInt(Numeric.cleanHexPrefix(transactionItem!!.blockNumber), 16)
                tv_transaction_blockchain_no!!.text = blockNumber.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            AppChainRpcService.getQuotaPrice(transactionItem!!.from)
                    .subscribe(object : NeuronSubscriber<String>() {
                        override fun onNext(price: String) {
                            super.onNext(price)
                            tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(Numeric.toBigInt(transactionItem!!.gasUsed)
                                    .multiply(Numeric.toBigInt(price))) + transactionItem!!.nativeSymbol
                        }
                    })
        }

        tv_transaction_blockchain_time.text = transactionItem!!.date

        tv_transaction_receiver!!.setOnClickListener { copyText(transactionItem!!.to) }
        tv_transaction_sender!!.setOnClickListener { copyText(transactionItem!!.from) }
        tv_transaction_number!!.setOnClickListener { copyText(transactionItem!!.hash) }
    }

    override fun initAction() {
        title!!.setOnRightClickListener {
            AndPermission.with(mActivity)
                    .runtime()
                    .permission(*Permission.Group.STORAGE)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        try {
                            SharePicUtils.savePic(ConstantUtil.IMG_SAVE_PATH + transactionItem!!.blockNumber +
                                    ".png", SharePicUtils.getCacheBitmapFromView(findViewById(R.id.ll_screenshot)))
                            SharePicUtils.SharePic(this, ConstantUtil.IMG_SAVE_PATH + transactionItem!!.blockNumber + ".png")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    .onDenied { permissions ->
                        dismissProgressBar()
                        PermissionUtil.showSettingDialog(mActivity, permissions)
                    }
                    .start()
        }
        title!!.setOnLeftClickListener { finish() }
    }

    private fun copyText(value: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("value", value)
        cm.primaryClip = mClipData
        Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }

}
