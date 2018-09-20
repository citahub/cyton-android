package org.nervos.neuron.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.NervosHttpService;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

import static org.nervos.neuron.activity.TransactionDetailActivity.EXTRA_TRANSACTION;

public class TransactionListActivity extends NBaseActivity {

    public static final String EXTRA_TOKEN = "extra_token";

    private List<TransactionItem> transactionItemList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TransactionAdapter adapter = new TransactionAdapter();

    private TitleBar titleBar;

    private AppCompatButton receiveButton, transferButton;
    private TokenItem tokenItem;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_transaction_list;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.transaction_recycler);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        receiveButton = findViewById(R.id.receive_token);
        transferButton = findViewById(R.id.transfer_token);
        titleBar = findViewById(R.id.title);
    }

    @Override
    protected void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);
        titleBar.setTitle(tokenItem.symbol);
        initAdapter();
        showProgressBar();
        getTransactionList();
        initRefreshData();
    }

    @Override
    protected void initAction() {
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, ReceiveQrCodeActivity.class));
            }
        });
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, TransferActivity.class);
                intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem);
                startActivity(intent);
            }
        });
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
                intent.putExtra(EXTRA_TRANSACTION, transactionItemList.get(position));
                startActivity(intent);
            }
        });
    }


    private void initRefreshData() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactionList();
            }
        });
    }


    private void getTransactionList() {
        if (!isNativeToken(tokenItem)) {
            dismissProgressBar();
            return;
        }
        Observable<List<TransactionItem>> observable = isETH(tokenItem) ?
                NervosHttpService.getETHTransactionList(mActivity)
                : NervosHttpService.getNervosTransactionList(mActivity);
        observable.subscribe(new Subscriber<List<TransactionItem>>() {
            @Override
            public void onCompleted() {
                dismissProgressBar();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                dismissProgressBar();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onNext(List<TransactionItem> list) {
                if (list == null) {
                    Toast.makeText(mActivity, R.string.network_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.sort(list, new Comparator<TransactionItem>() {
                    @Override
                    public int compare(TransactionItem item1, TransactionItem item2) {
                        return item2.getDate().compareTo(item1.getDate());
                    }
                });
                transactionItemList = list;
                adapter.notifyDataSetChanged();
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_empty_view, parent, false);
                ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.empty_no_transaction_data);
                return new RecyclerView.ViewHolder(view) {
                };
            }
            TransactionViewHolder holder = new TransactionViewHolder(LayoutInflater.from(
                    mActivity).inflate(R.layout.item_transaction_list, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TransactionViewHolder) {
                TransactionItem transactionItem = transactionItemList.get(position);
                TransactionViewHolder viewHolder = (TransactionViewHolder) holder;
                viewHolder.walletImage.setImageBitmap(Blockies.createIcon(walletItem.address));
                if (!transactionItem.from.equalsIgnoreCase(walletItem.address)) {
                    viewHolder.transactionIdText.setText(transactionItem.from);
                    viewHolder.inOutImage.setImageResource(R.drawable.ic_trans_in);
                } else {
                    viewHolder.transactionIdText.setText(transactionItem.to);
                    viewHolder.inOutImage.setImageResource(R.drawable.ic_trans_in);
                }
                String value = (transactionItem.from.equalsIgnoreCase(walletItem.address) ? "-" : "+")
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
    }

    private interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private boolean isNativeToken(TokenItem tokenItem) {
        return TextUtils.isEmpty(tokenItem.contractAddress);
    }

    private boolean isETH(TokenItem tokenItem) {
        return ConstUtil.ETH.equals(tokenItem.symbol);
    }
}
