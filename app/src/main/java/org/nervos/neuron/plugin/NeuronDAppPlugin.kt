package org.nervos.neuron.plugin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView

import com.google.gson.Gson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission

import org.nervos.neuron.activity.AppWebActivity
import org.nervos.neuron.activity.AppWebActivity.RESULT_CODE_SCAN_QRCODE
import org.nervos.neuron.activity.QrCodeActivity
import org.nervos.neuron.item.NeuronDApp.FileToBase64Item
import org.nervos.neuron.item.NeuronDApp.QrCodeItem
import org.nervos.neuron.item.NeuronDApp.TakePhotoItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.util.ConstUtil
import org.nervos.neuron.util.FileUtil
import org.nervos.neuron.util.JSLoadUtils
import org.nervos.neuron.util.LogUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale

import java.io.File
import java.util.ArrayList

class NeuronDAppPlugin(private val mContext: Activity, private val mWebView: WebView) {

    companion object {
        val TAKE_PHOTO_QUALITY_LOW = "low"
        val TAKE_PHOTO_QUALITY_NORMAL = "normal"
        val TAKE_PHOTO_QUALITY_HIGH = "high"
        val TAKE_PHOTO_TEMP_PATH = ConstUtil.IMG_SAVE_PATH + "photo.jpg"
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
                    var qrCodeItem = QrCodeItem("0", "0", "Permission Denied", "")
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
            item = FileToBase64Item("1", "", "", base64!!)
        } else {
            item = FileToBase64Item("0", "3", "Find No File", "")
        }
        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(item)) }
    }

    @JavascriptInterface
    fun takePhoto(quality: String, callback: String) {
        if (!TextUtils.isEmpty(callback)) {
            val permissionList = arrayOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA)
            AndPermission.with(mContext)
                    .runtime()
                    .permission(*permissionList)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        val imageUri: Uri
                        val file = File(ConstUtil.IMG_SAVE_PATH + System.currentTimeMillis() + ".jpg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            imageUri = FileProvider.getUriForFile(mContext, "org.nervos.neuron.fileprovider", file)
                        } else {
                            imageUri = Uri.fromFile(file)
                        }
                        val intent = Intent()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        mContext.startActivityForResult(intent, AppWebActivity.RESULT_CODE_TAKE_PHOTO)
                        if (mImpl != null) mImpl!!.takePhoto(imageUri, quality, callback)
                    }
                    .onDenied { permissions ->
                        PermissionUtil.showSettingDialog(mContext, permissions)
                        val takePhotoItem = TakePhotoItem("0", "0", "Permission Denied", "")
                        JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(takePhotoItem))
                    }
                    .start()
        }
    }

    interface NeuronDAppPluginImpl {
        fun takePhoto(imageUri: Uri, quality: String, callback: String)

        fun scanCode(callback: String)
    }
}
