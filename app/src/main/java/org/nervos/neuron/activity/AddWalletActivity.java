package org.nervos.neuron.activity;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.nervos.neuron.R;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.db.SharePrefUtil;

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
    }

    @Override
    protected void initData() {
        initGuideList();
        initIndicate();
        viewPager.setAdapter(new PagerAadapter());
    }

    @Override
    protected void initAction() {
        findViewById(R.id.create_wallet_button).setOnClickListener(v -> {
            startActivity(new Intent(mActivity, CreateWalletActivity.class));
        });

        findViewById(R.id.import_wallet_button).setOnClickListener(v ->
                startActivity(new Intent(mActivity, ImportWalletActivity.class)));
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
        ((TextView) guide1.findViewById(R.id.tv_guide)).setText(R.string.wallet_guide1_text);
        ((TextView) guide2.findViewById(R.id.tv_guide)).setText(R.string.wallet_guide2_text);
        ((TextView) guide3.findViewById(R.id.tv_guide)).setText(R.string.wallet_guide3_text);
        Glide.with(this)
                .load(R.drawable.wallet_guide1)
                .into((ImageView) guide1.findViewById(R.id.iv_guide));
        Glide.with(this)
                .load(R.drawable.wallet_guide2)
                .into((ImageView) guide2.findViewById(R.id.iv_guide));
        Glide.with(this)
                .load(R.drawable.wallet_guide3)
                .into((ImageView) guide3.findViewById(R.id.iv_guide));
    }

    private void initIndicate() {
        IndicateList.put(0, findViewById(R.id.iv_indicate1));
        IndicateList.put(1, findViewById(R.id.iv_indicate2));
        IndicateList.put(2, findViewById(R.id.iv_indicate3));
    }

    private void goBack() {
        if (DBWalletUtil.getCurrentWallet(mActivity) == null) {
            Intent intent = new Intent(mActivity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(MainActivity.EXTRA_TAG, AppFragment.TAG);
            startActivity(intent);
            finish();
        } else {
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
