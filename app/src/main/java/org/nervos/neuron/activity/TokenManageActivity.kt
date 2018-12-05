package org.nervos.neuron.activity

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
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

/**
 * Created by BaojunCZ on 2018/12/3.
 */
class TokenManageActivity : NBaseActivity() {

    private lateinit var mTitleBar: TitleBar
    private var mTokenList = mutableListOf<TokenItem>()
    private val mAdapter = TokenAdapter()
    //token item status true:normal false:draged
    private var mTokenStatus = true
    private val mItemTouchHelper = ItemTouchHelper(SimpleItemTouchCallback())

    override fun getContentLayout(): Int {
        return R.layout.activity_token_manage
    }

    override fun initView() {
        mTitleBar = findViewById(R.id.title)
    }

    override fun initData() {
        addCustomToken()
        token_recycler.layoutManager = LinearLayoutManager(mActivity)
        token_recycler.adapter = mAdapter
        mItemTouchHelper.attachToRecyclerView(token_recycler)
    }

    override fun initAction() {
        mTitleBar.setOnRightClickListener { }
        mTitleBar.setOnLeftClickListener {
            EventBus.getDefault().post(AddTokenRefreshEvent())
            finish()
        }
        mTitleBar.setOnRightClickListener {
            if (mTokenStatus) {
                mTitleBar.rightText = resources.getString(R.string.finish)
            } else {
                mTitleBar.rightText = resources.getString(R.string.edit)
            }
            mTokenStatus = !mTokenStatus
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun addCustomToken() {
        mTokenList = DBWalletUtil.getCurrentWallet(mActivity).tokenItems
        mAdapter.notifyDataSetChanged()
    }

    internal inner class TokenAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return TokenViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_token_info, parent, false))
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is TokenViewHolder) {
                TokenLogoUtil.setLogo(mTokenList[position], mActivity, holder.mIvTokenImage)
                holder.mTvTokenName.text = mTokenList[position].name
                holder.mTvTokenSymbol.text = mTokenList[position].symbol
                if (!TextUtils.isEmpty(mTokenList[position].contractAddress)) {
                    holder.mTvTokenContractAddress.text = mTokenList[position].contractAddress
                } else {
                    holder.mTvTokenContractAddress.visibility = View.GONE
                }
                holder.mSwtTokenSelect.setOnCheckedChangeListener(null)
                holder.mSwtTokenSelect.isChecked = mTokenList[position].selected

                holder.mSwtTokenSelect.setOnCheckedChangeListener { _, isChecked ->
                    mTokenList[position].selected = isChecked
                }
                holder.mRlRoot.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        if (!mTokenStatus) {
                            mItemTouchHelper.startDrag(holder)
                        }
                    }
                    false
                }
                if (mTokenStatus) {
                    holder.mSwtTokenSelect.visibility = View.VISIBLE
                    holder.mIvTokenDrag.visibility = View.GONE
                } else {
                    holder.mSwtTokenSelect.visibility = View.GONE
                    holder.mIvTokenDrag.visibility = View.VISIBLE
                }
                holder.itemView.tag = position
            }
        }

        override fun getItemCount(): Int {
            return mTokenList.size
        }

        fun exchange(position1: Int, position2: Int) {
            var item1 = mTokenList[position1]
            var item2 = mTokenList[position2]
            mTokenList.removeAt(position1)
            mTokenList.add(position1, item2)
            mTokenList.removeAt(position2)
            mTokenList.add(position2, item1)
            notifyItemMoved(position1, position2)
        }

        internal inner class TokenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var mIvTokenImage: ImageView = view.findViewById(R.id.token_image)
            var mTvTokenName: TextView = view.findViewById(R.id.token_name_text)
            var mTvTokenSymbol: TextView = view.findViewById(R.id.token_symbol_text)
            var mTvTokenContractAddress: TextView = view.findViewById(R.id.token_contract_address)
            var mSwtTokenSelect: Switch = view.findViewById(R.id.switch_token_select)
            var mIvTokenDrag: ImageView = view.findViewById(R.id.iv_drag)
            var mRlRoot: RelativeLayout = view.findViewById(R.id.rl_root)
        }
    }

    override fun finish() {
        var walletItem = DBWalletUtil.getCurrentWallet(mActivity)
        walletItem.tokenItems = mTokenList
        DBWalletUtil.saveWallet(mActivity, walletItem)
        EventBus.getDefault().post(AddTokenRefreshEvent())
        super.finish()
    }

    inner class SimpleItemTouchCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            val dragFlag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlag = 0
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlag, swipeFlag)
        }

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            mAdapter.exchange(viewHolder!!.adapterPosition, target!!.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        }

        override fun isLongPressDragEnabled(): Boolean {
            return !mTokenStatus
        }

    }

}
