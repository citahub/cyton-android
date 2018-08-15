package org.nervos.neuron.fragment.TokenListFragment.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.NumberUtil;

import java.util.List;

public class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public TokenAdapterListener listener;
    private Activity activity;
    private List<TokenItem> tokenItemList;
    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
            ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.empty_no_transaction_data);
            return new RecyclerView.ViewHolder(view) {
            };
        }
        TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_token_list, parent,
                false));
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        if (tokenItemList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof TokenViewHolder) {
            TokenViewHolder holder = (TokenViewHolder) viewHolder;
            TokenItem tokenItem = tokenItemList.get(position);
            Uri uri = null;
            if (TextUtils.isEmpty(tokenItem.avatar)) {
                if (tokenItem.chainId < 0) {
                    holder.tokenImage.setImageResource(R.drawable.ether_big);
                } else {
                    holder.tokenImage.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                uri = Uri.parse(tokenItem.avatar);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
                holder.tokenImage.setController(controller);
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
    }

    @Override
    public int getItemCount() {
        if (tokenItemList.size() == 0) {
            return 1;
        }
        return tokenItemList.size();
    }

    class TokenViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView tokenImage;
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