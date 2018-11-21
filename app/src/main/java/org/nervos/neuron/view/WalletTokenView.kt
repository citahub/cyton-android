package org.nervos.neuron.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.view_token_item.view.*
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.activity.transactionlist.view.TransactionListActivity
import org.nervos.neuron.event.TokenBalanceEvent
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.item.WalletTokenLoadItem
import org.nervos.neuron.service.http.TokenService
import org.nervos.neuron.service.http.WalletService
import org.nervos.neuron.util.AddressUtil
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.LogUtil
import org.nervos.neuron.util.NumberUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.url.HttpUrls
import org.web3j.crypto.Keys
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
        setLogo()
        tv_token_name.text = tokenItem.symbol
        tv_loading.text = resources.getString(R.string.wallet_token_loading)
        tv_token_currency.visibility = View.GONE
        WalletService.getTokenBalance(context, tokenItem, object : WalletService.OnGetWalletTokenListener {
            override fun onGetWalletToken(walletItem: WalletItem?) {
            }

            override fun onGetWalletError(message: String?) {
                tv_loading.post {
                    tv_loading.text = resources.getString(R.string.wallet_token_loading_failed)
                    EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                }
            }

            override fun onGetTokenBalance(_tokenItem: TokenItem?) {
                tv_loading.post {
                    if (DBWalletUtil.getCurrentWallet(context).address == address && _tokenItem!!.name == tokenItem.name) {
                        tokenItem = WalletTokenLoadItem(_tokenItem)
                        tv_loading.visibility = View.GONE
                        tv_token_balance.text = NumberUtil.getDecimal8ENotation(tokenItem.balance)
                        tv_token_balance.visibility = View.VISIBLE
                        if (tokenItem.balance != 0.0 && tokenItem.chainId < 0) {
                            getPrice()
                        } else {
                            EventBus.getDefault().post(TokenBalanceEvent(tokenItem, address))
                        }
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

    private fun setLogo() {
        if (tokenItem.chainId < 0 && !TextUtils.isEmpty(tokenItem.contractAddress)) {
            var address = tokenItem.contractAddress
            if (AddressUtil.isAddressValid(address))
                address = Keys.toChecksumAddress(address)
            val options = RequestOptions()
                    .error(R.drawable.ether_big)
                    .placeholder(R.drawable.ether_big)
            Glide.with(context)
                    .load(Uri.parse(String.format(HttpUrls.TOKEN_LOGO, address)))
                    .apply(options)
                    .into(iv_token_logo)

        } else {
            var icon = if (tokenItem.chainId < 0) R.drawable.ether_big else R.mipmap.ic_launcher
            val options = RequestOptions()
                    .error(icon)
                    .placeholder(icon)
            Glide.with(context)
                    .load(if (TextUtils.isEmpty(tokenItem.avatar)) "" else Uri.parse(tokenItem.avatar))
                    .apply(options)
                    .into(iv_token_logo)
        }
    }

    private fun getPrice() {
        var currencyItem = CurrencyUtil.getCurrencyItem(context)
        TokenService.getCurrency(tokenItem.symbol, currencyItem.name).subscribe(object : Subscriber<String>() {
            override fun onCompleted() {
                if (DBWalletUtil.getCurrentWallet(context).address == address) {
                    LogUtil.e(tokenItem.symbol + "----" + tokenItem.currencyPrice)
                    val df = DecimalFormat("######0.00")
                    tv_token_currency.text = currencyItem.symbol + df.format(tokenItem.currencyPrice)
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
                    val df = DecimalFormat("######0.0000")
                    tokenItem.currencyPrice = java.lang.Double.parseDouble(df.format(price * tokenItem.balance))
                } else
                    tokenItem.currencyPrice = 0.00
            }
        })
    }

}