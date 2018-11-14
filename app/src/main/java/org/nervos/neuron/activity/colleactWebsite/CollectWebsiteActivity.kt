package org.nervos.neuron.activity.colleactWebsite

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_website_collect.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.AppWebActivity
import org.nervos.neuron.activity.NBaseActivity
import org.nervos.neuron.util.db.DBAppUtil

/**
 * Created by BaojunCZ on 2018/11/14.
 */
class CollectWebsiteActivity : NBaseActivity() {

    override fun getContentLayout(): Int {
        return R.layout.activity_website_collect
    }

    override fun initView() {
        var linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
    }

    override fun initData() {
        var list = DBAppUtil.getAllApp(this)
        recycler.adapter = CollectWebsiteAdapter(list, object : CollectWebsiteAdapter.OnItemClickListener {
            override fun onItemClick(view: View, url: String) {
                val intent = Intent(mActivity, AppWebActivity::class.java)
                intent.putExtra(AppWebActivity.EXTRA_URL, url)
                startActivity(intent)
                finish()
            }

        })

    }

    override fun initAction() {
    }

}