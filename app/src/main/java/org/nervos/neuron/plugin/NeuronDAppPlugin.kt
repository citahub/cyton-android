package org.nervos.neuron.plugin

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import org.nervos.neuron.activity.AppWebActivity.RESULT_CODE_SCAN_QRCODE
import org.nervos.neuron.activity.QrCodeActivity
import org.nervos.neuron.constant.NeuronDAppCallback
import org.nervos.neuron.item.NeuronDApp.PermissionItem
import org.nervos.neuron.item.dapp.BaseNeuronDAppCallbackItem
import org.nervos.neuron.item.dapp.FileToBase64Item
import org.nervos.neuron.util.FileUtil
import org.nervos.neuron.util.JSLoadUtils
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import java.io.File
import java.util.*

class NeuronDAppPlugin(private val mContext: Activity, private val mWebView: WebView) {

    companion object {
        const val PERMISSION_CAMERA = "CAMERA"
        const val PERMISSION_STORAGE = "STORAGE"
    }


    private var mImpl: NeuronDAppPluginImpl? = null

    fun setImpl(impl: NeuronDAppPluginImpl) {
        mImpl = impl
    }

    val account: String
        @JavascriptInterface
        get() {
            val walletItem = DBWalletUtil.getCurrentWallet(mContext)
            return walletItem.address
        }

    val accounts: String
        @JavascriptInterface
        get() {
            val walletItems = DBWalletUtil.getAllWallet(mContext)
            val walletNames = ArrayList<String>()
            for (item in walletItems) {
                walletNames.add(item.address)
            }
            return Gson().toJson(walletNames)
        }

    @JavascriptInterface
    fun scanCode(callback: String) {
        AndPermission.with(mContext)
                .runtime().permission(*Permission.Group.CAMERA)
                .rationale(RuntimeRationale())
                .onGranted {
                    val intent = Intent(mContext, QrCodeActivity::class.java)
                    mContext.startActivityForResult(intent, RESULT_CODE_SCAN_QRCODE)
                    if (mImpl != null)
                        mImpl!!.scanCode(callback)
                }
                .onDenied { permissions ->
                    PermissionUtil.showSettingDialog(mContext, permissions)
                    var qrCodeItem = BaseNeuronDAppCallbackItem(NeuronDAppCallback.ERROR_CODE,
                            NeuronDAppCallback.PERMISSION_DENIED_CODE,
                            NeuronDAppCallback.PERMISSION_DENIED)
                    JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(qrCodeItem))
                }
                .start()
    }

    @JavascriptInterface
    fun fileToBase64(info: String, callback: String) {
        var base64: String? = ""
        if (!TextUtils.isEmpty(info) || info == "undefined") {
            base64 = FileUtil.fileToBase64(File(info))
        }
        val item: FileToBase64Item
        if (!TextUtils.isEmpty(base64)) {
            item = FileToBase64Item(base64!!)
        } else {
            item = FileToBase64Item(NeuronDAppCallback.ERROR_CODE, NeuronDAppCallback.FIND_NO_FILE_CODE,
                    NeuronDAppCallback.FIND_NO_FILE, "")
        }
        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(item)) }
    }

    interface NeuronDAppPluginImpl {
        fun scanCode(callback: String)
    }

    private fun switchPermission(info: String): Array<String>? {
        return when (info) {
            PERMISSION_CAMERA -> arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA)
            PERMISSION_STORAGE -> arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
            else -> null
        }
    }

    @JavascriptInterface
    fun checkPermissions(info: String, callback: String) {
        var permissionList = switchPermission(info)
        var permissionItem: PermissionItem
        if (permissionList !== null) {
            permissionItem = PermissionItem(AndPermission.hasPermissions(mContext, permissionList))
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        } else {
            permissionItem = PermissionItem(0, NeuronDAppCallback.NO_PERMISSION_CODE, NeuronDAppCallback.NO_PERMISSION, false)
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        }
    }

    @JavascriptInterface
    fun requestPermissions(info: String, callback: String) {
        var permissionList = switchPermission(info)
        var permissionItem: PermissionItem
        if (permissionList !== null) {
            AndPermission.with(mContext)
                    .runtime()
                    .permission(*permissionList)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        permissionItem = PermissionItem(true)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .onDenied { permissions ->
                        PermissionUtil.showSettingDialog(mContext, permissions)
                        permissionItem = PermissionItem(false)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .start()
        } else {
            permissionItem = PermissionItem(0, NeuronDAppCallback.NO_PERMISSION_CODE, NeuronDAppCallback.NO_PERMISSION, false)
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        }
    }
}
