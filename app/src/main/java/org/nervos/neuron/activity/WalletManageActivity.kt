package org.nervos.neuron.activity

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_wallet_manage.*
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.event.TokenRefreshEvent
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.service.http.WalletService
import org.nervos.neuron.util.Blockies
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.db.SharePrefUtil
import org.nervos.neuron.view.SettingButtonView
import org.nervos.neuron.view.dialog.SimpleDialog

/**
 * Created by duanyytop on 2018/11/7
 */
class WalletManageActivity : NBaseActivity() {

    private var walletItem: WalletItem? = null

    override fun getContentLayout(): Int = R.layout.activity_wallet_manage

    override fun initView() {}

    override fun initData() {
        walletItem = DBWalletUtil.getCurrentWallet(this)

        wallet_name_text.text = walletItem!!.name
        wallet_address.text = walletItem!!.address
        wallet_photo.setImageBitmap(Blockies.createIcon(walletItem!!.address))
    }

    override fun initAction() {
        wallet_name_layout.setOnClickListener {
            val simpleDialog = SimpleDialog(mActivity)
            simpleDialog.setTitle(getString(R.string.update_wallet_name))
            simpleDialog.setMessageHint(getString(R.string.input_wallet_name_hint))
            simpleDialog.setOnOkClickListener {
                updateWalletName(simpleDialog)
            }
            simpleDialog.setOnCancelClickListener { simpleDialog.dismiss() }
            simpleDialog.show()
        }

        change_password.setOnClickListener(SettingButtonView.OnClickListener {
            startActivity(Intent(mActivity, ChangePasswordActivity::class.java))
        })

        export_keystore.setOnClickListener(SettingButtonView.OnClickListener {
            val simpleDialog = SimpleDialog(mActivity)
            simpleDialog.setTitle(R.string.input_password_hint)
            simpleDialog.setMessageHint(R.string.input_password_hint)
            simpleDialog.setEditInputType(SimpleDialog.PASSWORD)
            simpleDialog.setOnOkClickListener {
                exportKeystore(simpleDialog)
            }
            simpleDialog.setOnCancelClickListener { simpleDialog.dismiss() }
            simpleDialog.show()
        })

        delete_wallet_button.setOnClickListener {
            val deleteDialog = SimpleDialog(mActivity)
            deleteDialog.setTitle(getString(R.string.ask_confirm_delete_wallet))
            deleteDialog.setMessageHint(getString(R.string.password))
            deleteDialog.setEditInputType(SimpleDialog.PASSWORD)
            deleteDialog.setOnCancelClickListener { deleteDialog.dismiss() }
            deleteDialog.setOnOkClickListener {
                deleteWallet(deleteDialog)
            }
            deleteDialog.show()
        }

    }

    private fun updateWalletName(simpleDialog: SimpleDialog) {
        when {
            TextUtils.isEmpty(simpleDialog.message.trim()) -> Toast.makeText(mActivity, R.string.wallet_name_not_null, Toast.LENGTH_SHORT).show()
            DBWalletUtil.checkWalletName(mActivity, simpleDialog.message) -> Toast.makeText(mActivity, R.string.wallet_name_exist, Toast.LENGTH_SHORT).show()
            else -> {
                wallet_name_text.text = simpleDialog.message
                if (DBWalletUtil.updateWalletName(mActivity, walletItem!!.name, simpleDialog.message)) {
                    SharePrefUtil.putCurrentWalletName(simpleDialog.message)
                    walletItem = DBWalletUtil.getCurrentWallet(this@WalletManageActivity)
                } else {
                    Toast.makeText(this@WalletManageActivity, R.string.change_wallet_name_failed, Toast.LENGTH_LONG).show()
                }
                simpleDialog.dismiss()
            }
        }
    }

    private fun exportKeystore(simpleDialog: SimpleDialog) {
        if (TextUtils.isEmpty(simpleDialog.message)) {
            Toast.makeText(mActivity, R.string.password_not_null, Toast.LENGTH_SHORT).show()
        } else if (!WalletService.checkPassword(mActivity, simpleDialog.message, walletItem)) {
            Toast.makeText(mActivity, R.string.wallet_password_error, Toast.LENGTH_SHORT).show()
        } else {
            generateKeystore()
            simpleDialog.dismiss()
        }
    }

    private fun deleteWallet(deleteDialog: SimpleDialog) {
        if (!WalletService.checkPassword(mActivity, deleteDialog.message, walletItem)) {
            Toast.makeText(mActivity, R.string.wallet_password_error, Toast.LENGTH_SHORT).show()
        } else {
            val names = DBWalletUtil.getAllWalletName(mActivity)
            if (names.size > 1) {
                SharePrefUtil.putCurrentWalletName(names[if (names.indexOf(walletItem!!.name) == 0) 1 else 0])
            } else if (names.size > 0) {
                SharePrefUtil.deleteWalletName()
            }
            DBWalletUtil.deleteWallet(mActivity, walletItem!!.name)
            deleteDialog.dismiss()
            Toast.makeText(mActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
            EventBus.getDefault().post(TokenRefreshEvent())
            finish()
        }
    }

    private fun generateKeystore() {
        val intent = Intent(mActivity, ExportKeystoreActivity::class.java)
        intent.putExtra(ExportKeystoreActivity.EXTRA_KEYSTORE, walletItem!!.keystore)
        startActivity(intent)
    }


}
