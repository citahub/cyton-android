package org.nervos.neuron.activity;

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

    public void onDestroy() {
        super.onDestroy();
        rootView = null;
        mProgressView = null;
    }

    /**
     * 显示Progress Bar
     */
    protected void showProgressBar() {
        showProgressBar("正在加载");
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

}
