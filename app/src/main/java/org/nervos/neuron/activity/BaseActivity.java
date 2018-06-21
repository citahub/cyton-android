package org.nervos.neuron.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.nervos.neuron.R;

public class BaseActivity extends AppCompatActivity {

    private View rootView;
    private View mProgressView;
    private View mProgressCircleView;

    protected Activity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

    }

    public void onDestroy() {
        super.onDestroy();
        rootView = null;
        mProgressView = null;
        mProgressCircleView = null;
    }

    /**
     * 显示Progress Bar
     */
    protected void showProgressBar() {
        showProgressBar(getString(R.string.loading));
    }

    protected void showProgressBar(@StringRes int message) {
        showProgressBar(getString(message));
    }

    protected void showProgressBar(String message) {
        if (mProgressView == null) {
            mProgressView = LayoutInflater.from(this).inflate(R.layout.progressbar_layout, null);
            TextView messageText = mProgressView.findViewById(R.id.progress_bar_text);
            messageText.setText(message);
            rootView = getWindow().getDecorView();
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;

            ((ViewGroup)rootView).addView(mProgressView, 1, fl);
        }
    }

    /**
     * 隐藏Progress Bar
     */
    protected void dismissProgressBar() {
        if (rootView != null && mProgressView != null) {
            ((ViewGroup)rootView).removeView(mProgressView);
        }
        mProgressView = null;
        rootView = null;
    }


    /**
     * show Progress circle
     */

    protected void showProgressCircle() {
        if (mProgressCircleView == null) {
            mProgressCircleView = LayoutInflater.from(this).inflate(R.layout.progressbar_circle, null);
            rootView = getWindow().getDecorView();
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;

            ((ViewGroup)rootView).addView(mProgressCircleView, 1, fl);
        }
    }

    /**
     * hide Progress circle
     */
    protected void dismissProgressCircle() {
        if (rootView != null && mProgressCircleView != null) {
            ((ViewGroup)rootView).removeView(mProgressCircleView);
        }
        mProgressCircleView = null;
        rootView = null;
    }

}
