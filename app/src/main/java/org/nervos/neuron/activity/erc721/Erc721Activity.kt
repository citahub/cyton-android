package org.nervos.neuron.activity.erc721

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_collection.*
import org.nervos.neuron.R
import org.nervos.neuron.activity.CollectionDetailActivity
import org.nervos.neuron.activity.NBaseActivity
import org.nervos.neuron.item.CollectionItem
import org.nervos.neuron.item.response.CollectionResponse
import org.nervos.neuron.service.http.TokenService
import rx.Subscriber
import java.util.*

/**
 * Created by BaojunCZ on 2018/11/22.
 */
class Erc721Activity : NBaseActivity() {

    companion object {
        const val EXTRA_COLLECTION = "collection"
    }

    private lateinit var mAdapter: Erc721Adapter
    private var mCollectionItemList: MutableList<CollectionItem> = ArrayList()

    override fun getContentLayout(): Int {
        return R.layout.fragment_collection
    }

    override fun initView() {
    }

    override fun initData() {
        mAdapter = Erc721Adapter(mActivity, mCollectionItemList)
        recycler.layoutManager = LinearLayoutManager(mActivity)
        recycler.adapter = mAdapter

        showProgressBar()
        getCollectionList()
    }

    override fun initAction() {
        swipe_refresh_layout.setOnRefreshListener { getCollectionList() }

        mAdapter.setOnItemClickListener { _, position ->
            val intent = Intent(mActivity, CollectionDetailActivity::class.java)
            intent.putExtra(EXTRA_COLLECTION, mCollectionItemList[position])
            startActivity(intent)
        }
    }

    private fun getCollectionList() {
        TokenService.getCollectionList(mActivity)
                .subscribe(object : Subscriber<CollectionResponse>() {
                    override fun onCompleted() {
                        swipe_refresh_layout.isRefreshing = false
                        dismissProgressBar()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        swipe_refresh_layout.isRefreshing = false
                        dismissProgressBar()
                    }

                    override fun onNext(collectionResponse: CollectionResponse) {
                        mCollectionItemList = collectionResponse.assets
                        mAdapter.refresh(mCollectionItemList)
                    }
                })
    }
}