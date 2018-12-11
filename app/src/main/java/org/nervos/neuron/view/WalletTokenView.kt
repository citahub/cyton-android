package org.nervos.neuron.view

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_token_item.view.*
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.activity.transactionlist.view.TransactionListActivity
import org.nervos.neuron.event.TokenBalanceEvent
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.WalletTokenLoadItem
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.service.http.WalletService
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.TokenLogoUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.ether.EtherUtil
import rx.Subscriber
import java.text.DecimalFormat

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class WalletTokenView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private lateinit var tokenItem: WalletTokenLoadItem
    private lateinit var address: String

    init {
        LayoutInflater.from(context).inflate(R.layout.view_token_item, this)
        initAction()
    }

    fun setData(address: String, item: WalletTokenLoadItem) {
        tokenItem = item
        this.address = address
        TokenLogoUtil.setLogo(tokenItem, context, iv_token_logo)
        tv_token_name.text = tokenItem.symbol
        tv_loading.text = resources.getString(R.string.wallet_token_loading)
        tv_token_currency.visibility = View.GONE
        WalletService.getTokenBalance(context, tokenItem)
                .subscribe(object : NeuronSubscriber<TokenItem>() {

                    override fun onError(e: Throwable) {
                        tv_loading.text = resources.getString(R.string.wallet_token_loading_failed)
                        EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                    }

                    override fun onNext(item: TokenItem) {
                        if (DBWalletUtil.getCurrentWallet(context).address == address && item.name == tokenItem.name) {
                            tokenItem = WalletTokenLoadItem(item)
                            tv_loading.visibility = View.GONE
                            tv_token_balance.text = NumberUtil.getDecimal8ENotation(tokenItem.balance)
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
                            context.resources.getString(R.string.approximate) + currencyItem.symbol + CurrencyUtil.formatCurrency(tokenItem.currencyPrice)
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