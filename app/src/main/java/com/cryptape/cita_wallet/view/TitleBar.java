package com.cryptape.cita_wallet.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cryptape.cita_wallet.R;

/**
 * Created by duanyangyang on 18/1/5.
 */
public class TitleBar extends RelativeLayout implements View.OnClickListener {
    private RelativeLayout mTitleBarView, mTitleBarLeftView, mTitleBarCenterView, mTitleBarRightView;
    private TextView mTitleBarLeftDefaultView, mTitleBarCenterDefaultView, mTitleBarRightDefaultView;
    private View bottomLine;
    private String mTitle, mLeftText, mRightText;
    private Context mContext;
    private OnRightClickListener mOnRightClickListener;
    private OnLeftClickListener mOnLeftClickListener;
    private boolean mIsShowLeft, mIsShowRight, mIsShowBottom;
    private int mTitleImg, mLeftImg, mRightImg, mTitleBg;


    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
        initFromAttributes(attrs);
        initListener();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.custom_titlebar, this, true);
        mTitleBarView = findViewById(R.id.title_bar);
        mTitleBarLeftView = findViewById(R.id.title_bar_left);
        mTitleBarCenterView = findViewById(R.id.title_bar_center);
        mTitleBarRightView = findViewById(R.id.title_bar_right);
        mTitleBarLeftDefaultView = findViewById(R.id.title_bar_left_default);
        mTitleBarCenterDefaultView = findViewById(R.id.title_bar_center_default);
        mTitleBarRightDefaultView = findViewById(R.id.title_bar_right_default);
        bottomLine = findViewById(R.id.bottom_line);
    }

    private void initListener() {
        mTitleBarLeftView.setOnClickListener(this);
        mTitleBarRightView.setOnClickListener(this);
    }

    private void initFromAttributes(AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        mIsShowLeft = a.getBoolean(R.styleable.TitleBar_isShowLeft, true);
        mIsShowRight = a.getBoolean(R.styleable.TitleBar_isShowRight, false);
        mIsShowBottom = a.getBoolean(R.styleable.TitleBar_isShowBottomLine, true);

        if (mIsShowRight)
            showRight();
        else
            hideRight();

        if (mIsShowLeft)
            showLeft();
        else
            hideLeft();

        if (mIsShowBottom) {
            showBottomLine();
        } else {
            hideBottomLine();
        }

        mTitle = a.getString(R.styleable.TitleBar_title);
        mTitleBarCenterDefaultView.setText(mTitle);
        int mTitleColor = a.getResourceId(R.styleable.TitleBar_title_color, 0);
        if (mTitleColor != 0)
            mTitleBarCenterDefaultView.setTextColor(getResources().getColor(mTitleColor));
        mLeftText = a.getString(R.styleable.TitleBar_left_text);
        mTitleBarLeftDefaultView.setText(mLeftText);
        mRightText = a.getString(R.styleable.TitleBar_right_text);
        mTitleBarRightDefaultView.setText(mRightText);

        Drawable drawable;
        mTitleImg = a.getResourceId(R.styleable.TitleBar_title_img, 0);

        mTitleBg = a.getResourceId(R.styleable.TitleBar_title_bg, R.color.white);
        mTitleBarView.setBackgroundResource(mTitleBg);

        if (0 != mTitleImg) {
            drawable = mContext.getResources().getDrawable(mTitleImg);
            mTitleBarCenterDefaultView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
        mLeftImg = a.getResourceId(R.styleable.TitleBar_left_img, R.drawable.black_back);
        drawable = mContext.getResources().getDrawable(mLeftImg);
        mTitleBarLeftDefaultView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        mRightImg = a.getResourceId(R.styleable.TitleBar_right_img, 0);
        if (0 != mRightImg) {
            drawable = mContext.getResources().getDrawable(mRightImg);
            mTitleBarRightDefaultView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
        a.recycle();

    }


    /**
     * 设置左边图片(仅图片)
     *
     * @param res 图片资源id
     */
    public void setLeftImage(int res) {
        showLeft();
        mTitleBarLeftDefaultView.setText("");
        Drawable drawable = mContext.getResources().getDrawable(res);
        mTitleBarLeftDefaultView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    /**
     * 设置左边文字 (仅文字)
     *
     * @param text
     */
    public void setLeftText(String text) {
        showLeft();
        mTitleBarLeftDefaultView.setText(text);
        mTitleBarLeftDefaultView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    /**
     * 设置左边View
     *
     * @param v
     */
    public void setLeftView(View v) {
        showLeft();
        mTitleBarLeftView.removeAllViews();
        mTitleBarLeftView.addView(v);
    }

    /**
     * 隐藏左边
     */
    public void hideLeft() {
        mIsShowLeft = false;
        mTitleBarLeftView.setEnabled(false);
        mTitleBarLeftView.setVisibility(View.GONE);
    }

    /**
     * 显示左边
     */
    public void showLeft() {
        mIsShowRight = true;
        mTitleBarLeftView.setEnabled(true);
        mTitleBarLeftView.setVisibility(View.VISIBLE);
    }

    /**
     * 是否左边
     */
    public boolean isShowLeft() {
        return mIsShowLeft;
    }

    /**
     * 获取右边默认的TextView
     *
     * @return
     */
    public TextView getDefaultLeft() {

        return mTitleBarLeftDefaultView;
    }

    /**
     * 设置右边图片(仅图片)
     *
     * @param res 图片资源id
     */
    public void setRightImage(int res) {
        showRight();
        mTitleBarRightDefaultView.setText("");
        Drawable drawable = mContext.getResources().getDrawable(res);
        mTitleBarRightDefaultView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    /**
     * 设置右边文字 (仅文字)
     *
     * @param text
     */
    public void setRightText(String text) {
        showRight();
        mTitleBarRightDefaultView.setText(text);
        mTitleBarRightDefaultView.setCompoundDrawables(null, null, null, null);
    }

    public void setRightTextColor(int color) {
        mTitleBarRightDefaultView.setTextColor(ContextCompat.getColor(getContext(), color));
    }

    /**
     * 获取右边文字 (仅文字)
     */
    public String getRightText() {
        return mTitleBarRightDefaultView.getText().toString();
    }

    /**
     * 设置右边View
     *
     * @param v
     */
    public void setRightView(View v) {
        showRight();
        mTitleBarCenterView.removeAllViews();
        mTitleBarCenterView.addView(v);
    }

    /**
     * 获取右边默认的TextView
     *
     * @return
     */
    public TextView getDefaultRight() {
        return mTitleBarRightDefaultView;
    }

    /**
     * 隐藏右边
     */
    public void hideRight() {
        mIsShowRight = false;
        mTitleBarRightView.setEnabled(false);
        mTitleBarRightView.setVisibility(View.GONE);

    }

    /**
     * 显示右边
     */
    public void showRight() {
        mIsShowRight = true;
        mTitleBarRightView.setEnabled(true);
        mTitleBarRightView.setVisibility(View.VISIBLE);
    }

    public void showBottomLine() {
        mIsShowBottom = true;
        bottomLine.setVisibility(VISIBLE);
    }

    public void hideBottomLine() {
        mIsShowBottom = false;
        bottomLine.setVisibility(GONE);
    }


    /**
     * 设置标题 (仅文字)
     *
     * @param title
     */
    public void setTitle(String title) {
        mTitle = title;
        mTitleBarCenterDefaultView.setText(title);
        mTitleBarCenterDefaultView.setCompoundDrawables(null, null, null, null);
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * 设置标题View
     *
     * @param v
     */
    public void setCenterView(View v) {
        mTitleBarCenterView.removeAllViews();
        mTitleBarCenterView.addView(v);
    }


    /**
     * 设置标题图片(仅图片)
     *
     * @param res 图片资源id
     */
    public void setCenterImage(int res) {
        mTitleBarCenterDefaultView.setText("");
        Drawable drawable = mContext.getResources().getDrawable(res);
        mTitleBarCenterDefaultView.setCompoundDrawables(drawable, null, null, null);
    }

    /**
     * 获取标题默认的TextView
     *
     * @return
     */
    public TextView getDefaultCenter() {
        return mTitleBarCenterDefaultView;
    }

    public void setOnRightClickListener(OnRightClickListener onRightClickListener) {
        this.mOnRightClickListener = onRightClickListener;
    }

    public void setOnLeftClickListener(OnLeftClickListener onLeftClickListener) {
        this.mOnLeftClickListener = onLeftClickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_bar_left:
                mTitleBarLeftView.setClickable(false);
                if (null != mOnLeftClickListener) {
                    mOnLeftClickListener.onLeftClick();
                } else {
                    goBack();
                }
                mTitleBarLeftView.setClickable(true);
                break;
            case R.id.title_bar_right:
                if (null != mOnRightClickListener)
                    mOnRightClickListener.onRightClick();
                break;
            default:
                break;
        }

    }


    /**
     * 返回
     */
    private void goBack() {
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    }

    /**
     * 右边按钮点击监听
     */
    public interface OnRightClickListener {
        void onRightClick();
    }

    /**
     * 左边按钮监听 未设置时 默认添加 实现返回操作
     */
    public interface OnLeftClickListener {
        void onLeftClick();
    }
}
