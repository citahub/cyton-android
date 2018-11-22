package org.nervos.neuron.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by 包俊 on 2018/7/30.
 */
abstract class NBaseFragment : BaseFragment() {

    var contentView: View? = null
    var isFirstLoad = true

    protected abstract val contentLayout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (contentView == null) {
            contentView = inflater.inflate(contentLayout, container, false)
            isFirstLoad = true
        } else {
            isFirstLoad = false
            val vp = contentView!!.parent as ViewGroup
            vp.removeView(contentView)
        }
        return contentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFirstLoad) {
            initView()
            initData()
            initAction()
        }
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
