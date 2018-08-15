package org.nervos.neuron.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;

/**
 * Created by 包俊 on 2018/7/30.
 */
public class SettingButtonView extends ConstraintLayout {

    private ImageView iconImage, openImage, switchImage;
    private TextView nameText, otherText;
    private TypedArray ta;
    private openListener openListener = null;
    private switchListener switchListener = null;
    private ConstraintLayout root;
    private boolean switchStatus = false;

    public SettingButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        otherText = findViewById(R.id.tv_other);
        openImage = findViewById(R.id.iv_setting_open);
        root = findViewById(R.id.root);
        switchImage = findViewById(R.id.iv_setting_switch);
    }

    private void initData() {
        String text = ta.getString(R.styleable.SettingButtonView_name);
        nameText.setText(text);
        int icon = ta.getResourceId(R.styleable.SettingButtonView_icon, -1);
        iconImage.setBackgroundResource(icon);
        boolean other1 = ta.getBoolean(R.styleable.SettingButtonView_other1, false);
        if (other1)
            otherText.setVisibility(VISIBLE);
        else
            otherText.setVisibility(GONE);
        boolean switchButton = ta.getBoolean(R.styleable.SettingButtonView_switch_button, false);
        if (switchButton)
            switchImage.setVisibility(VISIBLE);
        else
            switchImage.setVisibility(GONE);
        ta.recycle();
    }

    //set other1 text
    public void setOther1Text(String text) {
        otherText.setText(text);
    }

    public void setOpenListener(openListener openListener) {
        this.openListener = openListener;
        openImage.setVisibility(VISIBLE);
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
