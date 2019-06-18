package com.cryptape.cita_wallet.activity.addtoken

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_add_token.*
import org.greenrobot.eventbus.EventBus
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.activity.NBaseActivity
import com.cryptape.cita_wallet.activity.QrCodeActivity
import com.cryptape.cita_wallet.activity.TokenManageActivity
import com.cryptape.cita_wallet.event.AddTokenRefreshEvent
import com.cryptape.cita_wallet.item.Chain
import com.cryptape.cita_wallet.item.Token
import com.cryptape.cita_wallet.item.Wallet
import com.cryptape.cita_wallet.service.http.CITARpcService
import com.cryptape.cita_wallet.service.http.CytonSubscriber
import com.cryptape.cita_wallet.util.db.DBWalletUtil
import com.cryptape.cita_wallet.util.permission.PermissionUtil
import com.cryptape.cita_wallet.util.permission.RuntimeRationale
import com.cryptape.cita_wallet.util.qrcode.CodeUtils
import com.cryptape.cita_wallet.util.url.HttpCITAUrls
import com.cryptape.cita_wallet.view.TitleBar
import com.cryptape.cita_wallet.view.dialog.SimpleSelectDialog
import com.cryptape.cita_wallet.view.dialog.TokenInfoDialog

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class AddTokenActivity : NBaseActivity(), View.OnClickListener {

    private lateinit var mTitle: TitleBar

    private var mChainNameList: List<String>? = null
    private var mChainList: List<Chain>? = null
    private var mChain: Chain? = null
    private var mWallet: Wallet? = null

    private var manager: AddTokenManager? = null


    override fun getContentLayout(): Int {
        return R.layout.activity_add_token
    }

    override fun initView() {
        mTitle = findViewById(R.id.title)
    }

    override fun initData() {
        edit_add_token_contract_address.gravity = Gravity.END
        CITARpcService.init(this, HttpCITAUrls.CITA_NODE_URL)

        manager = AddTokenManager(this)
        mWallet = DBWalletUtil.getCurrentWallet(this)
        mChainList = manager!!.getChainList()
        mChainNameList = manager!!.getChainNameList(mChainList!!)
        mChain = mChainList!![0]

        tv_chain_name.text = mChainNameList!![0]
    }

    override fun initAction() {
        mTitle.setOnRightClickListener {
            startActivity(Intent(this, TokenManageActivity::class.java))
        }

        // add token data into local database
        btn_add_token.setOnClickListener {
            showProgressBar()
            if (mChain != null) {
                manager!!.loadErc20(mWallet!!.address, edit_add_token_contract_address.text!!, mChain!!)
                        .subscribe(object : CytonSubscriber<Token>() {
                            override fun onNext(token: Token?) {
                                dismissProgressBar()
                                val tokenInfoDialog = TokenInfoDialog(mActivity, token!!)
                                tokenInfoDialog.setOnOkListener {
                                    DBWalletUtil.addTokenToWallet(mActivity, mWallet!!.name, token)
                                    tokenInfoDialog.dismiss()
                                    Toast.makeText(mActivity, resources.getString(R.string.add_token_success), Toast.LENGTH_LONG).show()
                                    EventBus.getDefault().post(AddTokenRefreshEvent())
                                    finish()
                                }
                            }

                            override fun onError(e: Throwable?) {
                                dismissProgressBar()
                                Toast.makeText(mActivity, e!!.message, Toast.LENGTH_LONG).show()
                            }
                        })
            } else {
                manager!!.loadCITA(edit_add_token_contract_address.text!!)
                        .subscribe(object : CytonSubscriber<Chain>() {
                            override fun onError(e: Throwable?) {
                                dismissProgressBar()
                                Toast.makeText(mActivity, resources.getString(R.string.cita_node_error), Toast.LENGTH_LONG)
                                        .show()
                            }

                            override fun onNext(chain: Chain) {
                                dismissProgressBar()
                                var tokenInfoDialog = TokenInfoDialog(mActivity, Token(chain))
                                tokenInfoDialog.setOnOkListener {
                                    DBWalletUtil.saveChainInCurrentWallet(mActivity, chain)
                                    tokenInfoDialog.dismiss()
                                    EventBus.getDefault().post(AddTokenRefreshEvent())
                                    finish()
                                }
                            }
                        })
            }
        }

        // scan qrcode to get contract address
        iv_add_token_contract_address_scan.setOnClickListener {
            AndPermission.with(mActivity)
                    .runtime().permission(*Permission.Group.CAMERA)
                    .rationale(RuntimeRationale())
                    .onGranted {
                        val intent = Intent(mActivity, QrCodeActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE)
                    }
                    .onDenied { permissions -> PermissionUtil.showSettingDialog(mActivity, permissions) }
                    .start()
        }

        // select the type of blockchain
        tv_chain_name.setOnClickListener(this)
        iv_triangle_spinner.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_chain_name, R.id.iv_triangle_spinner -> {
                var selectedIndex = 0
                if (mChain == null) {
                    selectedIndex = mChainNameList!!.size - 1
                } else {
                    selectedIndex = mChainNameList!!.indexOf(mChain!!.name)
                }
                selectedIndex = if (selectedIndex == -1) 0 else selectedIndex
                val dialog = SimpleSelectDialog(mActivity, mChainNameList!!, selectedIndex)
                dialog.setOnOkListener(View.OnClickListener {
                    tv_chain_name.text = mChainNameList!![dialog.mSelected]
                    if (dialog.mSelected == mChainNameList!!.size - 1) {
                        tv_add_token_contract_address.text = resources.getString(R.string.cita_node)
                        edit_add_token_contract_address.hint = R.string.input_cita_node
                        mChain = null
                    } else {
                        tv_add_token_contract_address.text = resources.getString(R.string.contract_address)
                        edit_add_token_contract_address.hint = R.string.input_erc20_address
                        mChain = mChainList!![dialog.mSelected]
                    }
                    dialog.dismiss()
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                val bundle = data.extras ?: return
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    val result = bundle.getString(CodeUtils.RESULT_STRING)
                    edit_add_token_contract_address!!.text = result
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(mActivity, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {

        private const val REQUEST_CODE = 0x01
    }
}
