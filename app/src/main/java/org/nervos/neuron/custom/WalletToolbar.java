package org.nervos.neuron.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;

/**
 * Created by BaojunCZ on 2018/8/1.
 */
public class WalletToolbar extends Toolbar {
    private TextView mTxtMiddleTitle;
    private ImageView mIVRight;

    public WalletToolbar(Context context) {
        this(context, null);
    }

    public WalletToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WalletToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTxtMiddleTitle = findViewById(R.id.tv_main_title);
        mIVRight = findViewById(R.id.iv_right);
    }

    public void setMainTitle(String text) {
        this.setTitle(" ");
        mTxtMiddleTitle.setVisibility(View.VISIBLE);
        mTxtMiddleTitle.setText(text);
    }

    //set title font color
    public void setMainTitleColor(int color) {
        mTxtMiddleTitle.setTextColor(color);
    }


    //set right pic
    public void setRightTitleDrawable(int res) {
        mIVRight.setImageResource(res);
    }

    //set right clicklistener
    public void setRightTitleClickListener(OnClickListener onClickListener) {
        mIVRight.setOnClickListener(onClickListener);
    }
}
