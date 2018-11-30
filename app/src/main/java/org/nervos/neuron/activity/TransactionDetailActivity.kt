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
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.item.transaction.TransactionResponse
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.HexUtils
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.SharePicUtils
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.db.SharePrefUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import org.nervos.neuron.view.TitleBar
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class TransactionDetailActivity : NBaseActivity() {

    companion object {
        const val TRANSACTION_DETAIL = "TRANSACTION_DETAIL"
    }

    private var walletItem: WalletItem? = null
    private var transactionResponse: TransactionResponse? = null
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
        transactionResponse = intent.getParcelableExtra(TRANSACTION_DETAIL)

        tv_transaction_number.text = transactionResponse!!.hash
        tv_transaction_sender.text = transactionResponse!!.from
        tv_transaction_receiver.text =
                if (ConstantUtil.RPC_RESULT_ZERO == transactionResponse!!.to || transactionResponse!!.to.isEmpty())
                    resources.getString(R.string.contract_create)
                else transactionResponse!!.to
        val symbol = (if (transactionResponse!!.from.equals(walletItem!!.address, ignoreCase = true)) "-" else "+")
        transaction_amount.text = symbol + NumberUtil.getDecimal8ENotation(transactionResponse!!.value)
        if (!TextUtils.isEmpty(transactionResponse!!.gasPrice)) {
            tv_chain_name.text = SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_MAINNET).replace("_", " ")
            val gasPriceBig = BigInteger(transactionResponse!!.gasPrice)
            val gasUsedBig = BigInteger(transactionResponse!!.gasUsed)
            tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(gasPriceBig.multiply(gasUsedBig)) + transactionResponse!!.nativeSymbol
            tv_transaction_gas_price.text = Convert.fromWei(gasPriceBig.toString(), Convert.Unit.GWEI).toString() + " " + ConstantUtil.GWEI
            tv_token_unit.text = transactionResponse!!.symbol
            tv_transaction_blockchain_no!!.text = transactionResponse!!.blockNumber
        } else {
            tv_chain_name.text = transactionResponse!!.chainName
            tv_transaction_gas.text = transactionResponse!!.symbol
            tv_transaction_gas_price.visibility = View.GONE
            tv_transaction_gas_price_title.visibility = View.GONE

            try {
                val blockNumber = Numeric.toBigInt(transactionResponse!!.blockNumber).toString(10)
                tv_transaction_blockchain_no!!.text = blockNumber.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            AppChainRpcService.getQuotaPrice(transactionResponse!!.from)
                    .subscribe(object : NeuronSubscriber<String>() {
                        override fun onNext(price: String) {
                            super.onNext(price)
                            var gas = Numeric.toBigInt(transactionResponse!!.gasUsed).multiply(Numeric.toBigInt(price))
                            tv_transaction_gas.text =
                                    NumberUtil.getEthFromWeiForStringDecimal8(gas) + transactionResponse!!.nativeSymbol
                        }
                    })
        }

        tv_transaction_blockchain_time.text = transactionResponse!!.date

        tv_transaction_receiver!!.setOnClickListener {
            if (transactionResponse!!.to != ConstantUtil.RPC_RESULT_ZERO && !transactionResponse!!.to.isEmpty())
                copyText(transactionResponse!!.to)
        }
        tv_transaction_sender!!.setOnClickListener { copyText(transactionResponse!!.from) }
        tv_transaction_number!!.setOnClickListener { copyText(transactionResponse!!.hash) }
    }

    override fun initAction() {
        title!!.setOnRightClickListener {
            AndPermission.with(mActivity)
                    .runtime()
                    .permission(*Permission.Group.STORAGE)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        try {
                            SharePicUtils.savePic(ConstantUtil.IMG_SAVE_PATH + transactionResponse!!.blockNumber +
                                    ".png", SharePicUtils.getCacheBitmapFromView(findViewById(R.id.ll_screenshot)))
                            SharePicUtils.SharePic(this, ConstantUtil.IMG_SAVE_PATH + transactionResponse!!.blockNumber + ".png")
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
