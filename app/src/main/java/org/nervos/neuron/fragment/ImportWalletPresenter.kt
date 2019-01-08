package org.nervos.neuron.fragment

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.activity.ImportFingerTipActivity
import org.nervos.neuron.activity.MainActivity
import org.nervos.neuron.event.TokenRefreshEvent
import org.nervos.neuron.fragment.wallet.WalletFragment
import org.nervos.neuron.item.Wallet
import org.nervos.neuron.util.ConstantUtil
import org.nervos.neuron.util.crypto.WalletEntity
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.db.SharePrefUtil
import org.nervos.neuron.util.fingerprint.FingerPrintController
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.math.BigInteger

/**
 * Created by BaojunCZ on 2018/11/28.
 */
class ImportWalletPresenter(val activity: Activity, val progress: (show: Boolean) -> Unit) {

    private fun importWallet(observable: Observable<WalletEntity>, name: String) {
        progress(true)
        observable.map {
            if (checkWalletExist(it)) {
                it
            } else {
                throw Throwable(activity.resources.getString(R.string.wallet_address_exist))
            }
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    addWallet(it, name)
                }, {
                    handleError(it)
                }, {
                    importSuccess()
                })
    }

    fun importMnemonic(mnemonic: String, password: String, path: String, name: String) {
        return importWallet(Observable.fromCallable { WalletEntity.fromMnemonic(mnemonic, path, password) }, name)
    }

    fun importKeystore(keystore: String, password: String, name: String) {
        return importWallet(Observable.fromCallable { WalletEntity.fromKeyStore(password, keystore) }, name)
    }

    fun importPrivateKey(privateKey: BigInteger, password: String, name: String) {
        return importWallet(Observable.fromCallable { WalletEntity.fromPrivateKey(privateKey, password) }, name)
    }

    private fun checkWalletExist(entity: WalletEntity): Boolean {
        return !(DBWalletUtil.checkWalletAddress(activity, entity.credentials.address))
    }

    private fun handleError(e: Throwable) {
        progress(false)
        if (TextUtils.isEmpty(e.message)) {
            Toast.makeText(activity, activity.getString(R.string.mnemonic_import_failed), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWallet(entity: WalletEntity, name: String) {
        var walletItem = Wallet.fromWalletEntity(entity)
        walletItem.name = name
        walletItem = DBWalletUtil.initChainToCurrentWallet(activity, walletItem)
        DBWalletUtil.saveWallet(activity, walletItem)
        SharePrefUtil.putCurrentWalletName(walletItem.name)
    }

    private fun importSuccess() {
        progress(false)
        if (FingerPrintController(activity).isSupportFingerprint &&
                !SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT, false) &&
                !SharePrefUtil.getBoolean(ConstantUtil.FINGERPRINT_TIP, false)) {
            activity.startActivity(Intent(activity, ImportFingerTipActivity::class.java))
            SharePrefUtil.putBoolean(ConstantUtil.FINGERPRINT_TIP, true)
        } else {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_TAG, WalletFragment.TAG)
            activity.startActivity(intent)
        }
        Toast.makeText(activity, R.string.wallet_export_success, Toast.LENGTH_SHORT).show()
        EventBus.getDefault().post(TokenRefreshEvent())
        activity.finish()
    }

}