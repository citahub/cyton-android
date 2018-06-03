package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.snappydb.DB;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TransactionDetailActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionFragment extends Fragment {

    public static final String TAG = TransactionFragment.class.getName();

    private List<TransactionItem> transactionItemList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TransactionAdapter adapter = new TransactionAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        recyclerView = view.findViewById(R.id.transaction_recycler);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletItem = DBWalletUtil.getCurrentWallet(getContext());
        initData();
        initAdapter();
        initRefreshData();
    }

    private void initData() {
        transactionItemList.add(new TransactionItem("1", "0x123455667",
                "0x12343554", "100", "2018/3/12 12:30", "Ethereum Mainnet"));
        transactionItemList.add(new TransactionItem("1", "0x123455667",
                "0x12343554", "100", "2018/3/12 12:30", "Ethereum Mainnet"));
        transactionItemList.add(new TransactionItem("1", "0x123455667",
                "0x12343554", "100", "2018/3/12 12:30", "Ethereum Mainnet"));
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), TransactionDetailActivity.class);
                startActivity(intent);
            }
        });
    }


    private void initRefreshData() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                    }
                },500);
            }
        });
    }

    class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_EMPTY = 0;

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_empty_view, parent, false);
                return new RecyclerView.ViewHolder(view){};
            }
            TransactionViewHolder holder = new TransactionViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.item_transaction_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TransactionViewHolder) {
                TransactionViewHolder viewHolder = (TransactionViewHolder)holder;
                viewHolder.walletImage.setImageBitmap(Blockies.createIcon(walletItem.address));;
                viewHolder.transactionIdText.setText(transactionItemList.get(position).id);
                viewHolder.transactionAmountText.setText(transactionItemList.get(position).value);
                viewHolder.transactionChainNameText.setText(transactionItemList.get(position).chainName);
                viewHolder.transactionTimeText.setText(transactionItemList.get(position).date);
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

        class  TransactionViewHolder extends RecyclerView.ViewHolder {
            CircleImageView walletImage;
            TextView transactionIdText;
            TextView transactionAmountText;
            TextView transactionTimeText;
            TextView transactionChainNameText;

            public TransactionViewHolder (View view) {
                super(view);
                view.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, (int)v.getTag());
                    }
                });
                walletImage = view.findViewById(R.id.wallet_photo);
                transactionIdText = view.findViewById(R.id.transaction_id_text);
                transactionTimeText = view.findViewById(R.id.transaction_time_text);
                transactionAmountText = view.findViewById(R.id.transaction_amount);
                transactionChainNameText = view.findViewById(R.id.transaction_chain_name);
            }
        }
    }

    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
