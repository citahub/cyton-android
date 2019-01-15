package com.cryptape.cita_wallet.view.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatEditText;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.view.NoScrollViewPager;
import com.cryptape.cita_wallet.view.button.CommonButton;

import javax.annotation.Nullable;

/**
 * Created by BaojunCZ on 2018/9/13.
 */
public class TransferDialog {

    private BottomSheetDialog dialog;
    private View view;
    private View confirmView, pwdView;
    private NoScrollViewPager viewPager;
    private CommonButton confirmButton, pwdButton;
    private RelativeLayout mRlConfirmClose, mRlTransferClose;
    private ProgressBar progressBar;
    private AppCompatEditText passwordEdit;
    private OnSendPwdListener listener = null;

    public TransferDialog(@Nullable Activity context, OnSendPwdListener listener) {
        this.listener = listener;
        dialog = new BottomSheetDialog(context);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_transfer, null);
        confirmView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_transfer, null);
        pwdView = LayoutInflater.from(context).inflate(R.layout.dialog_transfer_password, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        initView();
        initData();
        initAction();
    }

    private void initView() {
        viewPager = view.findViewById(R.id.viewPager);
        confirmButton = confirmView.findViewById(R.id.transfer_confirm_button);
        pwdButton = pwdView.findViewById(R.id.transfer_send_button);
        mRlConfirmClose = confirmView.findViewById(R.id.rl_close);
        mRlTransferClose = pwdView.findViewById(R.id.rl_close);
        progressBar = pwdView.findViewById(R.id.transfer_progress);
        passwordEdit = pwdView.findViewById(R.id.wallet_password_edit);
    }

    private void initData() {
        viewPager.setAdapter(new PageAdapter());
        viewPager.setScroll(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    confirmButton.setClickable(true);
                } else {
                    confirmButton.setClickable(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setConfirmData(String address, String receiveAddress, String value, String feeSeek) {
        ((TextView) confirmView.findViewById(R.id.tv_from_address)).setText(address);
        ((TextView) confirmView.findViewById(R.id.tv_to_address)).setText(receiveAddress);
        ((TextView) confirmView.findViewById(R.id.tv_transfer_value)).setText(value);
        ((TextView) confirmView.findViewById(R.id.tv_transfer_fee)).setText(feeSeek);
    }

    private void initAction() {
        confirmButton.setOnClickListener(view1 -> {
            confirmButton.setClickable(false);
            viewPager.setCurrentItem(1);
        });
        mRlConfirmClose.setOnClickListener(view1 -> dialog.dismiss());
        mRlTransferClose.setOnClickListener(view1 -> dialog.dismiss());
        pwdButton.setOnClickListener(view1 -> {
            if (listener != null) {
                String password = passwordEdit.getText().toString().trim();
                listener.send(password, progressBar);
            }
        });
    }

    public void setButtonClickAble(boolean able) {
        pwdButton.setClickable(able);
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public interface OnSendPwdListener {
        void send(String pwd, ProgressBar progressBar);
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
                container.addView(confirmView);
                return confirmView;
            } else {
                container.addView(pwdView);
                return pwdView;
            }

        }
    }

}
