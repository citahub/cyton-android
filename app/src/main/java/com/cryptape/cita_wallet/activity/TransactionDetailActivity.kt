package com.cryptape.cita_wallet.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_transaction_detail.*
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.transactionlist.view.TransactionListActivity.TRANSACTION_STATUS
import com.cryptape.cita_wallet.activity.transactionlist.view.TransactionListActivity.TRANSACTION_TOKEN
import com.cryptape.cita_wallet.constant.ConstantUtil
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.item.Wallet
import com.cryptape.cita_wallet.item.transaction.RestTransaction
import com.cryptape.cita_wallet.service.http.CITARpcService
import com.cryptape.cita_wallet.service.http.CytonSubscriber
import com.cryptape.cita_wallet.util.NumberUtil
import com.cryptape.cita_wallet.util.SharePicUtils
import com.cryptape.cita_wallet.util.TokenLogoUtil
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.db.SharePrefUtil
import com.cryptape.cita_wallet.util.ether.EtherUtil
import com.cryptape.cita_wallet.util.permission.PermissionUtil
import com.cryptape.cita_wallet.util.permission.RuntimeRationale
import com.cryptape.cita_wallet.constant.url.HttpUrls
import com.cryptape.cita_wallet.view.TitleBar
import org.web3j.utils.Numeric
import java.io.IOException
import java.math.BigInteger

class TransactionDetailActivity : NBaseActivity(), View.OnClickListener {

    companion object {
        const val TRANSACTION_DETAIL = "TRANSACTION_DETAIL"
    }

    private var wallet: Wallet? = null
    private var restTransaction: RestTransaction? = null
    private var token: Token? = null
    private var title: TitleBar? = null
    private var isEther = false
    private var mTransactionStatus = RestTransaction.PENDING

    override fun getContentLayout(): Int {
        return R.layout.activity_transaction_detail
    }

    override fun initView() {
        title = findViewById(R.id.title)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        wallet = DBWalletUtil.getCurrentWallet(mActivity)
        restTransaction = intent.getParcelableExtra(TRANSACTION_DETAIL)
        mTransactionStatus = intent.getIntExtra(TRANSACTION_STATUS, RestTransaction.PENDING)

        token = intent.getParcelableExtra(TRANSACTION_TOKEN)
        isEther = EtherUtil.isEther(token)

        initTransactionStatusView()

        TokenLogoUtil.setLogo(token!!, mActivity, iv_token_icon)
        tv_token_unit.text = token!!.symbol
        tv_transaction_sender.text = restTransaction!!.from
        tv_transaction_sender.setOnClickListener {
            copyText(restTransaction!!.from)
        }
        iv_transaction_sender_copy.setOnClickListener {
            copyText(restTransaction!!.from)
        }
        if (checkReceiver(restTransaction!!.to)) {
            tv_transaction_receiver.text = resources.getString(R.string.contract_create)
            iv_transaction_receiver_copy.visibility = View.GONE
        } else {
            tv_transaction_receiver.text = restTransaction!!.to
            tv_transaction_receiver.setOnClickListener {
                copyText(restTransaction!!.to)
            }
            iv_transaction_receiver_copy.setOnClickListener {
                copyText(restTransaction!!.to)
            }
        }
        val symbol = (if (restTransaction!!.from.equals(wallet!!.address, ignoreCase = true)) "-" else "+")
        tv_transaction_amount.text = symbol + restTransaction!!.value

        tv_transaction_blockchain_time.text = restTransaction!!.date

        tv_transaction_receiver!!.setOnClickListener {
            if (checkReceiver(restTransaction!!.to)) {
                copyText(restTransaction!!.to)
            }
        }
        tv_transaction_sender!!.setOnClickListener { copyText(restTransaction!!.from) }
        tv_transaction_hash!!.setOnClickListener { copyText(restTransaction!!.hash) }

        if (isEther) {
            tv_chain_name.text = SharePrefUtil.getString(ConstantUtil.ETH_NET, ConstantUtil.ETH_MAINNET).replace("_", " ")
            if (mTransactionStatus != RestTransaction.FAILED && !TextUtils.isEmpty(restTransaction!!.gasPrice)) {
                tv_transaction_gas_price_title.text = resources.getString(R.string.gas_price) + ":"
                tv_transaction_gas_price.text = NumberUtil.getGWeiFromWeiForString(BigInteger(restTransaction!!.gasPrice)) + " " + ConstantUtil.GWEI

                val gas: BigInteger
                if (mTransactionStatus == RestTransaction.PENDING) {
                    tv_transaction_gas_limit_title.text = resources.getString(R.string.gas_limit) + ":"
                    tv_transaction_gas_limit.text = restTransaction!!.gasLimit
                    gas = BigInteger(restTransaction!!.gasLimit).multiply(BigInteger(restTransaction!!.gasPrice))
                } else {
                    tv_transaction_gas_limit_title.text = resources.getString(R.string.gas_used) + ":"
                    tv_transaction_gas_limit.text = restTransaction!!.gasUsed
                    gas = BigInteger(restTransaction!!.gasPrice).multiply(BigInteger(restTransaction!!.gasUsed))
                }
                tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(gas) + " " + ConstantUtil.ETH
            }
            Glide.with(this)
                    .load(R.drawable.icon_eth_microscope)
                    .into(iv_microscope)
        } else {
            if (token!!.chainId != "1") {
                line_receiver.visibility = View.GONE
                iv_microscope.visibility = View.GONE
                tv_microscope.visibility = View.GONE
                iv_microscope_arrow.visibility = View.GONE
            }

            Glide.with(this)
                    .load(R.drawable.icon_cita_microscope)
                    .into(iv_microscope)

            tv_chain_name.text = restTransaction!!.chainName

            if (mTransactionStatus != RestTransaction.FAILED) {
                tv_transaction_gas_price_title.text = resources.getString(R.string.quota_price) + ":"
                initQuotaInfo()
            }
        }
    }

    private fun initTransactionStatusView() {
        when (mTransactionStatus) {
            RestTransaction.FAILED -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_failed)
                tv_status.setText(if (checkReceiver(restTransaction!!.to)) R.string.transaction_detail_contract_status_fail else R.string.transaction_detail_status_fail)
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
            RestTransaction.PENDING -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_pending)
                tv_status.setText(if (checkReceiver(restTransaction!!.to)) R.string.transaction_detail_contract_status_pending else R.string.transaction_detail_status_pending)
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_pending))
            }
            RestTransaction.SUCCESS -> {
                tv_status.background = ContextCompat.getDrawable(this, R.drawable.bg_transaction_detail_success)
                tv_status.setText(if (checkReceiver(restTransaction!!.to)) R.string.transaction_detail_contract_status_success else R.string.transaction_detail_status_success)
                tv_status.setTextColor(ContextCompat.getColor(this, R.color.transaction_detail_success))

                //show transaction hash
                tv_transaction_hash_title.visibility = View.VISIBLE
                tv_transaction_hash.visibility = View.VISIBLE
                line_transaction_hash.visibility = View.VISIBLE
                tv_transaction_hash.text = restTransaction!!.hash

                //show block chain height
                tv_transaction_blockchain_height_title.visibility = View.VISIBLE
                tv_transaction_blockchain_height.visibility = View.VISIBLE
                line_blockchain_height.visibility = View.VISIBLE
                tv_transaction_blockchain_height.text = if (isEther) restTransaction!!.blockNumber else NumberUtil.hexToDecimal(restTransaction!!.blockNumber)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initQuotaInfo() {
        CITARpcService.getQuotaPrice(restTransaction!!.from)
                .subscribe(object : CytonSubscriber<String>() {
                    override fun onNext(price: String) {
                        super.onNext(price)
                        val tokenUnit = if (TextUtils.isEmpty(restTransaction!!.nativeSymbol)) {
                            DBWalletUtil.getChainItemFromCurrentWallet(mActivity, restTransaction!!.chainId).tokenSymbol
                        } else {
                            restTransaction!!.nativeSymbol
                        }
                        tv_transaction_gas_price.text = NumberUtil.getDecimalValid_8(NumberUtil.getEthFromWei(BigInteger(price))) + " " + tokenUnit
                        val gas: BigInteger
                        if (mTransactionStatus == RestTransaction.PENDING) {
                            tv_transaction_gas_limit_title.text = resources.getString(R.string.quota_limit) + ":"
                            tv_transaction_gas_limit.text = restTransaction!!.gasLimit
                            gas = BigInteger(restTransaction!!.gasLimit).multiply(BigInteger(price))
                        } else {
                            tv_transaction_gas_limit_title.text = resources.getString(R.string.quota_used) + ":"
                            tv_transaction_gas_limit.text = Numeric.toBigInt(restTransaction!!.gasUsed).toString()
                            gas = Numeric.toBigInt(restTransaction!!.gasUsed).multiply(BigInteger(price))
                        }
                        tv_transaction_gas.text = NumberUtil.getEthFromWeiForStringDecimal8(gas) + " " + tokenUnit
                    }
                })
    }

    override fun initAction() {
        title!!.setOnRightClickListener {
            AndPermission.with(mActivity)
                    .runtime()
                    .permission(*Permission.Group.STORAGE)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        try {
                            SharePicUtils.savePic(ConstantUtil.IMG_SAVE_PATH + restTransaction!!.blockNumber +
                                    ".png", SharePicUtils.getCacheBitmapFromView(cl_share))
                            SharePicUtils.SharePic(this, ConstantUtil.IMG_SAVE_PATH + restTransaction!!.blockNumber + ".png")
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
                    SimpleWebActivity.gotoSimpleWeb(this, String.format(EtherUtil.getEtherTransactionDetailUrl(), restTransaction!!.hash))
                } else {
                    SimpleWebActivity.gotoSimpleWeb(this, String.format(HttpUrls.CITA_TEST_TRANSACTION_DETAIL, restTransaction!!.hash))
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
