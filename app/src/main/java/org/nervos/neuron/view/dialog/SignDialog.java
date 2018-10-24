package org.nervos.neuron.view.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.view.NoScrollViewPager;
import org.nervos.neuron.view.button.CommonButton;
import org.nervos.neuron.view.webview.item.Message;
import org.nervos.neuron.view.webview.item.Transaction;
import org.web3j.utils.Numeric;

import javax.annotation.Nullable;

/**
 * Created by BaojunCZ on 2018/9/13.
 */
public class SignDialog {

    private BottomSheetDialog mDialog;
    private View view;
    private View mViewSign, mViewPwd;
    private NoScrollViewPager mViewPager;
    private CommonButton mCbtnSign, mCbtnPwd;
    private ImageView mIvSignClose, mIvTransferClos;
    private ProgressBar mProgressBar;
    private AppCompatEditText mEtPassword;
    private Message<Transaction> mMessage;
    private OnSignDataListener mListener;

    public SignDialog(@Nullable Activity context, Message<Transaction> message
            , OnSignDataListener listener) {
        this.mMessage = message;
        this.mListener = listener;
        mDialog = new BottomSheetDialog(context);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_sign, null);
        mViewSign = LayoutInflater.from(context).inflate(R.layout.dialog_sign_check, null);
        mViewPwd = LayoutInflater.from(context).inflate(R.layout.dialog_transfer_password, null);
        mDialog.setContentView(view);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
        initView();
        initData();
        initAction();
    }

    private void initView() {
        mViewPager = view.findViewById(R.id.viewPager);
        mCbtnSign = mViewSign.findViewById(R.id.btn_sign);
        mCbtnPwd = mViewPwd.findViewById(R.id.transfer_send_button);
        mIvSignClose = mViewSign.findViewById(R.id.close_layout);
        mIvTransferClos = mViewPwd.findViewById(R.id.close_layout);
        mProgressBar = mViewPwd.findViewById(R.id.transfer_progress);
        mEtPassword = mViewPwd.findViewById(R.id.wallet_password_edit);
    }

    private void initData() {
        mViewPager.setAdapter(new PageAdapter());
        mViewPager.setScroll(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset
                    , int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mCbtnSign.setClickable(true);
                } else {
                    mCbtnSign.setClickable(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (!TextUtils.isEmpty(mMessage.value.data) &&
                Numeric.containsHexPrefix(mMessage.value.data)) {
            ((TextView) mViewSign.findViewById(R.id.tv_sign_message))
                    .setText(NumberUtil.hexToUtf8(mMessage.value.data));
        }

    }

    private void initAction() {
        mCbtnSign.setOnClickListener(view1 -> {
            mCbtnSign.setClickable(false);
            mViewPager.setCurrentItem(1);
        });
        mIvSignClose.setOnClickListener(view1 -> {
            if (mListener != null)
                mListener.reject(mMessage);
            mDialog.dismiss();
        });
        mIvTransferClos.setOnClickListener(view1 -> {
            if (mListener != null)
                mListener.reject(mMessage);
            mDialog.dismiss();
        });
        mCbtnPwd.setOnClickListener(view1 -> {
            String password = mEtPassword.getText().toString().trim();
            if (mListener != null)
                mListener.send(password, mProgressBar, mMessage);
        });
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public boolean isShowing() {
        return mDialog.isShowing();
    }

    class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            if (position == 0) {
                container.addView(mViewSign);
                return mViewSign;
            } else {
                container.addView(mViewPwd);
                return mViewPwd;
            }

        }
    }

    public interface OnSignDataListener {
        void send(String pwd, ProgressBar progressBar, Message<Transaction> message);

        void reject(Message<Transaction> message);
    }

}
