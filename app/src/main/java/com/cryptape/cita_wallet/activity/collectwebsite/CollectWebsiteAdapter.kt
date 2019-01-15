package com.cryptape.cita_wallet.activity.collectwebsite

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.item.App
import com.cryptape.cita_wallet.util.db.DBAppUtil
import java.text.SimpleDateFormat

/**
 * Created by BaojunCZ on 2018/11/14.
 */
class CollectWebsiteAdapter(var collectWebsites: List<App>,
                            var listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_EMPTY = 0
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_empty_view, parent, false)
            (view.findViewById<View>(R.id.empty_text) as TextView).setText(R.string.empty_collect)
            return object : RecyclerView.ViewHolder(view) {

            }
        }
        var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_collect_website, parent, false)
        return CollectWebSiteViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (collectWebsites.isEmpty()) {
            return 1
        }
        return collectWebsites.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CollectWebSiteViewHolder) {
            Glide.with(context)
                    .load(collectWebsites[position].icon)
                    .into(holder.mIvIcon)
            holder.mTvTitle.text = collectWebsites[position].name
            val format = SimpleDateFormat("yyyy-MM-dd")
            var time: String
            try {
                time = format.format(collectWebsites[position].timestamp)
            } catch (e: Exception) {
                var item = collectWebsites[position]
                item.timestamp = System.currentTimeMillis()
                DBAppUtil.saveDbApp(context, item)
                time = format.format(item.timestamp)
                e.printStackTrace()
            }
            holder.mTvTime.text = time
            holder.mRoot.setOnClickListener { view -> listener.onItemClick(view, collectWebsites[position].entry) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (collectWebsites.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else VIEW_TYPE_ITEM
    }

    inner class CollectWebSiteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mIvIcon: ImageView = view.findViewById(R.id.iv_icon)
        var mTvTitle: TextView = view.findViewById(R.id.tv_title)
        var mTvTime: TextView = view.findViewById(R.id.tv_time)
        var mRoot: ConstraintLayout = view.findViewById(R.id.root)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, url: String)
    }

}