package com.cryptape.cita_wallet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.DipUtils;

/**
 * Created by BaojunCZ on 2018/7/30.
 */
public class SettingButtonView extends ConstraintLayout {

    private ImageView iconImage, openImage, rightImage;
    private Switch endSwitch;
    private TextView nameText, rightText;
    private View line;
    private TypedArray ta;
    private OnClickListener onClickListener = null;
    private OnCheckedChangeListener onCheckedChangeListener = null;
    private ConstraintLayout root;
    private Context context;

    public SettingButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingButtonView);
        this.ta = ta;
        LayoutInflater.from(context).inflate(R.layout.view_setting_button, this);
        initView();
        initData();
        initAction();
    }

    private void initView() {
        iconImage = findViewById(R.id.iv_setting_icon);
        nameText = findViewById(R.id.tv_setting);
        rightText = findViewById(R.id.tv_right);
        openImage = findViewById(R.id.iv_setting_open);
        root = findViewById(R.id.root);
        endSwitch = findViewById(R.id.iv_setting_switch);
        rightImage = findViewById(R.id.iv_setting_right);
        line = findViewById(R.id.line);
    }

    private void initData() {
        String text = ta.getString(R.styleable.SettingButtonView_name);
        nameText.setText(text);
        int icon = ta.getResourceId(R.styleable.SettingButtonView_icon, -1);
        if (icon == -1) {
            iconImage.setVisibility(GONE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) nameText.getLayoutParams();
            layoutParams.leftMargin = DipUtils.dip2px(context, 15);
            nameText.setLayoutParams(layoutParams);
        } else {
            iconImage.setVisibility(VISIBLE);
            iconImage.setBackgroundResource(icon);
        }
        String rightStr = ta.getString(R.styleable.SettingButtonView_right_txt);
        if (!TextUtils.isEmpty(rightStr)) {
            rightText.setVisibility(VISIBLE);
            rightText.setText(rightStr);
        } else{
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) nameText.getLayoutParams();
            layoutParams.width=0;
            nameText.setLayoutParams(layoutParams);
            rightText.setVisibility(GONE);
        }
        boolean switchButton = ta.getBoolean(R.styleable.SettingButtonView_switch_button, false);
        endSwitch.setVisibility(switchButton ? VISIBLE : GONE);

        boolean rightArrow = ta.getBoolean(R.styleable.SettingButtonView_right_arrow, true);
        if (rightArrow) {
            openImage.setVisibility(VISIBLE);
        } else {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) rightText.getLayoutParams();
            layoutParams.rightMargin = DipUtils.dip2px(context, 15);
            rightText.setLayoutParams(layoutParams);
            openImage.setVisibility(GONE);
        }
        int rightImg = ta.getResourceId(R.styleable.SettingButtonView_right_image, -1);
        if (rightImg != -1) {
            rightImage.setVisibility(VISIBLE);
            rightImage.setImageResource(rightImg);
        } else
            rightImage.setVisibility(GONE);
        boolean bottomLine = ta.getBoolean(R.styleable.SettingButtonView_bottom_line, true);
        if (bottomLine)
            line.setVisibility(VISIBLE);
        else
            line.setVisibility(GONE);
        ta.recycle();
    }

    //set other1 text
    public void setRightText(String text) {
        rightText.setText(text);
        rightText.setVisibility(VISIBLE);
    }

    public void setRightImage(int res) {
        rightImage.setImageResource(res);
    }

    public void setRightImageShow(boolean show) {
        if (show) rightImage.setVisibility(VISIBLE);
        else rightImage.setVisibility(GONE);
    }

    public void setNameText(String text) {
        nameText.setText(text);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setSwitchCheckedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setSwitchCheck(boolean isChecked) {
        endSwitch.setChecked(isChecked);
    }

    private void initAction() {
        root.setOnClickListener((view) -> {
            if (onClickListener != null)
                onClickListener.open();
        });

        endSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(isChecked);
                }
            }
        });
    }

    public interface OnClickListener {
        void open();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(boolean isChecked);
    }
}
