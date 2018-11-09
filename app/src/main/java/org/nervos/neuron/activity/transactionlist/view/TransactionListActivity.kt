package org.nervos.neuron.activity.transactionlist.view

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_transaction_list.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.NBaseActivity
import org.nervos.neuron.activity.ReceiveQrCodeActivity
import org.nervos.neuron.activity.SimpleWebActivity
import org.nervos.neuron.activity.TransactionDetailActivity
import org.nervos.neuron.activity.transactionlist.model.TransactionAdapter
import org.nervos.neuron.activity.transactionlist.presenter.TransactionListPresenter
import org.nervos.neuron.activity.transfer.TransferActivity
import org.nervos.neuron.item.EthErc20TokenInfoItem
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.TransactionItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.service.http.HttpUrls
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.ConstUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.view.TitleBar
import org.web3j.crypto.Keys
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class TransactionListActivity : NBaseActivity() {

    private var transactionItemList: List<TransactionItem> = ArrayList()
    private var walletItem: WalletItem? = null

    private var titleBar: TitleBar? = null
    private var tokenItem: TokenItem? = null

    private var transactionAdapter: TransactionAdapter? = null

    private var presenter: TransactionListPresenter? = null

    private var describe: String? = null

    private val listener = object : TransactionListPresenter.TransactionListPresenterImpl {
        override fun hideProgressBar() {
            swipe_refresh_layout.post { dismissProgressBar() }
        }

        override fun setRefreshing(refreshing: Boolean) {
            swipe_refresh_layout.isRefreshing = refreshing
        }

        override fun refreshList(list: List<TransactionItem>) {
            transactionItemList = list
            transactionAdapter!!.refresh(transactionItemList)
        }

        override fun getTokenDescribe(item: EthErc20TokenInfoItem) {
            describe = item.overView.zh
            tv_token_des_first.post {
                tv_token_symbol.text = item.symbol
                tv_token_des_first.text = item.overView.zh
                setDesSecondLine()
            }
        }

        override fun showTokenDescribe(show: Boolean) {
            swipe_refresh_layout.post {
                if (show) {
                    cl_token_des.visibility = View.VISIBLE
                    presenter!!.setTokenLogo(iv_token_logo)
                    initBalance()
                    var address = tokenItem!!.contractAddress
                    if (AddressUtil.isAddressValid(address))
                        address = Keys.toChecksumAddress(address)
                    val finalAddress = address
                    cl_token_des.setOnClickListener { SimpleWebActivity.gotoSimpleWeb(mActivity, HttpUrls.TOKEN_ERC20_DETAIL.replace("@address", finalAddress)) }
                } else
                    cl_token_des.visibility = View.GONE
            }
        }

        override fun getCurrency(currency: String) {
            tv_balance.post { tv_balance.text = currency }
        }
    }

    private val isTestToken: Boolean
        get() = "NATT".equals(tokenItem!!.symbol, ignoreCase = true) || "MBA".equals(tokenItem!!.symbol, ignoreCase = true)

    override fun getContentLayout(): Int {
        return R.layout.activity_transaction_list
    }

    override fun initView() {
        titleBar = findViewById(R.id.title)
    }

    override fun initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity)
        tokenItem = intent.getParcelableExtra(TRANSACTION_TOKEN)
        titleBar!!.title = tokenItem!!.symbol
        showProgressBar()
        presenter = TransactionListPresenter(this, tokenItem, listener)
        initAdapter()
        presenter!!.getTransactionList(walletItem!!.address)
        initDescribe()
        val formater = DecimalFormat("0.####")
        formater.roundingMode = RoundingMode.FLOOR

        tv_token_warning.visibility = if (isTestToken) View.VISIBLE else View.GONE
    }

    override fun initAction() {
        receive_token.setOnClickListener { startActivity(Intent(mActivity, ReceiveQrCodeActivity::class.java)) }
        transfer_token.setOnClickListener {
            val intent = Intent(mActivity, TransferActivity::class.java)
            intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem)
            startActivity(intent)
        }
        swipe_refresh_layout.setOnRefreshListener { presenter!!.getTransactionList(walletItem!!.address) }
    }

    private fun initAdapter() {
        transaction_recycler.layoutManager = LinearLayoutManager(mActivity)
        transactionAdapter = TransactionAdapter(this, transactionItemList, walletItem!!.address)
        transaction_recycler.adapter = transactionAdapter

        transactionAdapter!!.setOnItemClickListener { _, position ->
            val item = transactionItemList[position]
            if (item.status != TransactionItem.PENDING) {
                val intent = Intent(mActivity, TransactionDetailActivity::class.java)
                intent.putExtra(TransactionDetailActivity.TRANSACTION_DETAIL, item)
                intent.putExtra(TRANSACTION_TOKEN, tokenItem)
                startActivity(intent)
            }
        }
    }

    private fun initDescribe() {
        if (presenter!!.isEthereum(tokenItem)) {
            if (!presenter!!.isNativeToken(tokenItem)) {
                presenter!!.getTokenDescribe()
            } else {
                cl_token_des.visibility = View.VISIBLE
                describe = resources.getString(R.string.ETH_Describe)
                tv_token_des_first.setText(R.string.ETH_Describe)
                tv_token_symbol.text = ConstUtil.ETH
                setDesSecondLine()
                presenter!!.getBalance()
                cl_token_des.setOnClickListener { SimpleWebActivity.gotoSimpleWeb(mActivity, HttpUrls.TOKEN_DETAIL.replace("@address", "ethereum")) }
            }
        }
    }

    private fun initBalance() {
        if (tokenItem!!.balance != 0.0 && presenter!!.isEthereum(tokenItem)) {
            presenter!!.getBalance()
        }
    }

    private fun setDesSecondLine() {
        tv_token_des_first.postDelayed({
            val layout = tv_token_des_first.layout
            val mDesText = tv_token_des_first.layout.text.toString()
            val srcStr = StringBuilder(mDesText)
            val lineStr = srcStr.subSequence(layout.getLineStart(0), layout.getLineEnd(0))
                    .toString()
            val length = lineStr.length
            if (length > 0 && describe!!.length > length
                    && tv_token_des_second.text.toString().isEmpty()) {
                val secondText: String
                if ((length * 1.5).toInt() > describe!!.length) {
                    secondText = describe!!.substring(length)
                } else {
                    secondText = describe!!.substring(length, (length * 1.5).toInt()) + "..."
                }
                tv_token_des_second.text = secondText
            }
        }, 300)
    }

    companion object {

        val TRANSACTION_TOKEN = "TRANSACTION_TOKEN"
    }

}
