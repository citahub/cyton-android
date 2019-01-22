package com.cryptape.cita_wallet.activity.transactionlist.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.item.transaction.BaseTransaction;
import com.cryptape.cita_wallet.item.transaction.RestTransaction;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;

import java.util.List;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_EMPTY = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private List<RestTransaction> restTransactionList;
    private String address;
    private Context context;

    private OnItemClickListener onItemClickListener;

    public TransactionAdapter(Context context, List<RestTransaction> restTransactionList, String address) {
        this.restTransactionList = restTransactionList;
        this.address = address;
        this.context = context;
    }

    public void refresh(List<RestTransaction> restTransactionList) {
        this.restTransactionList = restTransactionList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void addLoadingView() {
        if (restTransactionList.size() > 0) {
            restTransactionList.add(null);
            notifyItemInserted(restTransactionList.size() - 1);
        }
    }

    public void removeLoadingView() {
        if (restTransactionList.size() > 0) {
            restTransactionList.remove(restTransactionList.size() - 1);
            notifyItemRemoved(restTransactionList.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
            ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.empty_no_transaction_data);
            return new RecyclerView.ViewHolder(view) {};
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_loading, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        } else {
            return new TransactionViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionViewHolder) {
            RestTransaction restTransaction = restTransactionList.get(position);
            TransactionViewHolder viewHolder = (TransactionViewHolder) holder;
            viewHolder.transactionToAddressText.setText(ConstantUtil.RPC_RESULT_ZERO.equals(restTransaction.to)
                    || TextUtils.isEmpty(restTransaction.to)
                    ? context.getResources().getString(R.string.contract_create) : restTransaction.to);
            String value = (restTransaction.from.equalsIgnoreCase(address) ? "-" : "+") + restTransaction.value;
            viewHolder.transactionAmountText.setText(value);
            viewHolder.transactionTimeText.setText(restTransaction.getDate());
            switch (restTransaction.status) {
                case BaseTransaction.FAILED:
                    viewHolder.transactionStatus.setText(R.string.transaction_status_failed);
                    viewHolder.transactionStatus.setTextColor(context.getResources().getColor(R.color.red));
                    break;
                case BaseTransaction.SUCCESS:
                    viewHolder.transactionStatus.setText(R.string.transaction_status_success);
                    viewHolder.transactionStatus.setTextColor(context.getResources().getColor(R.color.assist_color));
                    break;
                case BaseTransaction.PENDING:
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
        if (restTransactionList.size() == 0) {
            return 1;
        }
        return restTransactionList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (restTransactionList.size() == 0) {
            return VIEW_TYPE_EMPTY;
        } else if (restTransactionList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_ITEM;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionToAddressText;
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
            transactionToAddressText = view.findViewById(R.id.transaction_to_address_text);
            transactionTimeText = view.findViewById(R.id.transaction_time_text);
            transactionAmountText = view.findViewById(R.id.transaction_amount);
            transactionStatus = view.findViewById(R.id.transaction_status);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
