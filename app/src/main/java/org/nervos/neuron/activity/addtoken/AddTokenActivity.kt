package org.nervos.neuron.activity.addtoken

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import kotlinx.android.synthetic.main.activity_add_token.*
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.activity.NBaseActivity
import org.nervos.neuron.activity.QrCodeActivity
import org.nervos.neuron.activity.TokenManageActivity
import org.nervos.neuron.event.AddTokenRefreshEvent
import org.nervos.neuron.item.Chain
import org.nervos.neuron.item.Token
import org.nervos.neuron.item.Wallet
import org.nervos.neuron.service.http.CITARpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import org.nervos.neuron.util.qrcode.CodeUtils
import org.nervos.neuron.constant.url.HttpCITAUrls
import org.nervos.neuron.view.TitleBar
import org.nervos.neuron.view.dialog.SimpleSelectDialog
import org.nervos.neuron.view.dialog.TokenInfoDialog

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
                        .subscribe(object : NeuronSubscriber<Token>() {
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
                        .subscribe(object : NeuronSubscriber<Chain>() {
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
                var dialog = SimpleSelectDialog(mActivity, mChainNameList!!)
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
