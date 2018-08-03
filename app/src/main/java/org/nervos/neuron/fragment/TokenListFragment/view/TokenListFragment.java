package org.nervos.neuron.fragment.TokenListFragment.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.CurrencyActivity;
import org.nervos.neuron.activity.ReceiveQrCodeActivity;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.activity.TransferActivity;
import org.nervos.neuron.dialog.TokenTransferDialog;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.TokenListFragment.model.TokenAdapter;
import org.nervos.neuron.fragment.TokenListFragment.presenter.TokenListFragmentPresenter;
import org.nervos.neuron.item.CurrencyItem;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.SharePreConst;
import org.nervos.neuron.util.currency.TokenCurrencyManager;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

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
    private RelativeLayout totalMoneyRoot;
    private TextView totalText, moneyText;
    private ImageView addImage;
    private CurrencyItem currencyItem;
    private WalletItem walletItem = null;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_token_list;
    }

    @Override
    protected void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_token);
        noTokenRoot = (LinearLayout) findViewById(R.id.ll_no_token);
        totalMoneyRoot = (RelativeLayout) findViewById(R.id.ll_total_money);
        totalText = (TextView) findViewById(R.id.tv_total_money_title);
        moneyText = (TextView) findViewById(R.id.tv_total_money);
        addImage = (ImageView) findViewById(R.id.iv_add);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    @Override
    protected void initData() {
        presenter = new TokenListFragmentPresenter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TokenAdapter(getActivity(), this.tokenItemList);
        recyclerView.setAdapter(adapter);
        initWalletData(true);
        setCurrency();
        initRefresh();
    }

    @Override
    protected void initAction() {
        addImage.setOnClickListener((view) -> {
            startActivity(new Intent(getActivity(), TokenManageActivity.class));
        });
        adapter.setTokenAdapterListener(new TokenAdapter.TokenAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                TokenTransferDialog dialog = new TokenTransferDialog(getContext(), tokenItemList.get(position));
                dialog.setOnReceiveClickListener(v -> {
                    startActivity(new Intent(getActivity(), ReceiveQrCodeActivity.class));
                    dialog.dismiss();
                });
                dialog.setOnTransferClickListener(v -> {
                    Intent intent = new Intent(getActivity(), TransferActivity.class);
                    intent.putExtra(TransferActivity.EXTRA_TOKEN, tokenItemList.get(position));
                    startActivity(intent);
                    dialog.dismiss();
                });
                dialog.show();
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
        if (currencyItem == null || !currencyItem.getName().equals(presenter.getCurrencyItem().getName())) {
            currencyItem = presenter.getCurrencyItem();
            totalText.setText(getResources().getString(R.string.wallet_total_money) + "(" + currencyItem.getUnit() + ")");
            getPrice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData(true);
    }

    private void initRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initWalletData(false);
            }
        });
    }

    private void initWalletData(boolean showProgress) {
        WalletItem walletItem1 = DBWalletUtil.getCurrentWallet(getContext());
        if (showProgress) showProgressBar();
        WalletService.getWalletTokenBalance(getContext(), walletItem1, walletItem ->
                recyclerView.post(() -> {
                    if (showProgress) dismissProgressBar();
                    swipeRefreshLayout.setRefreshing(false);
                    if (walletItem.tokenItems != null) {
                        this.walletItem = walletItem;
                        this.tokenItemList = walletItem.tokenItems;
                        setData();
                    }
                })
        );
    }

    public void setData() {
        if (tokenItemList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noTokenRoot.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noTokenRoot.setVisibility(View.GONE);
            adapter.refresh(this.tokenItemList);
            getPrice();
        }
    }

    private void getPrice() {
        for (TokenItem item : this.tokenItemList) {
            if (item.balance != 0.0 && item.chainId < 0)
                TokenCurrencyManager.getTokenID(item.symbol).subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        if (!TextUtils.isEmpty(item.currencyID))
                            TokenCurrencyManager.getTokenCurrency(item.currencyID, currencyItem.getName()).subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                    LogUtil.e("Token", item.symbol + ">>" + item.currencyPrice);
                                    adapter.notifyDataSetChanged();
                                    moneyText.setText(presenter.getTotalMoney(tokenItemList));
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(String s) {
                                    if (!TextUtils.isEmpty(s)) {
                                        double price = Double.parseDouble(s.trim());
                                        DecimalFormat df = new DecimalFormat("######0.00");
//                                        item.currencyPrice = Double.parseDouble(df.format(price * 0.155));
                                        item.currencyPrice = Double.parseDouble(df.format(price * item.balance));
                                    } else
                                        item.currencyPrice = 0.00;
                                }
                            });
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        item.currencyID = s;
                    }
                });
        }
    }

}
