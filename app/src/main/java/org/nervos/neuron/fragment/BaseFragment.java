package org.nervos.neuron.fragment;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.nervos.neuron.R;

public class BaseFragment extends Fragment {

    private View rootView;
    private View mProgressView;

    public void onDestroy() {
        super.onDestroy();
        rootView = null;
        mProgressView = null;
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
        Activity activity = getActivity();
        if (mProgressView == null) {
            mProgressView = LayoutInflater.from(activity).inflate(R.layout.progressbar_layout, null);
            TextView messageText = mProgressView.findViewById(R.id.progress_bar_text);
            messageText.setText(message);
            rootView = activity.getWindow().getDecorView();
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

}
