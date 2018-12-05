package org.nervos.neuron.activity.addtoken

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import org.nervos.neuron.item.ChainItem
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.item.WalletItem
import org.nervos.neuron.service.http.AppChainRpcService
import org.nervos.neuron.service.http.NeuronSubscriber
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.util.permission.PermissionUtil
import org.nervos.neuron.util.permission.RuntimeRationale
import org.nervos.neuron.util.qrcode.CodeUtils
import org.nervos.neuron.util.url.HttpAppChainUrls
import org.nervos.neuron.view.TitleBar
import org.nervos.neuron.view.dialog.TokenInfoDialog

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class AddTokenActivity : NBaseActivity() {

    private lateinit var title: TitleBar

    private var chainNameList: List<String>? = null
    private var chainItemList: List<ChainItem>? = null
    private var chainItem: ChainItem? = null
    private var walletItem: WalletItem? = null

    private var manager: AddTokenManager? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_add_token
    }

    override fun initView() {
        title = findViewById(R.id.title)
    }

    override fun initData() {
        AppChainRpcService.init(this, HttpAppChainUrls.APPCHAIN_NODE_URL)

        manager = AddTokenManager(this)
        walletItem = DBWalletUtil.getCurrentWallet(this)
        chainItemList = manager!!.getChainList()
        chainNameList = manager!!.getChainNameList()
        chainItem = chainItemList!![0]

        var chainNames = chainNameList!!.toTypedArray<String?>()
        val adapter = ArrayAdapter(this, R.layout.spinner_item, chainNames)
        spinner_add_token_block_chain.adapter = adapter
    }

    override fun initAction() {
        title.setOnRightClickListener {
            startActivity(Intent(this, TokenManageActivity::class.java))
        }

        // add token data into local database
        add_token_button.setOnClickListener {
            showProgressBar()
            if (chainItem != null) {
                manager!!.loadErc20(walletItem!!.address, edit_add_token_contract_address.text.toString().trim(), chainItem!!)
                        .subscribe(object : NeuronSubscriber<TokenItem>() {
                            override fun onNext(tokenItem: TokenItem?) {
                                dismissProgressBar()
                                var tokenInfoDialog = TokenInfoDialog(mActivity, tokenItem!!)
                                tokenInfoDialog.setOnOkListener {
                                    DBWalletUtil.addTokenToWallet(mActivity, walletItem!!.name, tokenItem)
                                    tokenInfoDialog.dismiss()
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
                manager!!.loadAppChain(edit_add_token_contract_address.text.toString().trim())
                        .subscribe(object : NeuronSubscriber<ChainItem>() {
                            override fun onError(e: Throwable?) {
                                dismissProgressBar()
                                Toast.makeText(mActivity, resources.getString(R.string.appchain_node_error), Toast.LENGTH_LONG).show()
                            }

                            override fun onNext(chainItem: ChainItem) {
                                dismissProgressBar()
                                var tokenInfoDialog = TokenInfoDialog(mActivity, TokenItem(chainItem))
                                tokenInfoDialog.setOnOkListener {
                                    DBWalletUtil.saveChainInCurrentWallet(mActivity, chainItem)
                                    tokenInfoDialog.dismiss()
                                    EventBus.getDefault().post(AddTokenRefreshEvent())
                                    finish()
                                }
                            }
                        })
            }
        }

        // scan qrcode to get contract address
        add_token_contract_address_scan.setOnClickListener {
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
        spinner_add_token_block_chain.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == chainNameList!!.size - 1) {
                    add_token_contract_address_text.text = resources.getString(R.string.appchain_node)
                    edit_add_token_contract_address.hint = resources.getString(R.string.input_appchain_node)
                    chainItem = null
                } else {
                    chainItem = chainItemList!![position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

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
                    edit_add_token_contract_address!!.setText(result)
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    QrCodeActivity.track("1", false)
                    Toast.makeText(mActivity, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {

        private const val REQUEST_CODE = 0x01
    }
}
