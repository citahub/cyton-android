package com.cryptape.cita_wallet.fragment.wallet

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.view.WalletTokenView

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class WalletTokenAdapter(private var address: String, private var tokenItemList: List<Token>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_EMPTY = 0

    fun refresh(tokenItemList: List<Token>) {
        this.tokenItemList = tokenItemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_empty_view, parent, false)
            (view.findViewById<View>(R.id.empty_text) as TextView).setText(R.string.empty_no_token_data)
            val iv = view.findViewById<ImageView>(R.id.iv)
            val params = iv.layoutParams as LinearLayout.LayoutParams
            params.topMargin = 10
            iv.layoutParams=params
            return object : RecyclerView.ViewHolder(view) {}
        }
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_tokens, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (tokenItemList.isEmpty()) {
            return 1
        }
        return tokenItemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (tokenItemList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else VIEW_TYPE_ITEM
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.walletTokenView.setData(address, tokenItemList[position])
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var walletTokenView: WalletTokenView = view.findViewById(R.id.view_wallet_token)
    }
}