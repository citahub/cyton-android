package org.nervos.neuron.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.nervos.neuron.R;
import org.nervos.neuron.activity.AddWalletActivity;
import org.nervos.neuron.custom.WalletToolbar;
import org.nervos.neuron.custom.WalletTopView;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.db.DBWalletUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BaojunCZ on 2018/8/1.
 */
public class WalletFragments extends NBaseFragment {

    public static final String TAG = WalletFragments.class.getName();

    private NestedScrollView mNestedScrollView;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private AppBarLayout appBarLayout;
    private WalletToolbar toolbar;
    private WalletTopView walletView;
    private WalletItem walletItem;

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
        initWalletData(true);
    }

    @Override
    protected void initAction() {
        super.initAction();
        appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRangle = appBarLayout.getTotalScrollRange();
            if (verticalOffset == 0) {
                toolbar.setAlpha(0.0f);
                toolbar.setVisibility(View.GONE);
                walletView.setAlpha(1.0f);
            } else {
                //保留一位小数
                float alfha = Math.round(100.0f * Math.abs(verticalOffset) / scrollRangle) / 100.0f;
                toolbar.setAlpha(alfha);
                toolbar.setVisibility(View.VISIBLE);
                walletView.setAlpha(1.0f - alfha);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletSaveEvent(TokenRefreshEvent event) {
        initWalletData(true);
    }

    private void initWalletData(boolean showProgress) {
        if ((walletItem = DBWalletUtil.getCurrentWallet(getContext())) != null) {
//            if (showProgress) showProgressBar();
            walletView.setWalletItem(walletItem);
//            walletNameList = DBWalletUtil.getAllWalletName(getContext());
//            WalletService.getWalletTokenBalance(getContext(), walletItem, walletItem ->
//                    walletNameText.post(() -> {
//                        if (showProgress) dismissProgressBar();
//                        swipeRefreshLayout.setRefreshing(false);
//                        if (walletItem.tokenItems != null) {
//                            tokenItemList = walletItem.tokenItems;
//                            tokenAdapter.notifyDataSetChanged();
//                        }
//                    })
//            );
        } else {
            startActivity(new Intent(getActivity(), AddWalletActivity.class));
        }
    }

    private class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new MyFragment();
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

    public static class MyFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            String string = "123";
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                strings.add(string);
            }
            RecyclerView recyclerView = new RecyclerView(getActivity());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new MyListAdapter(strings));
            return recyclerView;
        }


        class MyListAdapter extends RecyclerView.Adapter {
            private List<String> mStrings;

            public MyListAdapter(List<String> strings) {
                this.mStrings = strings;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item, parent, false);
                return new MyListViewHolder(convertView);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                MyListViewHolder viewHolder = (MyListViewHolder) holder;
                viewHolder.mTextView.setText(mStrings.get(position));
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        }

        class MyListViewHolder extends RecyclerView.ViewHolder {
            private TextView mTextView;

            public MyListViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.tv_content);
            }
        }
    }
}
