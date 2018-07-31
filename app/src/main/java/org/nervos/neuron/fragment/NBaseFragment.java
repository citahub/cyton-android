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
            if(null != vp) {
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
            initAction();
            initData();
        }
    }

    /**
     * 设置布局文件
     *
     */
    protected abstract int getContentLayout();

    /**
     * 控件初始化
     *
     */
    protected void initView() {};

    /**
     * 事件监听
     */
    protected void initAction() {};

    /**
     * 数据处理
     */
    protected void initData() {};

    /**
     * 查找控件
     */
    public View findViewById(int id) {
        View v = null;
        if (contentView != null) {
            v = contentView.findViewById(id);
        }
        return v;
    }
}
