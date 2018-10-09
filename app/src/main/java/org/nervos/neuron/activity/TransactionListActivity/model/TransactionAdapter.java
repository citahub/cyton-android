package org.nervos.neuron.activity.TransactionListActivity.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.Blockies;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;
    private List<TransactionItem> transactionItemList;
    private String address;

    public OnItemClickListener onItemClickListener;

    public TransactionAdapter(List<TransactionItem> transactionItemList, String address) {
        this.transactionItemList = transactionItemList;
        this.address = address;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void refresh(List<TransactionItem> transactionItemList) {
        this.transactionItemList = transactionItemList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
            ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.empty_no_transaction_data);
            return new RecyclerView.ViewHolder(view) {
            };
        }
        TransactionViewHolder holder = new TransactionViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.item_transaction_list, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionViewHolder) {
            TransactionItem transactionItem = transactionItemList.get(position);
            TransactionViewHolder viewHolder = (TransactionViewHolder) holder;
            viewHolder.walletImage.setImageBitmap(Blockies.createIcon(address));
            if (!transactionItem.from.equalsIgnoreCase(address)) {
                viewHolder.transactionIdText.setText(transactionItem.from);
                viewHolder.inOutImage.setImageResource(R.drawable.ic_trans_in);
            } else {
                viewHolder.transactionIdText.setText(transactionItem.to);
                viewHolder.inOutImage.setImageResource(R.drawable.ic_trans_in);
            }
            String value = (transactionItem.from.equalsIgnoreCase(address) ? "-" : "+")
                    + transactionItem.value;
            viewHolder.transactionAmountText.setText(value);
            viewHolder.transactionChainNameText.setText(transactionItem.chainName);
            viewHolder.transactionTimeText.setText(transactionItem.getDate());
            viewHolder.itemView.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        if (transactionItemList.size() == 0) {
            return 1;
        }
        return transactionItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (transactionItemList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        return VIEW_TYPE_ITEM;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView walletImage;
        ImageView inOutImage;
        TextView transactionIdText;
        TextView transactionAmountText;
        TextView transactionTimeText;
        TextView transactionChainNameText;

        public TransactionViewHolder(View view) {
            super(view);
            view.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (int) v.getTag());
                }
            });
            walletImage = view.findViewById(R.id.wallet_photo);
            inOutImage = view.findViewById(R.id.iv_in_out);
            transactionIdText = view.findViewById(R.id.transaction_id_text);
            transactionTimeText = view.findViewById(R.id.transaction_time_text);
            transactionAmountText = view.findViewById(R.id.transaction_amount);
            transactionChainNameText = view.findViewById(R.id.transaction_chain_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
