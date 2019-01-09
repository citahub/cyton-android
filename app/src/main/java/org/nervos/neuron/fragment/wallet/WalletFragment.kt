package org.nervos.neuron.fragment.wallet

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.fragment_wallet.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.nervos.neuron.R
import org.nervos.neuron.activity.AddWalletActivity
import org.nervos.neuron.activity.addtoken.AddTokenActivity
import org.nervos.neuron.activity.changewallet.ChangeWalletActivity
import org.nervos.neuron.event.AddTokenRefreshEvent
import org.nervos.neuron.event.CurrencyRefreshEvent
import org.nervos.neuron.event.TokenBalanceEvent
import org.nervos.neuron.event.TokenRefreshEvent
import org.nervos.neuron.fragment.NBaseFragment
import org.nervos.neuron.item.Token
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.view.TitleBar
import org.nervos.neuron.view.WalletAssetsView

/**
 * Created by BaojunCZ on 2018/11/19.
 */
class WalletFragment : NBaseFragment(), View.OnClickListener {

    companion object {
        val TAG = WalletFragment::class.java.name!!
    }

    private var mWalletAssetsView: WalletAssetsView? = null
    private var mTitleBar: TitleBar? = null
    private var mTokenItemList: MutableList<Token> = mutableListOf()
    private lateinit var mAdapter: WalletTokenAdapter
    private lateinit var mCircleAnim: Animation

    override val contentLayout: Int
        get() = R.layout.fragment_wallet

    override fun initView() {
        mWalletAssetsView = findViewById(R.id.view_wallet_assets) as WalletAssetsView
        mTitleBar = findViewById(R.id.title) as TitleBar
    }

    override fun initData() {
        loadExchangeBtn()
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.isNestedScrollingEnabled = false
        reloadTokens()
    }

    override fun initAction() {
        iv_right_arrow.setOnClickListener(this)
        tv_add_token.setOnClickListener(this)
        iv_refresh.setOnClickListener(this)
        mTitleBar!!.setOnRightClickListener {
            if (DBWalletUtil.getAllWallet(activity).size > 1) {
                activity!!.startActivity(Intent(activity, ChangeWalletActivity::class.java))
                activity!!.overridePendingTransition(R.anim.wallet_activity_in, 0)
            } else {
                activity!!.startActivity(Intent(context, AddWalletActivity::class.java))
            }
        }
        swipe_refresh_layout.setOnRefreshListener {
            iv_refresh.visibility = View.GONE
            refreshTokens(false)
        }
    }

    //reload list after exchanging wallet
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWalletSaveEvent(event: TokenRefreshEvent) {
        loadExchangeBtn()
        reloadTokens()
    }

    //refresh list after adding token
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddTokenEvent(event: AddTokenRefreshEvent) {
        reloadTokens()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCurrencyRefreshEvent(event: CurrencyRefreshEvent) {
        mWalletAssetsView!!.setCurrency()
        mWalletAssetsView!!.setTotalAssets(resources.getString(R.string.wallet_assets_start_query))
        reloadTokens()
    }

    //Subscribe token balance and price
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTokenBalaceEvent(event: TokenBalanceEvent) {
        var isLoadedAll = true
        var totalAssets = 0.0
        mTokenItemList.forEachIndexed { index, item ->
            if (event.item.contractAddress == item.contractAddress && event.item.chainId == item.chainId
                    && event.item.name == item.name && event.item.symbol == item.symbol) {
                mTokenItemList[index] = event.item
                mTokenItemList[index].loaded = true
                totalAssets += event.item.currencyPrice
            } else {
                if (!item.loaded) isLoadedAll = false
            }
            totalAssets += item.currencyPrice
            if (totalAssets != 0.0) {
                mWalletAssetsView!!.setTotalAssets("≈" + CurrencyUtil.getCurrencyItem(context).symbol
                        + " " + CurrencyUtil.formatCurrency(totalAssets))
            }
        }
        if (isLoadedAll) {
            finishRefresh()
            if (totalAssets == 0.0) mWalletAssetsView!!.setTotalAssets(resources.getString(R.string.wallet_token_no_assets))
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.iv_right_arrow, R.id.tv_add_token -> startActivity(Intent(activity, AddTokenActivity::class.java))
            R.id.iv_refresh -> {
                if (swipe_refresh_layout.isEnabled)
                    refreshTokens(true)
            }
        }
    }

    private fun getMyTokens() {
        var list = DBWalletUtil.getCurrentWallet(context).tokens
        mTokenItemList.clear()
        list.forEachIndexed { _, tokenItem ->
            if (tokenItem.selected)
                mTokenItemList.add(Token(tokenItem))
        }
        if (mTokenItemList.size == 0) {
            finishRefresh()
            mWalletAssetsView!!.setTotalAssets(resources.getString(R.string.wallet_token_no_assets))
        }
    }

    private fun reloadTokens() {
        startIvRefresh()
        getMyTokens()
        mAdapter = WalletTokenAdapter(DBWalletUtil.getCurrentWallet(context).address, mTokenItemList)
        recycler.adapter = mAdapter
    }

    private fun refreshTokens(isIvRefresh: Boolean) {
        if (isIvRefresh)
            startIvRefresh()
        getMyTokens()
        mAdapter.refresh(mTokenItemList)
    }

    //iv_refresh animation
    private fun startIvRefresh() {
        swipe_refresh_layout.isEnabled = false
        mCircleAnim = AnimationUtils.loadAnimation(context, R.anim.anim_round_rotate)
        val interpolator = LinearInterpolator()
        mCircleAnim.interpolator = interpolator
        iv_refresh.startAnimation(mCircleAnim)
    }

    private fun finishRefresh() {
        swipe_refresh_layout.isRefreshing = false
        swipe_refresh_layout.isEnabled = true
        iv_refresh.clearAnimation()
        iv_refresh.visibility = View.VISIBLE
    }

    private fun loadExchangeBtn() {
        if (DBWalletUtil.getAllWalletName(activity!!).size > 1) {
            mTitleBar!!.setRightImage(R.drawable.ic_wallet_exchange)
        } else {
            mTitleBar!!.setRightImage(R.drawable.ic_wallet_rec_add)
        }
    }
}