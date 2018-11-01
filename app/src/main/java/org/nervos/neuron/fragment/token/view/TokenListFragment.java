package org.nervos.neuron.fragment.token.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.activity.transactionList.view.TransactionListActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.token.model.TokenAdapter;
import org.nervos.neuron.fragment.token.presenter.TokenListFragmentPresenter;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.TokenService;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.CurrencyUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

/**
 * Created by BaojunCZ on 2018/8/2.
 */
public class TokenListFragment extends NBaseFragment {

    private RecyclerView recyclerView;
    private TokenListFragmentPresenter presenter;
    private List<TokenItem> tokenItemList = new ArrayList<>();
    private TokenAdapter adapter;
    private LinearLayout noTokenRoot;
    private TextView totalText, moneyText;
    private RelativeLayout addImageRl;
    private CurrencyItem currencyItem;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_token_list;
    }

    @Override
    protected void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_token);
        noTokenRoot = (LinearLayout) findViewById(R.id.ll_no_token);
        totalText = (TextView) findViewById(R.id.tv_total_money_title);
        moneyText = (TextView) findViewById(R.id.tv_total_money);
        addImageRl = (RelativeLayout) findViewById(R.id.rl_add_image);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    @Override
    protected void initData() {
        presenter = new TokenListFragmentPresenter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TokenAdapter(getActivity(), tokenItemList);
        recyclerView.setAdapter(adapter);
        initWalletData(true);
        setCurrency();
        initRefresh();
    }

    @Override
    protected void initAction() {
        addImageRl.setOnClickListener((view) -> {
            startActivity(new Intent(getActivity(), TokenManageActivity.class));
        });
        adapter.setTokenAdapterListener(new TokenAdapter.TokenAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(), TransactionListActivity.class);
                intent.putExtra(TransactionListActivity.TRANSACTION_TOKEN, tokenItemList.get(position));
                startActivity(intent);
            }

            @Override
            public CurrencyItem getCurrency() {
                return currencyItem;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        setCurrency();
    }

    private void setCurrency() {
        if (currencyItem == null || !currencyItem.getName().equals(CurrencyUtil.getCurrencyItem(getContext()).getName())) {
            currencyItem = CurrencyUtil.getCurrencyItem(getContext());
            totalText.setText(getResources().getString(R.string.wallet_total_money) + "(" + currencyItem.getUnit() + ")");
            getPrice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData(true);
        adapter.refresh(tokenItemList);
    }

    private void initRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> initWalletData(false));
    }

    private void initWalletData(boolean showProgress) {
        moneyText.setText("0");
        if (showProgress) showProgressBar();
        WalletService.getWalletTokenBalance(getContext(), new WalletService.OnGetWalletTokenListener() {
            @Override
            public void onGetWalletToken(WalletItem walletItem) {
                recyclerView.post(() -> {
                    if (showProgress) dismissProgressBar();
                    swipeRefreshLayout.setRefreshing(false);
                    if (walletItem.tokenItems != null) {
                        tokenItemList = walletItem.tokenItems;
                        setData();
                    }
                });
            }

            @Override
            public void onGetWalletError(String message) {
                recyclerView.post(() -> {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void setData() {
        if (tokenItemList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noTokenRoot.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noTokenRoot.setVisibility(View.GONE);
            adapter.refresh(tokenItemList);
            getPrice();
        }
    }

    private void getPrice() {
        for (TokenItem item : this.tokenItemList) {
            if (item.balance != 0.0 && item.chainId < 0)
                TokenService.getCurrency(item.symbol, currencyItem.getName()).subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                        moneyText.setText(presenter.getTotalMoney(tokenItemList));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            double price = Double.parseDouble(s.trim());
                            DecimalFormat df = new DecimalFormat("######0.00");
                            item.currencyPrice = Double.parseDouble(df.format(price * item.balance));
                        } else item.currencyPrice = 0.00;
                    }
                });
        }
    }

}
