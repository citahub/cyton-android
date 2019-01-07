package org.nervos.neuron.plugin

import android.app.Activity
import android.content.Intent
import android.hardware.SensorManager
import android.text.TextUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import org.nervos.neuron.activity.AppWebActivity.RESULT_CODE_SCAN_QRCODE
import org.nervos.neuron.activity.QrCodeActivity
import org.nervos.neuron.constant.NeuronDAppCallback
import org.nervos.neuron.item.NeuronDApp.DeviceMotion
import org.nervos.neuron.item.NeuronDApp.Gyroscope
import org.nervos.neuron.item.dapp.BaseNeuronDAppCallback
import org.nervos.neuron.util.JSLoadUtils
import org.nervos.neuron.util.SensorUtils
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import java.util.*

class NeuronDAppPlugin(private val mContext: Activity, private val mWebView: WebView) {

    companion object {
        const val PERMISSION_CAMERA = "CAMERA"
        const val PERMISSION_STORAGE = "STORAGE"
    }

    private var mImpl: NeuronDAppPluginImpl? = null
    private var mSensorUtils = SensorUtils(mContext)

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
                    var qrCodeItem = BaseNeuronDAppCallback(NeuronDAppCallback.ERROR_CODE,
                            NeuronDAppCallback.PERMISSION_DENIED_CODE,
                            NeuronDAppCallback.PERMISSION_DENIED)
                    JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(qrCodeItem))
                }
                .start()
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
        var permissionItem: org.nervos.neuron.item.dapp.Permission
        if (permissionList !== null) {
            permissionItem = org.nervos.neuron.item.dapp.Permission(AndPermission.hasPermissions(mContext, permissionList))
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        } else {
            permissionItem = org.nervos.neuron.item.dapp.Permission(0, NeuronDAppCallback.NO_PERMISSION_CODE, NeuronDAppCallback.NO_PERMISSION, false)
            mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
        }
    }

    @JavascriptInterface
    fun requestPermissions(info: String, callback: String) {
        var permissionList = switchPermission(info)
        var permissionItem: org.nervos.neuron.item.dapp.Permission
        if (permissionList !== null) {
            AndPermission.with(mContext)
                    .runtime()
                    .permission(*permissionList)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        permissionItem = org.nervos.neuron.item.dapp.Permission(true)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .onDenied { permissions ->
                        PermissionUtil.showSettingDialog(mContext, permissions)
                        permissionItem = org.nervos.neuron.item.dapp.Permission(false)
                        mWebView.post { JSLoadUtils.loadFunc(mWebView, callback, Gson().toJson(permissionItem)) }
                    }
                    .start()
        } else {
            permissionItem = org.nervos.neuron.item.dapp.Permission(0, NeuronDAppCallback.NO_PERMISSION_CODE, NeuronDAppCallback.NO_PERMISSION, false)
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
