package com.cryptape.cita_wallet.view

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_token_item.view.*
import org.greenrobot.eventbus.EventBus
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.transactionlist.view.TransactionListActivity
import com.cryptape.cita_wallet.event.TokenBalanceEvent
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.service.http.CytonSubscriber
import com.cryptape.cita_wallet.service.http.TokenService
import com.cryptape.cita_wallet.service.http.WalletService
import com.cryptape.cita_wallet.util.CurrencyUtil
import com.cryptape.cita_wallet.util.NumberUtil
import com.cryptape.cita_wallet.util.TokenLogoUtil
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.db.SharePrefUtil
import com.cryptape.cita_wallet.util.ether.EtherUtil
import rx.Subscriber
import java.text.DecimalFormat

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class WalletTokenView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private lateinit var tokenItem: Token
    private lateinit var address: String

    init {
        LayoutInflater.from(context).inflate(R.layout.view_token_item, this)
        initAction()
    }

    fun setData(address: String, item: Token) {
        tokenItem = item
        this.address = address
        TokenLogoUtil.setLogo(tokenItem, context, iv_token_logo)
        tv_token_name.text = tokenItem.symbol
        if (tokenItem.balance == -1.0) {
            tv_loading.text = resources.getString(R.string.wallet_token_loading)
            tv_loading.visibility = View.VISIBLE
            tv_token_balance.visibility = View.GONE
        } else {
            tv_token_balance.text = CurrencyUtil.fmtMicrometer(NumberUtil.getDecimalValid_8(tokenItem.balance))
        }
        tv_token_currency.visibility = View.GONE
        if (EtherUtil.isEther(tokenItem)) {
            tv_chain_name.text = EtherUtil.getEthChainItem().name
        } else {
            tv_chain_name.text = tokenItem.chainName
        }
        val oldBalance = tokenItem.balance
        WalletService.getTokenBalance(context, tokenItem)
                .subscribe(object : CytonSubscriber<Token>() {

                    override fun onError(e: Throwable) {
                        tv_loading.text = resources.getString(R.string.wallet_token_loading_failed)
                        EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                    }

                    override fun onNext(item: Token) {
                        if (DBWalletUtil.getCurrentWallet(context).address == address && item.name == tokenItem.name) {
                            tokenItem = Token(item)
                            if (tokenItem.balance != oldBalance) {
                                DBWalletUtil.saveTokenBalanceCacheToWallet(context, SharePrefUtil.getCurrentWalletName(), Token(tokenItem))
                            }
                            tv_loading.visibility = View.GONE
                            tv_token_balance.text = CurrencyUtil.fmtMicrometer(NumberUtil.getDecimalValid_8(tokenItem.balance))
                            tv_token_balance.visibility = View.VISIBLE
                            if (tokenItem.balance != 0.0 && EtherUtil.isEther(tokenItem) && EtherUtil.isMainNet()) {
                                getPrice()
                            } else {
                                EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                            }
                        }
                    }
                })
    }

    private fun initAction() {
        root.setOnClickListener {
            val intent = Intent(context, TransactionListActivity::class.java)
            intent.putExtra(TransactionListActivity.TRANSACTION_TOKEN, tokenItem)
            context.startActivity(intent)
        }
    }

    private fun getPrice() {
        var currencyItem = CurrencyUtil.getCurrencyItem(context)
        TokenService.getCurrency(tokenItem.symbol, currencyItem.name).subscribe(object : Subscriber<String>() {
            override fun onCompleted() {
                if (DBWalletUtil.getCurrentWallet(context).address == address) {
                    tv_token_currency.text =
                            context.resources.getString(R.string.approximate) + currencyItem.symbol + " " + CurrencyUtil.formatCurrency(tokenItem.currencyPrice)
                    tv_token_currency.visibility = View.VISIBLE
                    EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                }
            }

            override fun onError(e: Throwable) {
                EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                e.printStackTrace()
            }

            override fun onNext(s: String) {
                if (!TextUtils.isEmpty(s)) {
                    val price = java.lang.Double.parseDouble(s.trim { it <= ' ' })
                    tokenItem.currencyPrice = java.lang.Double.parseDouble(DecimalFormat("######0.0000").format(price * tokenItem.balance))
                } else
                    tokenItem.currencyPrice = 0.00
            }
        })
    }

}