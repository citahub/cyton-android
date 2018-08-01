package org.nervos.neuron.activity;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;

public class AdvancedSetupActivity extends NBaseActivity {

    private TitleBar titleBar;
    private LinearLayout gasLayout;
    private RelativeLayout quotaLayout;
    private SeekBar gasPriceSeekBar, gasLimitSeekBar, quotaSeekBar;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_advanced_setup;
    }

    @Override
    protected void initView() {
        titleBar = findViewById(R.id.title);

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initAction() {
        titleBar.setOnRightClickListener(() -> {
//            setResult();
            finish();
        });
    }


}
