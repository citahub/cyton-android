package org.nervos.neuron.fragment.token.model

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.nervos.neuron.R
import org.nervos.neuron.item.WalletTokenLoadItem
import org.nervos.neuron.view.WalletTokenView

/**
 * Created by BaojunCZ on 2018/11/20.
 */
class WalletTokenAdapter(private var address: String, private var tokenItemList: List<WalletTokenLoadItem>)
    : RecyclerView.Adapter<WalletTokenAdapter.ViewHolder>() {

    fun refresh(tokenItemList: List<WalletTokenLoadItem>) {
        this.tokenItemList = tokenItemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_tokens, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tokenItemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.walletTokenView.setData(address, tokenItemList[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var walletTokenView: WalletTokenView = view.findViewById(R.id.view_wallet_token)
    }
}