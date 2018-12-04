package org.nervos.neuron.activity

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_token_manage.*
import org.greenrobot.eventbus.EventBus
import org.nervos.neuron.R
import org.nervos.neuron.event.AddTokenRefreshEvent
import org.nervos.neuron.item.TokenItem
import org.nervos.neuron.util.TokenLogoUtil
import org.nervos.neuron.util.db.DBWalletUtil
import org.nervos.neuron.view.TitleBar
import java.util.*

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class TokenManageActivity : NBaseActivity() {

    private var titleBar: TitleBar? = null
    private var tokenList: List<TokenItem> = ArrayList()
    private val adapter = TokenAdapter()

    override fun getContentLayout(): Int {
        return R.layout.activity_token_manage
    }

    override fun initView() {
        titleBar = findViewById(R.id.title)
    }

    override fun initData() {
        addCustomToken()
        token_recycler.layoutManager = LinearLayoutManager(mActivity)
        token_recycler.adapter = adapter
    }

    override fun initAction() {
        titleBar!!.setOnRightClickListener { }
        titleBar!!.setOnLeftClickListener {
            EventBus.getDefault().post(AddTokenRefreshEvent())
            finish()
        }
    }

    private fun addCustomToken() {
        tokenList = DBWalletUtil.getCurrentWallet(mActivity).tokenItems
        adapter.notifyDataSetChanged()
    }

    internal inner class TokenAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return TokenViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_token_info, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is TokenViewHolder) {
                TokenLogoUtil.setLogo(tokenList[position], mActivity, holder.tokenImage)
                holder.tokenName.text = tokenList[position].name
                holder.tokenSymbol.text = tokenList[position].symbol
                if (!TextUtils.isEmpty(tokenList[position].contractAddress)) {
                    holder.tokenContractAddress.text = tokenList[position].contractAddress
                } else {
                    holder.tokenContractAddress.visibility = View.GONE
                }
                holder.tokenSelectSwitch.setOnCheckedChangeListener(null)
                holder.tokenSelectSwitch.isChecked = tokenList[position].selected

                holder.tokenSelectSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    tokenList[position].selected = isChecked
                    if (isChecked) {
                        DBWalletUtil.updateTokenToCurrentWallet(mActivity, tokenList[position])
                    } else {
                        DBWalletUtil.updateTokenToCurrentWallet(mActivity, tokenList[position])
                    }
                }
                holder.itemView.tag = position
            }
        }

        override fun getItemCount(): Int {
            return tokenList.size
        }

        internal inner class TokenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tokenImage: ImageView = view.findViewById(R.id.token_image)
            var tokenName: TextView = view.findViewById(R.id.token_name_text)
            var tokenSymbol: TextView = view.findViewById(R.id.token_symbol_text)
            var tokenContractAddress: TextView = view.findViewById(R.id.token_contract_address)
            var tokenSelectSwitch: Switch = view.findViewById(R.id.switch_token_select)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            EventBus.getDefault().post(AddTokenRefreshEvent())
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
