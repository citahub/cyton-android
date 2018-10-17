package org.nervos.neuron.fragment.token.model;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.nervos.neuron.R;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.util.NumberUtil;

import java.util.List;

public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.TokenViewHolder> {

    public TokenAdapterListener listener;
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

    public void setTokenAdapterListener(TokenAdapterListener listener) {
        this.listener = listener;
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
                if (!TextUtils.isEmpty(tokenItem.contractAddress)) {
                    RequestOptions options = new RequestOptions()
                            .error(R.drawable.ether_big);
                    Glide.with(activity)
                            .load(Uri.parse(HttpUrls.TOKEN_LOGO.replace("@address", tokenItem.contractAddress)))
                            .apply(options)
                            .into(holder.tokenImage);
                } else {
                    Glide.with(activity)
                            .load(R.drawable.ether_big)
                            .into(holder.tokenImage);
                }
            } else {
                Glide.with(activity)
                        .load(R.mipmap.ic_launcher)
                        .into(holder.tokenImage);
            }
        } else {
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.ether_big);
            Glide.with(activity)
                    .load(Uri.parse(tokenItem.avatar))
                    .apply(options)
                    .into(holder.tokenImage);
        }
        if (tokenItem != null) {
            holder.tokenName.setText(tokenItem.symbol);
            holder.tokenBalance.setText(NumberUtil.getDecimal8ENotation(tokenItem.balance));
        }
        if (!TextUtils.isEmpty(tokenItem.chainName)) {
            holder.tokenNetworkText.setText(tokenItem.chainName);
        } else {
            if (tokenItem.chainId < 0) {
                holder.tokenNetworkText.setText(activity.getString(R.string.ethereum_mainnet));
            }
        }
        if (tokenItem.currencyPrice == 0.00) {
            holder.tokenCurrencyText.setText("");
        } else {
            holder.tokenCurrencyText.setText(listener.getCurrency().getSymbol() +
                    NumberUtil.getDecimalValid_2(tokenItem.currencyPrice));
        }
        holder.root.setOnClickListener((view) -> {
            listener.onItemClick(view, position);
        });
    }

    @Override
    public int getItemCount() {
        return tokenItemList.size();
    }

    class TokenViewHolder extends RecyclerView.ViewHolder {
        ImageView tokenImage;
        TextView tokenName;
        TextView tokenBalance;
        TextView tokenNetworkText;
        TextView tokenCurrencyText;
        RelativeLayout root;

        public TokenViewHolder(View view) {
            super(view);
            tokenImage = view.findViewById(R.id.token_image);
            tokenName = view.findViewById(R.id.token_name);
            tokenBalance = view.findViewById(R.id.token_balance);
            tokenNetworkText = view.findViewById(R.id.token_network);
            tokenCurrencyText = view.findViewById(R.id.token_currency);
            root = view.findViewById(R.id.root);
        }
    }

    public interface TokenAdapterListener {
        void onItemClick(View view, int position);

        CurrencyItem getCurrency();
    }

}