package com.cryptape.cita_wallet.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.constant.ConstantUtil;
import com.cryptape.cita_wallet.util.ScreenUtils;
import com.cryptape.cita_wallet.util.db.DBWalletUtil;
import com.cryptape.cita_wallet.util.db.SharePrefUtil;
import com.cryptape.cita_wallet.view.dialog.ProtocolDialog;

/**
 * Created by duanyytop on 2018/5/17
 */
public class AddWalletActivity extends NBaseActivity {

    private ViewPager viewPager;
    private SparseArray<View> lists = new SparseArray<>();
    private SparseArray<ImageView> IndicateList = new SparseArray<>();

    @Override
    protected int getContentLayout() {
        return R.layout.activity_add_wallet;
    }

    @Override
    protected void initView() {
        viewPager = findViewById(R.id.viewPager);
        RelativeLayout.LayoutParams paramsV = new RelativeLayout.LayoutParams((int) (ScreenUtils.getScreenHeight(mActivity) * 0.6 * 750 / 800), (int) (ScreenUtils.getScreenHeight(mActivity) * 0.6));
        paramsV.topMargin = (int) (ScreenUtils.getScreenHeight(mActivity) * 0.1);
        viewPager.setLayoutParams(paramsV);
    }

    @Override
    protected void initData() {
        initGuideList();
        initIndicate();
        viewPager.setAdapter(new PagerAadapter());
        if (!SharePrefUtil.getBoolean(ConstantUtil.PROTOCOL, false))
            viewPager.postDelayed(() -> {
                ProtocolDialog dialog = new ProtocolDialog(mActivity);
                dialog.show();
            }, 500);
    }

    @Override
    protected void initAction() {
        findViewById(R.id.create_wallet_button).setOnClickListener(v -> {
            startActivity(new Intent(mActivity, CreateWalletActivity.class));
            finish();
        });
        findViewById(R.id.import_wallet_button).setOnClickListener(v -> {
            startActivity(new Intent(mActivity, ImportWalletActivity.class));
            finish();
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < IndicateList.size(); i++)
                    if (position == i)
                        Glide.with(mActivity)
                                .load(R.drawable.wallet_guide_indicate_selecter)
                                .into(IndicateList.get(i));
                    else
                        Glide.with(mActivity)
                                .load(R.drawable.wallet_guide_indicate_unselect)
                                .into(IndicateList.get(i));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    private void initGuideList() {
        View guide1 = LayoutInflater.from(this).inflate(R.layout.item_wallet_guide, null);
        View guide2 = LayoutInflater.from(this).inflate(R.layout.item_wallet_guide, null);
        View guide3 = LayoutInflater.from(this).inflate(R.layout.item_wallet_guide, null);
        lists.put(0, guide1);
        lists.put(1, guide2);
        lists.put(2, guide3);
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(this)
                .load(R.drawable.wallet_guide1)
                .apply(options)
                .into((ImageView) guide1.findViewById(R.id.iv_guide));
        Glide.with(this)
                .load(R.drawable.wallet_guide2)
                .apply(options)
                .into((ImageView) guide2.findViewById(R.id.iv_guide));
        Glide.with(this)
                .load(R.drawable.wallet_guide3)
                .apply(options)
                .into((ImageView) guide3.findViewById(R.id.iv_guide));
    }

    private void initIndicate() {
        IndicateList.put(0, findViewById(R.id.iv_indicate1));
        IndicateList.put(1, findViewById(R.id.iv_indicate2));
        IndicateList.put(2, findViewById(R.id.iv_indicate3));
    }

    private void goBack() {
        if (DBWalletUtil.getCurrentWallet(mActivity) != null) {
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName()))
                goBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(SharePrefUtil.getCurrentWalletName()))
            goBack();
    }

    class PagerAadapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(lists.get(position));
            return lists.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(lists.get(position));
        }
    }
}
