package org.nervos.neuron.fragment.TokenListFragment.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.TokenListFragment.model.TokenAdapter;
import org.nervos.neuron.fragment.TokenListFragment.presenter.TokenListFragmentPresenter;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.WalletService;
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
    private TokenListFragmentImpl listener = null;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_token_list;
    }

    @Override
    protected void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_token);
        noTokenRoot = (LinearLayout) findViewById(R.id.ll_no_token);
    }

    @Override
    protected void initData() {
        presenter = new TokenListFragmentPresenter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TokenAdapter(getActivity(), this.tokenItemList);
        recyclerView.setAdapter(adapter);
        initWalletData(true);
    }

    @Override
    protected void initAction() {
    }

    public void setListener(TokenListFragmentImpl listener) {
        this.listener = listener;
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
            if (listener != null)
                listener.setTotalMoney(presenter.getTotalMoney(this.tokenItemList));
        }
    }

    public interface TokenListFragmentImpl {
        void setTotalMoney(String money);
    }

}
