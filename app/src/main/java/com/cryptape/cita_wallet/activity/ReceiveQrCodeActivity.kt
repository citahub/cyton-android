package com.cryptape.cita_wallet.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_receive_qrcode.*
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.util.Blockies
import com.cryptape.cita_wallet.util.SharePicUtils
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.permission.PermissionUtil
import com.cryptape.cita_wallet.util.permission.RuntimeRationale
import com.cryptape.cita_wallet.view.TitleBar
import java.io.FileNotFoundException
import java.io.IOException


/**
 * Created by duanyytop on 2018/5/17
 */
class ReceiveQrCodeActivity : NBaseActivity() {

    private val savePath = Environment.getExternalStorageDirectory().absolutePath + "/Download/NQrCode.png"
    private var title: TitleBar? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_receive_qrcode
    }

    override fun initView() {
        title = findViewById(R.id.title)
    }

    override fun initData() {
        val walletItem = DBWalletUtil.getCurrentWallet(mActivity)
        Glide.with(this)
                .load(Blockies.createIcon(walletItem.address))
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(iv_logo)

        tv_copy!!.setOnClickListener {
            val cm = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val mClipData = ClipData.newPlainText("qrCode", walletItem.address)
            cm.primaryClip = mClipData
            Toast.makeText(mActivity, R.string.copy_success, Toast.LENGTH_SHORT).show()
        }

        tv_name!!.text = walletItem.name
        tv_address!!.text = walletItem.address

        val bitmap = CodeUtils.createImage(walletItem.address, 400, 400, null)
        iv_qrcode!!.setImageBitmap(bitmap)
    }

    override fun initAction() {
        title!!.setOnRightClickListener {
            showProgressBar()
            AndPermission.with(mActivity)
                    .runtime().permission(*Permission.Group.STORAGE)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        try {
                            tv_copy!!.visibility = View.GONE
                            SharePicUtils.savePic(savePath, SharePicUtils.getCacheBitmapFromView(rl_root))
                            tv_copy!!.visibility = View.VISIBLE
                            SharePicUtils.SharePic(this, savePath)
                            dismissProgressBar()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                            dismissProgressBar()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            dismissProgressBar()
                        }
                    }
                    .onDenied { permissions ->
                        dismissProgressBar()
                        PermissionUtil.showSettingDialog(mActivity, permissions)
                    }
                    .start()
        }
    }

}
