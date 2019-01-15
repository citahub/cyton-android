package com.cryptape.cita_wallet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by BaojunCZ on 2018/7/30.
 */
abstract class NBaseFragment : BaseFragment() {

    var contentView: View? = null

    protected abstract val contentLayout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        contentView = inflater.inflate(contentLayout, container, false)
        return contentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
        initAction()
    }

    protected open fun initView() {}

    protected open fun initAction() {}

    protected open fun initData() {}

    fun findViewById(id: Int): View? {
        var v: View? = null
        if (contentView != null) {
            v = contentView!!.findViewById(id)
        }
        return v
    }
}
