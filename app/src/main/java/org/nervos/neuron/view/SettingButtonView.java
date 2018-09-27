package org.nervos.neuron.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.util.DipUtils;

/**
 * Created by 包俊 on 2018/7/30.
 */
public class SettingButtonView extends ConstraintLayout {

    private ImageView iconImage, openImage, switchImage, rightImage;
    private TextView nameText, rightText;
    private View line;
    private TypedArray ta;
    private openListener openListener = null;
    private switchListener switchListener = null;
    private ConstraintLayout root;
    private boolean switchStatus = false;
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
        switchImage = findViewById(R.id.iv_setting_switch);
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
        } else
            rightText.setVisibility(GONE);
        boolean switchButton = ta.getBoolean(R.styleable.SettingButtonView_switch_button, false);
        if (switchButton)
            switchImage.setVisibility(VISIBLE);
        else
            switchImage.setVisibility(GONE);
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

    public void setOpenListener(openListener openListener) {
        this.openListener = openListener;
    }

    public void setSwitchListener(switchListener switchListener) {
        this.switchListener = switchListener;
    }

    public void setSwitch(boolean is) {
        if (is) {
            switchStatus = true;
            switchImage.setImageResource(R.drawable.ic_setting_onoff_on);
        } else {
            switchStatus = false;
            switchImage.setImageResource(R.drawable.ic_setting_onoff_off);
        }
    }

    private void initAction() {
        root.setOnClickListener((view) -> {
            if (openListener != null)
                openListener.open();
        });
        switchImage.setOnClickListener((view) -> {
            if (switchListener != null) {
                switchListener.click(!switchStatus);
            }
        });
    }

    public interface openListener {
        void open();
    }

    public interface switchListener {
        void click(boolean is);
    }
}
