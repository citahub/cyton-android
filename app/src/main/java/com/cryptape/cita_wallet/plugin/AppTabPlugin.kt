package com.cryptape.cita_wallet.plugin

import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import com.cryptape.cita_wallet.activity.collection.CollectActivity
import com.cryptape.cita_wallet.activity.collectwebsite.CollectWebsiteActivity

class AppTabPlugin(private val mContext: Context) {

    @JavascriptInterface
    fun toWebCollection() {
        mContext.startActivity(Intent(mContext, CollectWebsiteActivity::class.java))
    }

    @JavascriptInterface
    fun toErc721() {
        mContext.startActivity(Intent(mContext, CollectActivity::class.java))
    }

}
