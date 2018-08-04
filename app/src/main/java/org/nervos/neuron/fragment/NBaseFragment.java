package org.nervos.neuron.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 包俊 on 2018/7/30.
 */
public abstract class NBaseFragment extends BaseFragment {

    protected View contentView = null;
    protected boolean isFirstLoad = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(getContentLayout(), container, false);
            isFirstLoad = true;
        } else {
            isFirstLoad = false;
            ViewGroup vp = (ViewGroup) contentView.getParent();
            if (null != vp) {
                vp.removeView(contentView);
            }
        }
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFirstLoad) {
            initView();
            initData();
            initAction();
        }
    }

    protected abstract int getContentLayout();

    protected void initView() {
    }

    protected void initAction() {
    }

    protected void initData() {
    }

    public View findViewById(int id) {
        View v = null;
        if (contentView != null) {
            v = contentView.findViewById(id);
        }
        return v;
    }
}
