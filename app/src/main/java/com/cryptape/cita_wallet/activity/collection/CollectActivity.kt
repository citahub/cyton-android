package com.cryptape.cita_wallet.activity.collection

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_collection.*
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.CollectionDetailActivity
import com.cryptape.cita_wallet.activity.NBaseActivity
import com.cryptape.cita_wallet.item.Collection
import com.cryptape.cita_wallet.item.response.CollectionResponse
import com.cryptape.cita_wallet.service.http.TokenService
import rx.Subscriber
import java.util.*

/**
 * Created by BaojunCZ on 2018/11/22.
 */
class CollectActivity : NBaseActivity() {

    companion object {
        const val EXTRA_COLLECTION = "collection"
    }

    private lateinit var mAdapter: CollectionAdapter
    private var mCollectionList: MutableList<Collection> = ArrayList()

    override fun getContentLayout(): Int {
        return R.layout.activity_collection
    }

    override fun initView() {
    }

    override fun initData() {
        mAdapter = CollectionAdapter(mActivity, mCollectionList)
        recycler.layoutManager = LinearLayoutManager(mActivity)
        recycler.adapter = mAdapter

        showProgressBar()
        getCollectionList()
    }

    override fun initAction() {
        swipe_refresh_layout.setOnRefreshListener { getCollectionList() }

        mAdapter.setOnItemClickListener { _, position ->
            val intent = Intent(mActivity, CollectionDetailActivity::class.java)
            intent.putExtra(EXTRA_COLLECTION, mCollectionList[position])
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
                        mCollectionList = collectionResponse.assets
                        mAdapter.refresh(mCollectionList)
                    }
                })
    }
}