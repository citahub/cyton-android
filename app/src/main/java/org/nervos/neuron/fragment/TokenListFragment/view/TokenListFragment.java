package org.nervos.neuron.fragment.TokenListFragment.view;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.TokenManageActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.TokenListFragment.model.TokenAdapter;
import org.nervos.neuron.fragment.TokenListFragment.presenter.TokenListFragmentPresenter;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.WalletService;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    protected void initData() {
        presenter = new TokenListFragmentPresenter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TokenAdapter(getActivity(), this.tokenItemList);
        recyclerView.setAdapter(adapter);
        LogUtil.e("Token", "initData");
        initWalletData(true);
        totalText.setText(getResources().getString(R.string.wallet_total_money) + presenter.getTotalMoneyTitle());
    }

    @Override
    protected void initAction() {
        addImage.setOnClickListener((view) -> {
            startActivity(new Intent(getActivity(), TokenManageActivity.class));
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData(true);
    }

    private void initWalletData(boolean showProgress) {
        WalletItem walletItem1 = DBWalletUtil.getCurrentWallet(getContext());
        if (showProgress) showProgressBar();
        WalletService.getWalletTokenBalance(getContext(), walletItem1, walletItem ->
                recyclerView.post(() -> {
                    if (showProgress) dismissProgressBar();
                    if (walletItem.tokenItems != null) {
                        setData(walletItem.tokenItems);
                    }
                })
        );
    }

    public void setData(List<TokenItem> tokenItemList) {
        this.tokenItemList = tokenItemList;
        if (tokenItemList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noTokenRoot.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noTokenRoot.setVisibility(View.GONE);
            adapter.refresh(this.tokenItemList);
            moneyText.setText(presenter.getTotalMoney(this.tokenItemList));
        }
    }

}
