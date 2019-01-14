package com.cryptape.cita_wallet.plugin

import android.app.Activity
import android.content.Intent
import android.hardware.SensorManager
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import com.cryptape.cita_wallet.activity.AppWebActivity.RESULT_CODE_SCAN_QRCODE
import com.cryptape.cita_wallet.activity.QrCodeActivity
import com.cryptape.cita_wallet.constant.CytonDAppCallback
import com.cryptape.cita_wallet.item.dapp.DeviceMotion
import com.cryptape.cita_wallet.item.dapp.Gyroscope
import com.cryptape.cita_wallet.item.dapp.BaseCytonDAppCallback
import com.cryptape.cita_wallet.util.JSLoadUtils
import com.cryptape.cita_wallet.util.SensorUtils
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.permission.PermissionUtil
import com.cryptape.cita_wallet.util.permission.RuntimeRationale
import java.util.*

class CytonDAppPlugin(private val mContext: Activity, private val mWebView: WebView) {

    companion object {
        const val PERMISSION_CAMERA = "CAMERA"
        const val PERMISSION_STORAGE = "STORAGE"
    }

    private var mImpl: CytonDAppPluginImpl? = null
    private var mSensorUtils = SensorUtils(mContext)

    fun setImpl(impl: CytonDAppPluginImpl) {
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
                    var qrCodeItem = BaseCytonDAppCallback(CytonDAppCallback.ERROR_CODE,
                            CytonDAppCallback.PERMISSION_DENIED_CODE,
                            CytonDAppCallback.PERMISSION_DENIED)
                    JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(qrCodeItem))
                }
                .start()
    }

    interface CytonDAppPluginImpl {
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
        var permissionItem: com.cryptape.cita_wallet.item.dapp.Permission
        if (permissionList !== null) {
            permissionItem = com.cryptape.cita_wallet.item.dapp.Permission(AndPermission.hasPermissions(mContext, permissionList))
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        } else {
            permissionItem = com.cryptape.cita_wallet.item.dapp.Permission(0, CytonDAppCallback.NO_PERMISSION_CODE, CytonDAppCallback.NO_PERMISSION, false)
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        }
    }

    @JavascriptInterface
    fun requestPermissions(info: String, callback: String) {
        var permissionList = switchPermission(info)
        var permissionItem: com.cryptape.cita_wallet.item.dapp.Permission
        if (permissionList !== null) {
            AndPermission.with(mContext)
                    .runtime()
                    .permission(*permissionList)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        permissionItem = com.cryptape.cita_wallet.item.dapp.Permission(true)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .onDenied { permissions ->
                        PermissionUtil.showSettingDialog(mContext, permissions)
                        permissionItem = com.cryptape.cita_wallet.item.dapp.Permission(false)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .start()
        } else {
            permissionItem = com.cryptape.cita_wallet.item.dapp.Permission(0, CytonDAppCallback.NO_PERMISSION_CODE, CytonDAppCallback.NO_PERMISSION, false)
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        }
    }

    @JavascriptInterface
    fun startDeviceMotionListening(info: String, callback: String) {
        if (TextUtils.isEmpty(callback)) return
        var interval = when (info) {
            SensorUtils.INTERVAL_GAME -> SensorManager.SENSOR_DELAY_GAME
            SensorUtils.INTERVAL_UI -> SensorManager.SENSOR_DELAY_UI
            else -> SensorManager.SENSOR_DELAY_NORMAL
        }
        mSensorUtils.startDeviceMotionListening(interval, object : SensorUtils.OnMotionListener {
            override fun motionListener(values: FloatArray) {
                var motion = DeviceMotion.Motion(values[2].toString(), values[0].toString(), values[1].toString())
                var motionItem = DeviceMotion(motion)
                JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(motionItem))
            }
        })
    }

    @JavascriptInterface
    fun startGyroscopeListening(info: String, callback: String) {
        if (TextUtils.isEmpty(callback)) return
        var interval = when (info) {
            SensorUtils.INTERVAL_GAME -> SensorManager.SENSOR_DELAY_GAME
            SensorUtils.INTERVAL_UI -> SensorManager.SENSOR_DELAY_UI
            else -> SensorManager.SENSOR_DELAY_NORMAL
        }
        mSensorUtils.startGyroscopeListening(interval, object : SensorUtils.OnGyroscopeListener {
            override fun gyroscopeListener(values: FloatArray) {
                var gyroscope = Gyroscope.Gyroscope(values[0].toString(), values[1].toString(), values[2].toString())
                var gyroscopeItem = Gyroscope(gyroscope)
                JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(gyroscopeItem))
            }
        })
    }

    @JavascriptInterface
    fun stopSensorListner() {
        mSensorUtils.stopListening()
    }
}
