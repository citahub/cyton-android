package org.nervos.neuron.fragment.wallet.view;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.activity.ChangeWalletActivity;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.NBaseFragment;
import org.nervos.neuron.fragment.collection.CollectionListFragment;
import org.nervos.neuron.fragment.token.view.TokenListFragment;
import org.nervos.neuron.fragment.wallet.presenter.WalletFragmentPresenter;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.service.intent.AppChainTransactionCheckService;
import org.nervos.neuron.service.intent.EtherTransactionCheckService;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.url.HttpAppChainUrls;
import org.nervos.neuron.view.WalletToolbar;
import org.nervos.neuron.view.WalletTopView;

/**
 * Created by BaojunCZ on 2018/8/1.
 */
public class WalletsFragment extends NBaseFragment {

    public static final String TAG = WalletsFragment.class.getName();
    private static final int APPCAHIN_TRANSACTION_FETCH_PERIOD = 3000;
    private static final int ETHER_TRANSACTION_FETCH_PERIOD = 15000;

    private NestedScrollView mNestedScrollView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AppBarLayout appBarLayout;
    private WalletToolbar toolbar;
    private WalletTopView walletView;
    private WalletItem walletItem;
    private TokenListFragment tokenListFragment;
    private CollectionListFragment collectionListFragment = null;
    private WalletFragmentPresenter presenter;

    private String[] mTitles = {"代币", "藏品"};

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_wallets;
    }

    @Override
    protected void initView() {
        super.initView();
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        toolbar = (WalletToolbar) findViewById(R.id.toolbar);
        walletView = (WalletTopView) findViewById(R.id.wallet_view);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        mNestedScrollView.setFillViewport(true);

        mTabLayout.setupWithViewPager(mViewPager);
        MyAdapter adapter = new MyAdapter(getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        super.initData();
        walletView.setActivity(getActivity());
        mTabLayout.post(() -> {
            presenter.setIndicator(mTabLayout, 70, 70);
        });
        tokenListFragment = new TokenListFragment();
        presenter = new WalletFragmentPresenter(getActivity());
        initWalletData();

        startCheckAppChainTransaction();
        startCheckEtherTransaction();

    }

    @Override
    protected void initAction() {
        super.initAction();
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRange = appBarLayout.getTotalScrollRange();
            if (verticalOffset == 0) {
                toolbar.setAlpha(0.0f);
                toolbar.setVisibility(View.GONE);
                walletView.setAlpha(1.0f);
            } else if (Math.abs(verticalOffset) > scrollRange / 4) {
                float alpha = Math.round(100.0f * Math.abs(verticalOffset) / scrollRange) / 100.0f;
                toolbar.setAlpha(alpha);
                toolbar.setVisibility(View.VISIBLE);
                walletView.setAlpha(1.0f - alpha);
            }
        });
        toolbar.setRightTitleClickListener((view) -> {
            if (DBWalletUtil.getAllWallet(getActivity()).size() > 1) {
                Intent intent2 = new Intent(getActivity(), ChangeWalletActivity.class);
                startActivity(intent2);
                getActivity().overridePendingTransition(R.anim.wallet_activity_in, 0);
            } else {
                startActivity(new Intent(getActivity(), AddWalletActivity.class));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData();
    }

    private void initWalletData() {
        if (DBWalletUtil.getAllWallet(getActivity()).size() > 1) {
            toolbar.setmIVRight(R.drawable.ic_wallet_exchange);
        } else {
            toolbar.setmIVRight(R.drawable.ic_wallet_top_exchange);
        }
        if ((walletItem = DBWalletUtil.getCurrentWallet(getContext())) != null) {
            walletView.setWalletItem(walletItem);
        } else {
            startActivity(new Intent(getActivity(), AddWalletActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initWalletData();
    }

    private void startCheckAppChainTransaction() {
        AppChainRpcService.init(getContext(), HttpAppChainUrls.APPCHAIN_NODE_URL);
        AppChainTransactionCheckService.enqueueWork(getContext(), new Intent());
        AppChainTransactionCheckService.listener = () ->
                new Handler().postDelayed(this::startCheckAppChainTransaction, APPCAHIN_TRANSACTION_FETCH_PERIOD);
    }

    private void startCheckEtherTransaction() {
        EthRpcService.initNodeUrl();
        EtherTransactionCheckService.enqueueWork(getContext(), new Intent());
        EtherTransactionCheckService.listener = () ->
                new Handler().postDelayed(this::startCheckEtherTransaction, ETHER_TRANSACTION_FETCH_PERIOD);
    }

    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return tokenListFragment;
                case 1:
                    if (collectionListFragment == null)
                        collectionListFragment = new CollectionListFragment();
                    return collectionListFragment;
                default:
                    return tokenListFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

}
