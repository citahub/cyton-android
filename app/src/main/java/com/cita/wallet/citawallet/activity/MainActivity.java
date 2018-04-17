package com.cita.wallet.citawallet.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cita.wallet.citawallet.fragment.DiscoverFragment;
import com.cita.wallet.citawallet.fragment.MineFragment;
import com.cita.wallet.citawallet.R;
import com.cita.wallet.citawallet.fragment.WalletFragment;

public class MainActivity extends AppCompatActivity {

    private RadioGroup navigation;
    private DiscoverFragment discoverFragment;
    private WalletFragment walletFragment;
    private MineFragment mineFragment;
    private FragmentManager fMgr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

    }


    private void initView(){
        navigation = findViewById(R.id.navigation);
        navigation.check(RadioGroup.NO_ID);
        fMgr = getFragmentManager();
    }

    private void initListener() {
        navigation.setOnCheckedChangeListener((group, checkedId) -> {
            FragmentTransaction transaction = fMgr.beginTransaction();
            hideFragments(transaction);
            switch (checkedId) {
                case R.id.navigation_discover:
                    if(discoverFragment == null){
                        discoverFragment = new DiscoverFragment();
                        transaction.add(R.id.fragment, discoverFragment);
                    } else {
                        transaction.show(discoverFragment);
                    }
                    break;
                case R.id.navigation_wallet:
                    if(walletFragment == null){
                        walletFragment = new WalletFragment();
                        transaction.add(R.id.fragment, walletFragment);
                    } else {
                        transaction.show(walletFragment);
                    }
                    break;
                case R.id.navigation_mine:
                    if(mineFragment == null){
                        mineFragment = new MineFragment();
                        transaction.add(R.id.fragment, mineFragment);
                    } else {
                        transaction.show(mineFragment);
                    }
                    break;
                default:
                    if(walletFragment == null){
                        walletFragment = new WalletFragment();
                        transaction.add(R.id.fragment, walletFragment);
                    } else {
                        transaction.show(walletFragment);
                    }
                    break;
            }
            transaction.commitAllowingStateLoss();
        });

        setNavigationItem(WalletFragment.TAG);

    }

    /**
     * 根据各个Fragment的TAG判断跳转到何处
     *
     * @param tag Fragment标签值
     */
    public void setNavigationItem(String tag) {
        if (TextUtils.equals(tag, DiscoverFragment.TAG)) {
            navigation.check(R.id.navigation_discover);
        } else if (TextUtils.equals(tag, WalletFragment.TAG)) {
            navigation.check(R.id.navigation_wallet);
        } else if (TextUtils.equals(tag, MineFragment.TAG)) {
            navigation.check(R.id.navigation_mine);
        }
    }


    /**
     * 隐藏所有的fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (discoverFragment != null) {
            transaction.hide(discoverFragment);
        }
        if (walletFragment != null) {
            transaction.hide(walletFragment);
        }
        if (mineFragment != null) {
            transaction.hide(mineFragment);
        }
    }



    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (fMgr.findFragmentByTag(WalletFragment.TAG) != null && !fMgr.findFragmentByTag(WalletFragment.TAG).isVisible()) {
                FragmentTransaction fragmentTransaction = fMgr.beginTransaction();
                hideFragments(fragmentTransaction);
                setNavigationItem(WalletFragment.TAG);
                return true;
            } else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return false;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
