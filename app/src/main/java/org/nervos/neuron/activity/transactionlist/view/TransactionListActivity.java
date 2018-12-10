package org.nervos.neuron.activity.transactionlist.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.NBaseActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TransactionDetailActivity;
import org.nervos.neuron.activity.transactionlist.model.TransactionAdapter;
import org.nervos.neuron.activity.transactionlist.presenter.TransactionListPresenter;
import org.nervos.neuron.activity.transfer.TransferActivity;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.item.transaction.TransactionResponse;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.TokenProfileView;
import org.nervos.neuron.view.loadmore.OnLoadMoreListener;
import org.nervos.neuron.view.loadmore.RecyclerViewLoadMoreScroll;

import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends NBaseActivity {

    public static final String TRANSACTION_TOKEN = "TRANSACTION_TOKEN";
    public static final String TRANSACTION_STATUS = "TRANSACTION_STATUS";

    private List<TransactionResponse> transactionResponseList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTokenWarning;
    private TitleBar titleBar;
    private AppCompatButton receiveButton, transferButton;
    private TokenProfileView mTokenProfile;

    private TokenItem tokenItem;
    private TransactionAdapter transactionAdapter;
    private RecyclerViewLoadMoreScroll scrollListener;
    private TransactionListPresenter presenter;

    private int mPage = 0;

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
        tvTokenWarning = findViewById(R.id.tv_token_warning);
        mTokenProfile = findViewById(R.id.view_token_profile);
    }

    @Override
    protected void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        tokenItem = getIntent().getParcelableExtra(TRANSACTION_TOKEN);
        titleBar.setTitle(tokenItem.symbol);
        tvTokenWarning.setVisibility(isTestToken() ? View.VISIBLE : View.GONE);

        presenter = new TransactionListPresenter(this, tokenItem, listener);
        mTokenProfile.init(tokenItem);
        initTransactionData();
    }

    private void initTransactionData() {
        initAdapter();
        showProgressBar();
        presenter.getTransactionList(mPage);
    }

    @Override
    protected void initAction() {
        receiveButton.setOnClickListener(v -> startActivity(new Intent(mActivity, ReceiveQrCodeActivity.class)));
        transferButton.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, TransferActivity.class);
            intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            mPage = 0;
            presenter.getTransactionList(mPage);
        });
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        transactionAdapter = new TransactionAdapter(this, transactionResponseList, walletItem.address);
        recyclerView.setAdapter(transactionAdapter);

        scrollListener = new RecyclerViewLoadMoreScroll(linearLayoutManager);
        scrollListener.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                LoadMoreData();
            }
        });

        recyclerView.addOnScrollListener(scrollListener);

        transactionAdapter.setOnItemClickListener((view, position) -> {
            TransactionResponse response = transactionResponseList.get(position);
            Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
            intent.putExtra(TransactionDetailActivity.TRANSACTION_DETAIL, response);
            intent.putExtra(TRANSACTION_TOKEN, tokenItem);
            intent.putExtra(TRANSACTION_STATUS, response.status);
            startActivity(intent);
        });
    }

    private void LoadMoreData() {
        transactionAdapter.addLoadingView();
        presenter.getTransactionList(mPage);
    }

    private TransactionListPresenter.TransactionListPresenterImpl listener
            = new TransactionListPresenter.TransactionListPresenterImpl() {
        @Override
        public void hideProgressBar() {
            swipeRefreshLayout.post(() -> dismissProgressBar());
        }

        @Override
        public void setRefreshing(boolean refreshing) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }

        @Override
        public void updateNewList(List<TransactionResponse> list) {
            mPage++;
            transactionResponseList = list;
            transactionAdapter.refresh(transactionResponseList);
        }

        @Override
        public void refreshList(List<TransactionResponse> list) {
            mPage++;
            transactionAdapter.removeLoadingView();
            transactionResponseList.addAll(list);
            transactionAdapter.refresh(transactionResponseList);
            scrollListener.setLoaded();
        }

        @Override
        public void noMoreLoading() {
            transactionAdapter.removeLoadingView();
            scrollListener.setLoaded();
            if (transactionResponseList.size() > 0) {
                Toast.makeText(mActivity, R.string.no_more_transaction_data, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean isTestToken() {
        return "NATT".equalsIgnoreCase(tokenItem.symbol) || "MBA".equalsIgnoreCase(tokenItem.symbol);
    }

}
