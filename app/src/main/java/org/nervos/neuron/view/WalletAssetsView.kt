package org.nervos.neuron.view

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_wallet_assets.view.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.ReceiveQrCodeActivity
import org.nervos.neuron.activity.transfer.TransferActivity
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.util.CurrencyUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.view.dialog.SimpleSelectDialog

/**
 * Created by BaojunCZ on 2018/11/19.
 */
class WalletAssetsView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private lateinit var mList: List<TokenItem>

    init {
        LayoutInflater.from(context).inflate(R.layout.view_wallet_assets, this)
        initView()
        initAction()
    }

    private fun initView() {
        setCurrency()
    }

    private fun initAction() {
        rl_receive.setOnClickListener {
            context.startActivity(Intent(context, ReceiveQrCodeActivity::class.java))
        }
        rl_transfer.setOnClickListener {
            mList = DBWalletUtil.getCurrentWallet(context).tokenItems
            var list = mutableListOf<String>()
            mList.forEach { item -> list.add(item.symbol) }
            var dialog = SimpleSelectDialog(context, list)
            dialog.setOnOkListener(View.OnClickListener {
                val intent = Intent(context, TransferActivity::class.java)
                intent.putExtra(TransferActivity.EXTRA_TOKEN, mList[dialog.mSelected])
                context.startActivity(intent)
                dialog.dismiss()
            })
        }
    }

    fun setCurrency() {
        tv_total_assets_title.text = (context.resources.getString(R.string.wallet_total_money) + "("
                + CurrencyUtil.getCurrencyItem(context).name + ")")
    }

    fun setTotalAssets(asserts: String) {
        tv_total_assets.text = asserts
    }
}