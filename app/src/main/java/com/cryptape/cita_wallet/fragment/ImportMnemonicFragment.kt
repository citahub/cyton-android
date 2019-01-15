package com.cryptape.cita_wallet.fragment

import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_import_mnemonic.*
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.util.NumberUtil
import com.cryptape.cita_wallet.util.WalletTextWatcher
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import java.util.*

/**
 * Created by BaojunCZ on 2018/11/28.
 */
class ImportMnemonicFragment : NBaseFragment() {

    lateinit var paths: List<String>

    private lateinit var presenter: ImportWalletPresenter
    private var check1 = false
    private var check2 = false
    private var check3 = false
    private var check4 = false

    override val contentLayout: Int
        get() = R.layout.fragment_import_mnemonic

    private val isWalletValid: Boolean
        get() = check1 && check2 && check3 && check4

    override fun initView() {
    }

    override fun initData() {
        paths = Arrays.asList(*resources.getStringArray(R.array.mnemonic_path))
        presenter = ImportWalletPresenter(activity!!) { show ->
            if (show)
                showProgressBar()
            else
                dismissProgressBar()
        }
    }

    override fun initAction() {
        cb_import.setOnClickListener {
            if (!NumberUtil.isPasswordOk(et_password.text.toString().trim())) {
                Toast.makeText(context, R.string.password_weak, Toast.LENGTH_SHORT).show()
            } else if (!TextUtils.equals(et_password.text.toString().trim(), et_repassword.text.toString().trim())) {
                Toast.makeText(context, R.string.password_not_same, Toast.LENGTH_SHORT).show()
            } else if (DBWalletUtil.checkWalletName(context, et_name.text.toString())) {
                Toast.makeText(context, R.string.wallet_name_exist, Toast.LENGTH_SHORT).show()
            } else {
                generateAndSaveWallet()
            }
        }
        checkWalletStatus()
    }

    private fun generateAndSaveWallet() {
        showProgressBar(R.string.wallet_importing)
        presenter.importMnemonic(et_mnemonic.text.toString().trim(),
                et_password.text.toString().trim(), paths[0], et_name.text.toString().trim())
    }

    private fun checkWalletStatus() {
        et_name!!.addTextChangedListener(object : WalletTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                super.onTextChanged(charSequence, i, i1, i2)
                check1 = !TextUtils.isEmpty(et_name.text.toString().trim())
                cb_import.setClickAble(isWalletValid)
            }
        })
        et_password.addTextChangedListener(object : WalletTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                super.onTextChanged(charSequence, i, i1, i2)
                check2 = !TextUtils.isEmpty(et_password.text.toString().trim())
                        && et_password.text.toString().trim().length >= 8
                cb_import.setClickAble(isWalletValid)
            }
        })
        et_repassword.addTextChangedListener(object : WalletTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                super.onTextChanged(charSequence, i, i1, i2)
                check3 = !TextUtils.isEmpty(et_repassword!!.text.toString().trim())
                        && et_repassword.text.toString().trim().length >= 8
                cb_import.setClickAble(isWalletValid)
            }
        })
        et_mnemonic.addTextChangedListener(object : WalletTextWatcher() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                super.onTextChanged(charSequence, i, i1, i2)
                check4 = !TextUtils.isEmpty(et_mnemonic.text.toString().trim())
                cb_import.setClickAble(isWalletValid)
            }
        })
    }

}
