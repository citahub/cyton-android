package org.nervos.neuron.activity.transactionList.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.item.TransactionItem;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_EMPTY = 0;
    private List<TransactionItem> transactionItemList;
    private String address;
    private Context context;

    private OnItemClickListener onItemClickListener;

    public TransactionAdapter(Context context, List<TransactionItem> transactionItemList, String address) {
        this.transactionItemList = transactionItemList;
        this.address = address;
        this.context = context;
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
            if (!transactionItem.from.equalsIgnoreCase(address)) {
                viewHolder.transactionIdText.setText(transactionItem.from);
            } else {
                viewHolder.transactionIdText.setText(transactionItem.to);
            }
            String value = (transactionItem.from.equalsIgnoreCase(address) ? "-" : "+")
                    + transactionItem.value;
            viewHolder.transactionAmountText.setText(value);
            viewHolder.transactionTimeText.setText(transactionItem.getDate());
            switch (transactionItem.status) {
                case 0:
                    viewHolder.transactionStatus.setText(R.string.transaction_status_failed);
                    viewHolder.transactionStatus.setTextColor(context.getResources().getColor(R.color.red));
                    break;
                case 1:
                    viewHolder.transactionStatus.setText(R.string.transaction_status_success);
                    viewHolder.transactionStatus.setTextColor(context.getResources().getColor(R.color.font_title_third));
                    break;
                case 2:
                default:
                    viewHolder.transactionStatus.setText(R.string.transaction_status_pending);
                    viewHolder.transactionStatus.setTextColor(context.getResources().getColor(R.color.font_title_third));
                    break;
            }
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
        TextView transactionIdText;
        TextView transactionAmountText;
        TextView transactionTimeText;
        TextView transactionStatus;

        public TransactionViewHolder(View view) {
            super(view);
            view.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, (int) v.getTag());
                }
            });
            transactionIdText = view.findViewById(R.id.transaction_id_text);
            transactionTimeText = view.findViewById(R.id.transaction_time_text);
            transactionAmountText = view.findViewById(R.id.transaction_amount);
            transactionStatus = view.findViewById(R.id.transaction_status);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
