package org.nervos.neuron.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.webkit.WebView
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import org.nervos.neuron.R
import org.nervos.neuron.activity.AddWebsiteActivity
import org.nervos.neuron.activity.AppWebActivity
import org.nervos.neuron.activity.colleactWebsite.CollectWebsiteActivity
import org.nervos.neuron.plugin.AppTabPlugin
import org.nervos.neuron.util.url.HttpUrls
import org.nervos.neuron.util.web.WebAppUtil
import org.nervos.neuron.view.TitleBar
import org.nervos.neuron.view.WebErrorView
import org.nervos.neuron.view.webview.SimpleWebViewClient

/**
 * Created by duanyytop on 2018/5/18
 */
class AppFragment : NBaseFragment() {

    companion object {
        val TAG = AppFragment::class.java.name!!
        const val COLLECT_WEBSITE = "https://dapp.cryptape.com/mine"
    }

    private var webView: WebView? = null
    private var webErrorView: WebErrorView? = null

    override val contentLayout: Int
        get() = R.layout.fragment_application

    override fun initView() {
        webView = findViewById(R.id.webview) as WebView
        webErrorView = findViewById(R.id.view_web_error) as WebErrorView
    }

    override fun initData() {
        webView!!.loadUrl(HttpUrls.DISCOVER_URL)
        initWebSettings()
        initWebView()
    }

    override fun initAction() {
        webErrorView!!.setImpl { reloadUrl ->
            webView!!.loadUrl(reloadUrl)
            webView!!.visibility = View.VISIBLE
            webErrorView!!.visibility = View.GONE
        }
        var title = findViewById(R.id.title) as TitleBar
        title.setOnRightClickListener {
            activity!!.startActivity(Intent(activity, AddWebsiteActivity::class.java))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSettings() {
        SensorsDataAPI.sharedInstance().showUpWebView(webView, false, true)
        WebAppUtil.initWebSettings(webView!!.settings)
        WebAppUtil.initWebViewCache(context, webView!!.settings)
    }

    @SuppressLint("JavascriptInterface")
    private fun initWebView() {
        webView!!.webViewClient = object : SimpleWebViewClient(context, webErrorView) {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.contains(COLLECT_WEBSITE)) {
                    startActivity(Intent(context, CollectWebsiteActivity::class.java))
                } else {
                    val intent = Intent(context, AppWebActivity::class.java)
                    intent.putExtra(AppWebActivity.EXTRA_URL, url)
                    startActivity(intent)
                }
                return true
            }
        }
        webView!!.addJavascriptInterface(AppTabPlugin(context), "appHybrid")
    }

    fun canGoBack(): Boolean {
        return webView!!.canGoBack()
    }

    fun goBack() {
        webView!!.goBack()
    }

}
