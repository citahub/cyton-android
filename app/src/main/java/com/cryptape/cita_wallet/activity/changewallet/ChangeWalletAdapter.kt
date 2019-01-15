package com.cryptape.cita_wallet.activity.changewallet

import android.app.Activity
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.AddWalletActivity
import com.cryptape.cita_wallet.event.TokenRefreshEvent
import com.cryptape.cita_wallet.item.Wallet
import com.cryptape.cita_wallet.util.Blockies
import com.cryptape.cita_wallet.util.db.SharePrefUtil

/**
 * Created by BaojunCZ on 2018/11/23.
 */
class ChangeWalletAdapter(private val context: Activity, private val wallets: List<Wallet>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val WALLET = 0
    private val ADD = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == WALLET) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet, parent, false)
            WalletHolder(view)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_add, parent, false)
            AddHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WalletHolder) {
            val walletItem = wallets[position]
            holder.nameText.text = walletItem.name
            holder.addressText.text = walletItem.address
            holder.photoImage.setImageBitmap(Blockies.createIcon(walletItem.address))
            if (position == 0) {
                holder.root.background = ContextCompat.getDrawable(context, R.drawable.bg_wallet_change_item)
                holder.nameText.setTextColor(ContextCompat.getColor(context, R.color.white))
                holder.addressText.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                holder.root.background = ContextCompat.getDrawable(context, R.drawable.bg_wallet_change_item_un)
                holder.nameText.setTextColor(ContextCompat.getColor(context, R.color.font_title))
                holder.addressText.setTextColor(ContextCompat.getColor(context, R.color.font_title_third))
            }
            holder.root.setOnClickListener {
                SharePrefUtil.putCurrentWalletName(walletItem.name)
                EventBus.getDefault().post(TokenRefreshEvent())
                context.finish()
                context.overridePendingTransition(0, R.anim.wallet_activity_out)
            }
        } else if (holder is AddHolder) {
            holder.root.setOnClickListener { context.startActivity(Intent(context, AddWalletActivity::class.java)) }
        }
    }

    override fun getItemCount(): Int {
        return wallets.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == wallets.size) ADD else WALLET
    }

    internal inner class WalletHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var photoImage: ImageView = itemView.findViewById(R.id.iv_photo)
        var nameText: TextView = itemView.findViewById(R.id.tv_name)
        var addressText: TextView = itemView.findViewById(R.id.tv_address)
        var root: ConstraintLayout = itemView.findViewById(R.id.root)
    }

    internal inner class AddHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root: ConstraintLayout = itemView.findViewById(R.id.root)
    }
}


