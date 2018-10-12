package org.nervos.neuron.view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.nervos.neuron.R;
import org.nervos.neuron.util.ScreenUtils;

/**
 * Created by BaojunCZ on 2018/10/12.
 */
public class WebErrorView extends ConstraintLayout {

    private WebErrorViewImpl impl;

    public WebErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_web_error, this);
        ImageView iv = findViewById(R.id.iv);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (ScreenUtils.getScreenWidth(context) * 0.73), (int) (ScreenUtils.getScreenWidth(context) * 0.73 / 1.4723));
        params.topMargin = (int) (ScreenUtils.getScreenHeight(context) * 0.24);
        iv.setLayoutParams(params);
        findViewById(R.id.btn).setOnClickListener((view) -> {
            impl.click();
        });
    }

    public void setImpl(WebErrorViewImpl impl) {
        this.impl = impl;
    }

    public interface WebErrorViewImpl {
        void click();
    }
}
