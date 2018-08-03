package org.nervos.neuron.fragment.TokenListFragment.model;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.NumberUtil;

import java.util.List;

public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.TokenViewHolder> {

    public OnItemClickListener onItemClickListener;
    private Activity activity;
    private List<TokenItem> tokenItemList;

    public TokenAdapter(Activity activity, List<TokenItem> tokenItemList) {
        this.activity = activity;
        this.tokenItemList = tokenItemList;
    }

    public void refresh(List<TokenItem> tokenItemList) {
        this.tokenItemList = tokenItemList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public TokenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_token_list, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TokenViewHolder holder, int position) {
        TokenItem tokenItem = tokenItemList.get(position);
        if (TextUtils.isEmpty(tokenItem.avatar)) {
            if (tokenItem.chainId < 0) {
                Glide.with(activity)
                        .load(R.drawable.ether_small)
                        .into(holder.tokenImage);
            } else {
                Glide.with(activity)
                        .load(R.mipmap.ic_launcher)
                        .into(holder.tokenImage);
            }
        } else {
            Glide.with(activity)
                    .load(tokenItem.avatar)
                    .into(holder.tokenImage);
        }
        if (tokenItem != null) {
            holder.tokenName.setText(tokenItem.symbol);
            holder.tokenAmount.setText(NumberUtil.getDecimal_6(tokenItem.balance));
        }
        if (!TextUtils.isEmpty(tokenItem.chainName)) {
            holder.tokenNetworkText.setText(tokenItem.chainName);
        } else {
            if (tokenItem.chainId < 0) {
                holder.tokenNetworkText.setText(activity.getString(R.string.ethereum_mainnet));
            } else {
            }
        }
    }

    @Override
    public int getItemCount() {
        return tokenItemList.size();
    }

    class TokenViewHolder extends RecyclerView.ViewHolder {
        ImageView tokenImage;
        TextView tokenName;
        TextView tokenAmount;
        TextView tokenNetworkText;
        TextView tokenCurrencyText;

        public TokenViewHolder(View view) {
            super(view);
            view.setOnClickListener((v) -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (int) v.getTag());
                }
            });
            tokenImage = view.findViewById(R.id.token_image);
            tokenName = view.findViewById(R.id.token_name);
            tokenAmount = view.findViewById(R.id.token_amount);
            tokenNetworkText = view.findViewById(R.id.token_network);
            tokenCurrencyText = view.findViewById(R.id.token_currency);
        }
    }

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}