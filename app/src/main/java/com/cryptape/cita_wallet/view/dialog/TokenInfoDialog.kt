package com.cryptape.cita_wallet.view.dialog

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.item.Token

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class TokenInfoDialog(private val context: Context, private var mToken: Token) : BottomSheetDialog(context) {
    init {
        setTitle(context.getString(R.string.token_title))
        setRecyclerView(Adapter())
        setOnCloseListener { dismiss() }
    }

    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_erc20_detail, parent, false))
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            when (position) {
                0 -> {
                    holder.mTvTitle.text = context.getString(R.string.token_name_hint)
                    holder.mTvValue.text = mToken.name
                }
                1 -> {
                    holder.mTvTitle.text = context.getString(R.string.token_symbol_hint)
                    holder.mTvValue.text = mToken.symbol
                }
                2 -> {
                    holder.mTvTitle.text = context.getString(R.string.token_decimal_hint)
                    holder.mTvValue.text = mToken.decimals.toString()
                }
            }
        }

    }

    private inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mTvTitle = view.findViewById<TextView>(R.id.tv_title)!!
        var mTvValue = view.findViewById<TextView>(R.id.tv_value)!!
    }
}
