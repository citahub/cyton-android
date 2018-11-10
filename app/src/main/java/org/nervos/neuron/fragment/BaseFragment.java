package org.nervos.neuron.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.nervos.neuron.view.dialog.ProgressingDialog;

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

    /**
     * 显示Progress Bar
     */
    protected void showProgressBar() {
        if (dialog == null)
            dialog = new ProgressingDialog(getActivity());
        dialog.show();
    }

    protected void showProgressBar(@StringRes int message) {
        if (dialog == null)
            dialog = new ProgressingDialog(getActivity());
        dialog.show();
        dialog.setMsg(getString(message));
    }

    protected void showProgressBar(String message) {
        if (dialog == null)
            dialog = new ProgressingDialog(getActivity());
        dialog.show();
        dialog.setMsg(message);
    }

    /**
     * 隐藏Progress Bar
     */
    protected void dismissProgressBar() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    @Subscribe
    public void onEvent(Object object) {
    }
}
