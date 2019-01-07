package org.nervos.neuron.activity.changewallet

import android.content.pm.ActivityInfo
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_change_wallet.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.NBaseActivity
import org.nervos.neuron.item.Wallet
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.db.SharePrefUtil
import java.util.*

/**
 * Created by BaojunCZ on 2018/8/3.
 */
class ChangeWalletActivity : NBaseActivity() {

    private var wallets: List<Wallet> = ArrayList()

    override fun getContentLayout(): Int {
        return R.layout.activity_change_wallet
    }

    override fun initView() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun initData() {
        wallets = DBWalletUtil.getAllWallet(this)
        for (i in wallets.indices) {
            if (wallets[i].name == SharePrefUtil.getCurrentWalletName()) {
                if (i != 0) {
                    Collections.swap(wallets, 0, i)
                }
                break
            }
        }
        wallet_recycler.layoutManager = LinearLayoutManager(this)
        wallet_recycler.adapter = ChangeWalletAdapter(this, wallets)
    }

    override fun initAction() {
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.wallet_activity_out)
    }

}
