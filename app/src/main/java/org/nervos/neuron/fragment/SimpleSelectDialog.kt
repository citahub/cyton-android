package org.nervos.neuron.fragment

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import org.nervos.neuron.R
import org.nervos.neuron.view.dialog.SelectorDialog

/**
 * Created by BaojunCZ on 2018/11/22.
 */
class SimpleSelectDialog(val context: Context, val list: List<String>) : SelectorDialog(context) {

    var mSelected: Int = 0
    private var mMaxIndex: Int = 7
    private var mMaxHeight: Float = 280f

    init {
        setRecyclerView(Adapter())
        if (list.size >= mMaxIndex) {
            setRecyclerHeight(mMaxHeight)
        }
        setOnCloseListener(View.OnClickListener { dismiss() })
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_transfer_token, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = list[position]
            if (position == mSelected)
                holder.name.setTextColor(context.resources.getColor(R.color.font_link))
            else
                holder.name.setTextColor(context.resources.getColor(R.color.font_title))
            holder.root.setOnClickListener {
                mSelected = position
                notifyDataSetChanged()
            }
        }

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.tv_name)!!
        var root = view.findViewById<RelativeLayout>(R.id.root)!!
    }

}