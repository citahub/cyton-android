package com.cryptape.cita_wallet.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import com.cryptape.cita_wallet.view.dialog.ProgressingDialog;

/**
 * Created by duanyytop on 2018/5/17
 */
public class BaseFragment extends Fragment {

    private ProgressingDialog dialog = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    protected void showProgressBar() {
        if (dialog == null) dialog = new ProgressingDialog(getContext());
        dialog.show();
    }

    protected void showProgressBar(@StringRes int message) {
        if (dialog == null) dialog = new ProgressingDialog(getContext());
        dialog.show();
        dialog.setMsg(getString(message));
    }

    protected void showProgressBar(String message) {
        if (dialog == null) dialog = new ProgressingDialog(getContext());
        dialog.show();
        dialog.setMsg(message);
    }

    protected void dismissProgressBar() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    @Subscribe
    public void onEvent(Object object) {
    }
}
