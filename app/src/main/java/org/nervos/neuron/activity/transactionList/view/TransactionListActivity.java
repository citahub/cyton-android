package org.nervos.neuron.activity.transactionList.view;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.NBaseActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.activity.TransactionDetailActivity;
import org.nervos.neuron.activity.transactionList.model.TransactionAdapter;
import org.nervos.neuron.activity.transactionList.presenter.TransactionListPresenter;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.item.EthErc20TokenInfoItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.HttpUrls;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.TitleBar;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.nervos.neuron.activity.TransactionDetailActivity.EXTRA_TRANSACTION;

public class TransactionListActivity extends NBaseActivity {

    public static final String EXTRA_TOKEN = "extra_token";

    private List<TransactionItem> transactionItemList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TitleBar titleBar;

    private AppCompatButton receiveButton, transferButton;
    private TokenItem tokenItem;

    private ImageView tokenLogoImage;
    private TextView tokenDesText, tokenBalanceText;
    private ConstraintLayout tokenDesRoot;
    private TransactionAdapter transactionAdapter;

    private TransactionListPresenter presenter;

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
        tokenLogoImage = findViewById(R.id.iv_token_logo);
        tokenDesText = findViewById(R.id.tv_token_des);
        tokenBalanceText = findViewById(R.id.tv_balance);
        tokenDesRoot = findViewById(R.id.cl_token_des);
    }

    @Override
    protected void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        tokenItem = getIntent().getParcelableExtra(EXTRA_TOKEN);
        titleBar.setTitle(tokenItem.symbol);
        showProgressBar();
        presenter = new TransactionListPresenter(this, tokenItem, listener);
        initAdapter();
        presenter.getTransactionList();
        initDescribe();
        DecimalFormat formater = new DecimalFormat("0.####");
        formater.setRoundingMode(RoundingMode.FLOOR);
        tokenBalanceText.setText(formater.format(tokenItem.balance) + tokenItem.symbol);
    }

    @Override
    protected void initAction() {
        receiveButton.setOnClickListener(v -> startActivity(new Intent(mActivity, ReceiveQrCodeActivity.class)));
        transferButton.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, TransferActivity.class);
            intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.getTransactionList());
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        transactionAdapter = new TransactionAdapter(this, transactionItemList, walletItem.address, tokenItem.symbol);
        recyclerView.setAdapter(transactionAdapter);

        transactionAdapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
            intent.putExtra(EXTRA_TRANSACTION, transactionItemList.get(position));
            startActivity(intent);
        });
    }

    private void initDescribe() {
        if (presenter.isEthereum(tokenItem)) {
            if (!presenter.isNativeToken(tokenItem)) {
                presenter.getTokenDescribe();
            } else {
                tokenDesRoot.setVisibility(View.VISIBLE);
                tokenDesText.setText(R.string.ETH_Describe);
                presenter.getBalance();
            }
        }
    }

    private void initBalance() {
        if (tokenItem.balance != 0.0 && presenter.isEthereum(tokenItem)) {
            presenter.getBalance();
        }
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
        public void refreshList(List<TransactionItem> list) {
            transactionItemList = list;
            transactionAdapter.refresh(transactionItemList);
        }

        @Override
        public void getTokenDescribe(EthErc20TokenInfoItem item) {
            tokenDesText.post(() -> tokenDesText.setText(item.overView.zh));
        }

        @Override
        public void showTokenDescribe(boolean show) {
            swipeRefreshLayout.post(() -> {
                if (show) {
                    tokenDesRoot.setVisibility(View.VISIBLE);
                    presenter.setTokenLogo(tokenLogoImage);
                    initBalance();
                    tokenDesRoot.setOnClickListener(view -> SimpleWebActivity.gotoSimpleWeb(mActivity, HttpUrls.TOKEN_DETAIL.replace("@address", tokenItem.contractAddress)));
                } else
                    tokenDesRoot.setVisibility(View.GONE);
            });
        }

        @Override
        public void getCurrency(String currency) {
            tokenBalanceText.post(() -> tokenBalanceText.setText(currency));
        }
    };

}
