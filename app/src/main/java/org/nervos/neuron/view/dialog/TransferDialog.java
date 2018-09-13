package org.nervos.neuron.view.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nervos.neuron.R;

import javax.annotation.Nullable;

/**
 * Created by BaojunCZ on 2018/9/13.
 */
public class TransferDialog {

    private BottomSheetDialog dialog;
    private View view;
    private View confirmView, pwdView;
    private ViewPager viewPager;
    private AppCompatButton confirmButton, pwdButton;
    private ImageView closeImage, closeImage1;
    private ProgressBar progressBar;
    private AppCompatEditText passwordEdit;
    private onSendPwdListener listener = null;

    public TransferDialog(@Nullable Activity context, onSendPwdListener listener) {
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
        closeImage = confirmView.findViewById(R.id.close_layout);
        closeImage1 = pwdView.findViewById(R.id.close_layout);
        progressBar = pwdView.findViewById(R.id.transfer_progress);
        passwordEdit = pwdView.findViewById(R.id.wallet_password_edit);
    }

    private void initData() {
        viewPager.setAdapter(new PageAdapter());
//        try {
//            Field mScroller = null;
//            mScroller = ViewPager.class.getDeclaredField("mScroller");
//            mScroller.setAccessible(true);
//            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(), 1500);
//            mScroller.set(viewPager, scroller);
//        } catch (NoSuchFieldException e) {
//
//        } catch (IllegalArgumentException e) {
//
//        } catch (IllegalAccessException e) {
//
//        }
    }

    public void setConfirmData(String address, String receiveAddress, String value, String feeSeek) {
        ((TextView) confirmView.findViewById(R.id.from_address)).setText(address);
        ((TextView) confirmView.findViewById(R.id.to_address)).setText(receiveAddress);
        ((TextView) confirmView.findViewById(R.id.transfer_value)).setText(value);
        ((TextView) confirmView.findViewById(R.id.transfer_fee)).setText(feeSeek);
    }

    private void initAction() {
        viewPager.setOnTouchListener((view, motionEvent) -> true);
        confirmButton.setOnClickListener(view1 -> viewPager.setCurrentItem(1));
        closeImage.setOnClickListener(view1 -> dialog.dismiss());
        closeImage1.setOnClickListener(view1 -> dialog.dismiss());
        pwdButton.setOnClickListener(view1 -> {
            if (listener != null) {
                String password = passwordEdit.getText().toString().trim();
                listener.send(password, progressBar);
            }
        });
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public interface onSendPwdListener {
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
