package org.nervos.neuron.plugin

import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import org.nervos.neuron.activity.collection.CollectActivity
import org.nervos.neuron.activity.collectwebsite.CollectWebsiteActivity

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
