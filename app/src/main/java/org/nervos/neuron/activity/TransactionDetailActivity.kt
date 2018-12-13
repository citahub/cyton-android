package org.nervos.neuron.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_transaction_detail.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.transactionlist.view.TransactionListActivity.TRANSACTION_STATUS
import org.nervos.neuron.activity.transactionlist.view.TransactionListActivity.TRANSACTION_TOKEN
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.item.transaction.TransactionResponse
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.util.*
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.db.SharePrefUtil
import org.nervos.neuron.util.ether.EtherUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import org.nervos.neuron.util.url.HttpUrls
import org.nervos.neuron.view.TitleBar
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class TransactionDetailActivity : NBaseActivity(), View.OnClickListener {

    companion object {
        const val TRANSACTION_DETAIL = "TRANSACTION_DETAIL"
    }

    private var walletItem: WalletItem? = null
    private var transactionResponse: TransactionResponse? = null
    private var tokenItem: TokenItem? = null
    private var title: TitleBar? = null
    private var isEther = false
    private var mTransactionStatus = TransactionResponse.PENDING

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
        mTransactionStatus = intent.getIntExtra(TRANSACTION_STATUS, TransactionResponse.PENDING)

        LogUtil.d("transaction: " + Gson().toJson(transactionResponse))

        tokenItem = intent.getParcelableExtra(TRANSACTION_TOKEN)
        isEther = EtherUtil.isEther(tokenItem)

        TokenLogoUtil.setLogo(tokenItem!!, mActivity, iv_token_icon)
        when (mTransactionStatus) {
            TransactionResponse.FAILED -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_failed)
                if (checkReceiver(transactionResponse!!.to)) {
                    tv_status.text = resources.getString(R.string.transaction_detail_contract_status_fail)
                } else {
                    tv_status.text = resources.getString(R.string.transaction_detail_status_fail)
                }
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_failed))

                //hide gas
                line_gas.visibility = View.GONE
                tv_transaction_gas_title.visibility = View.GONE
                tv_transaction_gas.visibility = View.GONE

                //hide gas price
                tv_transaction_gas_price_title.visibility = View.GONE
                tv_transaction_gas_price.visibility = View.GONE
                line_gas_price.visibility = View.GONE

                //hide query transaction
                line_receiver.visibility = View.GONE
                iv_microscope.visibility = View.GONE
                tv_microscope.visibility = View.GONE
                iv_microscope_arrow.visibility = View.GONE

                //hide gas limit
                tv_transaction_gas_limit_title.visibility = View.GONE
                tv_transaction_gas_limit.visibility = View.GONE
            }
            TransactionResponse.PENDING -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_pending)
                if (checkReceiver(transactionResponse!!.to)) {
                    tv_status.text = resources.getString(R.string.transaction_detail_contract_status_pending)
                } else {
                    tv_status.text = resources.getString(R.string.transaction_detail_status_pending)
                }
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_pending))
            }
            TransactionResponse.SUCCESS -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_success)
                if (checkReceiver(transactionResponse!!.to)) {
                    tv_status.text = resources.getString(R.string.transaction_detail_contract_status_success)
                } else {
                    tv_status.text = resources.getString(R.string.transaction_detail_status_success)
                }
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_success))

                //show transaction hash
                tv_transaction_hash_title.visibility = View.VISIBLE
                tv_transaction_hash.visibility = View.VISIBLE
                line_transaction_hash.visibility = View.VISIBLE
                tv_transaction_hash.text = transactionResponse!!.hash

                //show block chain height
                tv_transaction_blockchain_height_title.visibility = View.VISIBLE
                tv_transaction_blockchain_height.visibility = View.VISIBLE
                line_blockchain_height.visibility = View.VISIBLE
                if (isEther) {
                    tv_transaction_blockchain_height!!.text = transactionResponse!!.blockNumber
                } else {
                    try {
                        tv_transaction_blockchain_height.text = Numeric.toBigInt(transactionResponse!!.blockNumber).toString(10)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        tv_token_unit.text = tokenItem!!.symbol
        tv_transaction_sender.text = transactionResponse!!.from
        tv_transaction_sender.setOnClickListener {
            copyText(transactionResponse!!.from)
        }
        iv_transaction_sender_copy.setOnClickListener {
            copyText(transactionResponse!!.from)
        }
        if (checkReceiver(transactionResponse!!.to)) {
            tv_transaction_receiver.text = resources.getString(R.string.contract_create)
            iv_transaction_receiver_copy.visibility = View.GONE
        } else {
            tv_transaction_receiver.text = transactionResponse!!.to
            tv_transaction_receiver.setOnClickListener {
                copyText(transactionResponse!!.to)
            }
            iv_transaction_receiver_copy.setOnClickListener {
                copyText(transactionResponse!!.to)
            }
        }
        val symbol = (if (transactionResponse!!.from.equals(walletItem!!.address, ignoreCase = true)) "-" else "+")
        tv_transaction_amount.text = symbol + NumberUtil.getDecimal8ENotation(transactionResponse!!.value)

        tv_transaction_blockchain_time.text = transactionResponse!!.date

        tv_transaction_receiver!!.setOnClickListener {
            if (checkReceiver(transactionResponse!!.to)) {
                copyText(transactionResponse!!.to)
            }
        }
        tv_transaction_sender!!.setOnClickListener { copyText(transactionResponse!!.from) }
        tv_transaction_hash!!.setOnClickListener { copyText(transactionResponse!!.hash) }

        if (isEther) {
            tv_chain_name.text = SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_MAINNET).replace("_", " ")
            if (mTransactionStatus != TransactionResponse.FAILED && !TextUtils.isEmpty(transactionResponse!!.gasPrice)) {
                val gas: BigInteger
                val gasPriceBig = BigInteger(transactionResponse!!.gasPrice)
                tv_transaction_gas_price.text = Convert.fromWei(gasPriceBig.toString(), Convert.Unit.GWEI).toString() + " " + ConstantUtil.GWEI
                if (mTransactionStatus == TransactionResponse.PENDING) {
                    tv_transaction_gas_limit_title.text = resources.getString(R.string.gas_limit)
                    tv_transaction_gas_limit.text = Numeric.toBigInt(transactionResponse!!.gasLimit).toString()
                    gas = Numeric.toBigInt(transactionResponse!!.gasLimit)
                            .multiply(Numeric.toBigInt(HexUtils.IntToHex(gasPriceBig.toInt())))
                } else {
                    tv_transaction_gas_limit_title.text = resources.getString(R.string.gas_used)
                    tv_transaction_gas_limit.text = BigInteger(transactionResponse!!.gasUsed).toString()
                    gas = BigInteger(transactionResponse!!.gasPrice).multiply(BigInteger(transactionResponse!!.gasUsed))
                }
                tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(gas) + ConstantUtil.ETH
            }
            Glide.with(this)
                    .load(R.drawable.icon_eth_microscope)
                    .into(iv_microscope)
        } else {
            if (tokenItem!!.chainId != "1") {
                line_receiver.visibility = View.GONE
                iv_microscope.visibility = View.GONE
                tv_microscope.visibility = View.GONE
                iv_microscope_arrow.visibility = View.GONE
            }

            Glide.with(this)
                    .load(R.drawable.icon_appchain_microscope)
                    .into(iv_microscope)

            tv_chain_name.text = transactionResponse!!.chainName

            if (mTransactionStatus != TransactionResponse.FAILED) {
                tv_transaction_gas_price_title.text = resources.getString(R.string.quota_price)
                AppChainRpcService.getQuotaPrice(transactionResponse!!.from)
                        .subscribe(object : NeuronSubscriber<String>() {
                            override fun onNext(price: String) {
                                super.onNext(price)
                                val token = if (TextUtils.isEmpty(transactionResponse!!.nativeSymbol)) {
                                    DBWalletUtil.getChainItemFromCurrentWallet(mActivity, transactionResponse!!.chainId).tokenSymbol
                                } else {
                                    transactionResponse!!.nativeSymbol
                                }
                                tv_transaction_gas_price.text = NumberUtil.getDecimal8ENotation(NumberUtil.getEthFromWei(BigInteger(price))) + transactionResponse!!.symbol
                                val gas: BigInteger
                                if (mTransactionStatus == TransactionResponse.PENDING) {
                                    tv_transaction_gas_limit_title.text = resources.getString(R.string.quota_limit)
                                    tv_transaction_gas_limit.text = Numeric.toBigInt(transactionResponse!!.gasLimit).toString()
                                    gas = Numeric.toBigInt(transactionResponse!!.gasLimit)
                                            .multiply(Numeric.toBigInt(HexUtils.IntToHex(price.toInt())))
                                } else {
                                    tv_transaction_gas_limit_title.text = resources.getString(R.string.quota_used)
                                    tv_transaction_gas_limit.text = Numeric.toBigInt(transactionResponse!!.gasUsed).toString()
                                    gas = Numeric.toBigInt(transactionResponse!!.gasUsed)
                                            .multiply(Numeric.toBigInt(HexUtils.IntToHex(price.toInt())))
                                }
                                tv_transaction_gas.text =
                                        NumberUtil.getEthFromWeiForStringDecimal8(gas) + token
                            }
                        })
            }
        }
    }

    private fun initTransactionStatusView() {
        when (mTransactionStatus) {
            TransactionResponse.FAILED -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_failed)
                tv_status.setText(if (checkReceiver(transactionResponse!!.to)) R.string.transaction_detail_contract_status_fail else R.string.transaction_detail_status_fail)
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_failed))

                //hide gas
                line_gas.visibility = View.GONE
                tv_transaction_gas_title.visibility = View.GONE
                tv_transaction_gas.visibility = View.GONE

                //hide gas price
                tv_transaction_gas_price_title.visibility = View.GONE
                tv_transaction_gas_price.visibility = View.GONE
                line_gas_price.visibility = View.GONE

                //hide query transaction
                line_receiver.visibility = View.GONE
                iv_microscope.visibility = View.GONE
                tv_microscope.visibility = View.GONE
                iv_microscope_arrow.visibility = View.GONE

                //hide gas limit
                tv_transaction_gas_limit_title.visibility = View.GONE
                tv_transaction_gas_limit.visibility = View.GONE
            }
            TransactionResponse.PENDING -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_pending)
                tv_status.setText(if (checkReceiver(transactionResponse!!.to)) R.string.transaction_detail_contract_status_pending else R.string.transaction_detail_status_pending)
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_pending))
            }
            TransactionResponse.SUCCESS -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_success)
                tv_status.setText(if (checkReceiver(transactionResponse!!.to)) R.string.transaction_detail_contract_status_success else R.string.transaction_detail_status_success)
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_success))

                //show transaction hash
                tv_transaction_hash_title.visibility = View.VISIBLE
                tv_transaction_hash.visibility = View.VISIBLE
                line_transaction_hash.visibility = View.VISIBLE
                tv_transaction_hash.text = transactionResponse!!.hash

                //show block chain height
                tv_transaction_blockchain_height_title.visibility = View.VISIBLE
                tv_transaction_blockchain_height.visibility = View.VISIBLE
                line_blockchain_height.visibility = View.VISIBLE
                tv_transaction_blockchain_height.text = if (isEther) transactionResponse!!.blockNumber else NumberUtil.hexToDecimal(transactionResponse!!.blockNumber)
            }
        }
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
                                    ".png", SharePicUtils.getCacheBitmapFromView(cl_share))
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
        iv_microscope.setOnClickListener(this)
        tv_microscope.setOnClickListener(this)
        iv_microscope_arrow.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_microscope, R.id.tv_microscope, R.id.iv_microscope_arrow -> {
                if (isEther) {
                    SimpleWebActivity.gotoSimpleWeb(this, String.format(EtherUtil.getEtherTransactionDetailUrl(), transactionResponse!!.hash))
                } else {
                    SimpleWebActivity.gotoSimpleWeb(this, String.format(HttpUrls.APPCHAIN_TEST_TRANSACTION_DETAIL, transactionResponse!!.hash))
                }
            }
        }
    }

    private fun copyText(value: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("value", value)
        cm.primaryClip = mClipData
        Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }

    private fun checkReceiver(receiver: String): Boolean {
        return ConstantUtil.RPC_RESULT_ZERO == receiver || receiver.isEmpty()
    }

}
