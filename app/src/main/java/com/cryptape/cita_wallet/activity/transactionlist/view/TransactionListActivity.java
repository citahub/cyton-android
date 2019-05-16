package com.cryptape.cita_wallet.activity.transactionlist.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.activity.NBaseActivity;
import com.cryptape.cita_wallet.activity.ReceiveQrCodeActivity;
import com.cryptape.cita_wallet.activity.TransactionDetailActivity;
import com.cryptape.cita_wallet.activity.transactionlist.model.TransactionAdapter;
import com.cryptape.cita_wallet.activity.transactionlist.presenter.TransactionListPresenter;
import com.cryptape.cita_wallet.activity.transfer.TransferActivity;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.event.TransferPushEvent;
import com.cryptape.cita_wallet.item.Token;
import com.cryptape.cita_wallet.item.Wallet;
import com.cryptape.cita_wallet.item.transaction.RestTransaction;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.view.TitleBar;
import com.cryptape.cita_wallet.view.TokenProfileView;
import com.cryptape.cita_wallet.view.loadmore.OnLoadMoreListener;
import com.cryptape.cita_wallet.view.loadmore.RecyclerViewLoadMoreScroll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionListActivity extends NBaseActivity {

    public static final String TRANSACTION_TOKEN = "TRANSACTION_TOKEN";
    public static final String TRANSACTION_STATUS = "TRANSACTION_STATUS";

    private List<RestTransaction> restTransactionList = new ArrayList<>();
    private Wallet wallet;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTokenWarning;
    private TitleBar titleBar;
    private AppCompatButton receiveButton, transferButton;
    private TokenProfileView mTokenProfile;

    private Token token;
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
        wallet = DBWalletUtil.getCurrentWallet(mActivity);
        token = getIntent().getParcelableExtra(TRANSACTION_TOKEN);
        titleBar.setTitle(token.symbol);
        tvTokenWarning.setVisibility(isTestToken() ? View.VISIBLE : View.GONE);

        presenter = new TransactionListPresenter(this, token, listener);
        mTokenProfile.init(token);
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
            intent.putExtra(TransferActivity.EXTRA_TOKEN, token);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            mPage = 0;
            presenter.getTransactionList(mPage);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransferPush(TransferPushEvent event) {
        mPage = 0;
        presenter.getTransactionList(mPage);
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        transactionAdapter = new TransactionAdapter(this, restTransactionList, wallet.address);
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
            RestTransaction response = restTransactionList.get(position);
            Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
            intent.putExtra(TransactionDetailActivity.TRANSACTION_DETAIL, response);
            intent.putExtra(TRANSACTION_TOKEN, token);
            intent.putExtra(TRANSACTION_STATUS, response.status);
            startActivity(intent);
        });
    }

    private void LoadMoreData() {
        transactionAdapter.addLoadingView();
        presenter.getTransactionList(mPage);
    }

    private TransactionListPresenter.TransactionListPresenterImpl listener = new TransactionListPresenter.TransactionListPresenterImpl() {
        @Override
        public void hideProgressBar() {
            swipeRefreshLayout.post(() -> dismissProgressBar());
        }

        @Override
        public void setRefreshing(boolean refreshing) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }

        @Override
        public void updateNewList(List<RestTransaction> list) {
            mPage++;
            restTransactionList = list;
            transactionAdapter.refresh(restTransactionList);
        }

        @Override
        public void refreshList(List<RestTransaction> list) {
            mPage++;
            transactionAdapter.removeLoadingView();
            restTransactionList.addAll(list);
            Collections.sort(restTransactionList, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
            transactionAdapter.refresh(restTransactionList);
            scrollListener.setLoaded();
        }

        @Override
        public void noMoreLoading() {
            transactionAdapter.removeLoadingView();
            scrollListener.setLoaded();
            if (restTransactionList.size() > 0) {
                Toast.makeText(mActivity, R.string.no_more_transaction_data, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean isTestToken() {
        return ConstantUtil.DEFAULT_TOKEN_NAME.equalsIgnoreCase(token.symbol) || ConstantUtil.MBA_TOKEN_SYMBOL.equalsIgnoreCase(token.symbol);
    }

}
