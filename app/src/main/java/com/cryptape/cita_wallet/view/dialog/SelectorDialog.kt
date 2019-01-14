package com.cryptape.cita_wallet.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.util.DipUtils


/**
 * Created by BaojunCZ on 2018/9/11.
 */
open class SelectorDialog(private val context: Context) {

    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val view: View = LayoutInflater.from(context).inflate(R.layout.view_bottom_selector, null)
    private var closeListener: View.OnClickListener? = null
    private var okListener: View.OnClickListener? = null
    private var mRecyclerView = view.findViewById<RecyclerView>(R.id.recycler)
    private var mTvCancel = view.findViewById<TextView>(R.id.tv_cancel)
    private var mTvOk = view.findViewById<TextView>(R.id.tv_ok)

    init {
        dialog.setContentView(view)
        dialog.show()
        initAction()
    }

    private fun initAction() {
        mTvCancel.setOnClickListener { view ->
            if (closeListener != null) {
                closeListener!!.onClick(view)
            } else {
                dialog.dismiss()
            }
        }
        mTvOk.setOnClickListener {
            if (okListener != null) {
                okListener!!.onClick(view)
            } else {
                dialog.dismiss()
            }
        }
    }

    fun setOnCloseListener(listener: View.OnClickListener) {
        this.closeListener = listener
    }

    fun setOnOkListener(listener: View.OnClickListener) {
        this.okListener = listener
    }

    fun setRecyclerView(adapter: RecyclerView.Adapter<*>) {
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.adapter = adapter
    }

    fun setRecyclerHeight(height: Float) {
        mRecyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DipUtils.dip2px(context, height))
    }

    fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
        dialog.setOnDismissListener(listener)
    }

    fun dismiss() {
        dialog.dismiss()
    }

}
