package org.nervos.neuron.activity.transactionlist.view;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.NBaseActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.activity.TransactionDetailActivity;
import org.nervos.neuron.activity.transfer.TransferActivity;
import org.nervos.neuron.activity.transactionlist.model.TransactionAdapter;
import org.nervos.neuron.activity.transactionlist.presenter.TransactionListPresenter;
import org.nervos.neuron.item.EthErc20TokenInfoItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.url.HttpUrls;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.view.loadmore.OnLoadMoreListener;
import org.nervos.neuron.view.loadmore.RecyclerViewLoadMoreScroll;
import org.web3j.crypto.Keys;

import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends NBaseActivity {

    public static final String TRANSACTION_TOKEN = "TRANSACTION_TOKEN";

    private List<TransactionItem> transactionItemList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerViewLoadMoreScroll scrollListener;

    private TitleBar titleBar;
    private AppCompatButton receiveButton, transferButton;
    private TokenItem tokenItem;

    private ImageView tokenLogoImage;
    private TextView tokenDesTextFirst, tokenDesTextSecond, tokenBalanceText, tokenSymbol, tvTokenWarning;
    private ConstraintLayout tokenDesRoot;
    private TransactionAdapter transactionAdapter;

    private TransactionListPresenter presenter;

    private String describe;
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
        tokenLogoImage = findViewById(R.id.iv_token_logo);
        tokenDesTextFirst = findViewById(R.id.tv_token_des_first);
        tokenDesTextSecond = findViewById(R.id.tv_token_des_second);
        tokenBalanceText = findViewById(R.id.tv_balance);
        tokenDesRoot = findViewById(R.id.cl_token_des);
        tokenSymbol = findViewById(R.id.tv_token_symbol);
        tvTokenWarning = findViewById(R.id.tv_token_warning);
    }

    @Override
    protected void initData() {
        walletItem = DBWalletUtil.getCurrentWallet(mActivity);
        tokenItem = getIntent().getParcelableExtra(TRANSACTION_TOKEN);
        titleBar.setTitle(tokenItem.symbol);
        tvTokenWarning.setVisibility(isTestToken() ? View.VISIBLE : View.GONE);

        presenter = new TransactionListPresenter(this, tokenItem, listener);
        initDescribe();
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
        mPage = 0;
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.getTransactionList(mPage));
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        transactionAdapter = new TransactionAdapter(this, transactionItemList, walletItem.address);
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
            TransactionItem item = transactionItemList.get(position);
            if (item.status != TransactionItem.PENDING) {
                Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
                intent.putExtra(TransactionDetailActivity.Companion.getTRANSACTION_DETAIL(), item);
                intent.putExtra(TRANSACTION_TOKEN, tokenItem);
                startActivity(intent);
            }
        });
    }

    private void LoadMoreData() {
        transactionAdapter.addLoadingView();
        presenter.getTransactionList(mPage);
    }

    private void initDescribe() {
        if (presenter.isEther(tokenItem)) {
            if (!presenter.isNativeToken(tokenItem)) {
                presenter.getTokenDescribe();
            } else {
                tokenDesRoot.setVisibility(View.VISIBLE);
                describe = getResources().getString(R.string.ETH_Describe);
                tokenDesTextFirst.setText(R.string.ETH_Describe);
                tokenSymbol.setText(ConstantUtil.ETH);
                setDesSecondLine();
                presenter.getBalance();
                tokenDesRoot.setOnClickListener(view ->
                        SimpleWebActivity.gotoSimpleWeb(mActivity, String.format(HttpUrls.TOKEN_DETAIL, ConstantUtil.ETHEREUM)));
            }
        }
    }

    private void initBalance() {
        if (tokenItem.balance != 0.0 && presenter.isEther(tokenItem)) {
            presenter.getBalance();
        }
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
        public void refreshList(List<TransactionItem> list) {
            mPage++;
            transactionAdapter.removeLoadingView();
            transactionItemList.addAll(list);
            transactionAdapter.refresh(transactionItemList);
            scrollListener.setLoaded();
        }

        @Override
        public void getTokenDescribe(EthErc20TokenInfoItem item) {
            describe = item.overView.zh;
            tokenDesTextFirst.post(() -> {
                tokenSymbol.setText(item.symbol);
                tokenDesTextFirst.setText(item.overView.zh);
                setDesSecondLine();
            });
        }

        @Override
        public void showTokenDescribe(boolean show) {
            swipeRefreshLayout.post(() -> {
                if (show) {
                    tokenDesRoot.setVisibility(View.VISIBLE);
                    presenter.setTokenLogo(tokenLogoImage);
                    initBalance();
                    String address = tokenItem.contractAddress;
                    if (AddressUtil.isAddressValid(address))
                        address = Keys.toChecksumAddress(address);
                    String finalAddress = address;
                    tokenDesRoot.setOnClickListener(view ->
                            SimpleWebActivity.gotoSimpleWeb(mActivity, String.format(HttpUrls.TOKEN_ERC20_DETAIL, finalAddress)));
                } else {
                    tokenDesRoot.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void getCurrency(String currency) {
            tokenBalanceText.post(() -> tokenBalanceText.setText(currency));
        }

        @Override
        public void noMoreLoading() {
            transactionAdapter.removeLoadingView();
            scrollListener.setLoaded();
            if (transactionItemList.size() > 0) {
                Toast.makeText(mActivity, R.string.no_more_transaction_data, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setDesSecondLine() {
        tokenDesTextFirst.postDelayed(() -> {
            Layout layout = tokenDesTextFirst.getLayout();
            String mDesText = tokenDesTextFirst.getLayout().getText().toString();
            StringBuilder srcStr = new StringBuilder(mDesText);
            String lineStr = srcStr.subSequence(layout.getLineStart(0), layout.getLineEnd(0)).toString();
            int length = lineStr.length();
            if (length > 0 && describe.length() > length
                    && tokenDesTextSecond.getText().toString().length() == 0) {
                String secondText;
                if ((int) (length * 1.5) > describe.length()) {
                    secondText = describe.substring(length);
                } else {
                    secondText = describe.substring(length, (int) (length * 1.5)) + "...";
                }
                tokenDesTextSecond.setText(secondText);
            }
        }, 300);
    }

    private boolean isTestToken() {
        return "NATT".equalsIgnoreCase(tokenItem.symbol) || "MBA".equalsIgnoreCase(tokenItem.symbol);
    }

}
