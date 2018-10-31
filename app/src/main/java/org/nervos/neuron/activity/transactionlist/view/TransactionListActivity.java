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

import org.nervos.neuron.R;
import org.nervos.neuron.activity.NBaseActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.SimpleWebActivity;
import org.nervos.neuron.activity.TransactionDetailActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.activity.transactionlist.model.TransactionAdapter;
import org.nervos.neuron.activity.transactionlist.presenter.TransactionListPresenter;
import org.nervos.neuron.item.EthErc20TokenInfoItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.httpservice.HttpUrls;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.util.ConstUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.view.TitleBar;
import org.web3j.crypto.Keys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends NBaseActivity {

    public static final String TRANSACTION_TOKEN = "TRANSACTION_TOKEN";

    private List<TransactionItem> transactionItemList = new ArrayList<>();
    private WalletItem walletItem;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TitleBar titleBar;

    private AppCompatButton receiveButton, transferButton;
    private TokenItem tokenItem;

    private ImageView tokenLogoImage;
    private TextView tokenDesTextFirst, tokenDesTextSecond, tokenBalanceText, tokenSymbol, tvTokenWarning;
    private ConstraintLayout tokenDesRoot;
    private TransactionAdapter transactionAdapter;

    private TransactionListPresenter presenter;

    private String describe;

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
        showProgressBar();
        presenter = new TransactionListPresenter(this, tokenItem, listener);
        initAdapter();
        presenter.getTransactionList(walletItem.address);
        initDescribe();
        DecimalFormat formater = new DecimalFormat("0.####");
        formater.setRoundingMode(RoundingMode.FLOOR);

        tvTokenWarning.setVisibility(isTestToken()? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initAction() {
        receiveButton.setOnClickListener(v -> startActivity(new Intent(mActivity
                , ReceiveQrCodeActivity.class)));
        transferButton.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, TransferActivity.class);
            intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItem);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(() ->
                presenter.getTransactionList(walletItem.address));
    }

    private void initAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        transactionAdapter = new TransactionAdapter(this, transactionItemList
                , walletItem.address);
        recyclerView.setAdapter(transactionAdapter);

        transactionAdapter.setOnItemClickListener((view, position) -> {
            TransactionItem item = transactionItemList.get(position);
            if (item.status != 2) {
                Intent intent = new Intent(mActivity, TransactionDetailActivity.class);
                intent.putExtra(TransactionDetailActivity.TRANSACTION_DETAIL, item);
                intent.putExtra(TRANSACTION_TOKEN, tokenItem);
                startActivity(intent);
            }
        });
    }

    private void initDescribe() {
        if (presenter.isEthereum(tokenItem)) {
            if (!presenter.isNativeToken(tokenItem)) {
                presenter.getTokenDescribe();
            } else {
                tokenDesRoot.setVisibility(View.VISIBLE);
                describe = getResources().getString(R.string.ETH_Describe);
                tokenDesTextFirst.setText(R.string.ETH_Describe);
                tokenSymbol.setText(ConstUtil.ETH);
                setDesSecondLine();
                presenter.getBalance();
                tokenDesRoot.setOnClickListener(view -> SimpleWebActivity.gotoSimpleWeb(mActivity
                        , HttpUrls.TOKEN_DETAIL.replace("@address", "ethereum")));
            }
        }
    }

    private void initBalance() {
        if (tokenItem.balance != 0.0 && presenter.isEthereum(tokenItem)) {
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
            transactionItemList = list;
            transactionAdapter.refresh(transactionItemList);
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
                            SimpleWebActivity.gotoSimpleWeb(mActivity
                                    , HttpUrls.TOKEN_ERC20_DETAIL.replace("@address"
                                            , finalAddress)));
                } else
                    tokenDesRoot.setVisibility(View.GONE);
            });
        }

        @Override
        public void getCurrency(String currency) {
            tokenBalanceText.post(() -> tokenBalanceText.setText(currency));
        }
    };

    private void setDesSecondLine() {
        tokenDesTextFirst.postDelayed(() -> {
            Layout layout = tokenDesTextFirst.getLayout();
            String mDesText = tokenDesTextFirst.getLayout().getText().toString();
            StringBuilder srcStr = new StringBuilder(mDesText);
            String lineStr = srcStr.subSequence(layout.getLineStart(0)
                    , layout.getLineEnd(0)).toString();
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
