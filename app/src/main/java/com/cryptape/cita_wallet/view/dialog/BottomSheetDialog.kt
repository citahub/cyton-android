package com.cryptape.cita_wallet.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.util.DipUtils
import com.cryptape.cita_wallet.view.TitleBar
import com.cryptape.cita_wallet.view.button.CommonButton

/**
 * Created by BaojunCZ on 2018/12/3.
 */
open class BottomSheetDialog(private val context: Context) {
    private val dialog: BottomSheetDialog = BottomSheetDialog(context)
    private val view: View = LayoutInflater.from(context).inflate(R.layout.view_bottom_dialog, null)
    private lateinit var closeMethod: () -> Unit
    private lateinit var okMethod: () -> Unit
    private var mTitleBar: TitleBar = view.findViewById(R.id.title)
    private var mBtnOk: CommonButton = view.findViewById(R.id.btn_ok)
    private var mRecyclerView = view.findViewById<RecyclerView>(R.id.recycler)

    init {
        dialog.setContentView(view)
        dialog.show()
        initAction()
    }

    private fun initAction() {
        mTitleBar.setOnLeftClickListener {
            closeMethod()
        }
        mBtnOk.setOnClickListener {
            okMethod()
        }
    }

    fun setTitle(title: String) {
        mTitleBar.title = title
    }

    fun setOnCloseListener(closeMethod: () -> Unit) {
        this.closeMethod = closeMethod
    }

    fun setOnOkListener(okMethod: () -> Unit) {
        this.okMethod = okMethod
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