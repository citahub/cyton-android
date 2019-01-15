package com.cryptape.cita_wallet.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cryptape.cita_wallet.R;
import com.cryptape.cita_wallet.util.DipUtils;

/**
 * Created by BaojunCZ on 2018/9/14.
 */
public class WebMenuPopupWindow extends PopupWindow {

    private View pop;
    private WebMenuListener listener = null;

    public WebMenuPopupWindow(Context context) {
        super(context);
        pop = LayoutInflater.from(context).inflate(R.layout.popupwindow_web_menu, null);
        this.setContentView(pop);
        this.setWidth(DipUtils.dip2px(context, 150));
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        initAction();
    }

    public void setListener(WebMenuListener listener) {
        this.listener = listener;
    }

    public void initAction() {
        pop.findViewById(R.id.menu_collect).setOnClickListener(view -> {
            if (listener != null)
                listener.collect(this);
        });
        pop.findViewById(R.id.menu_reload).setOnClickListener(view -> {
            if (listener != null)
                listener.reload(this);
        });
    }

    public void setCollectText(String text) {
        pop.post(() -> ((TextView) pop.findViewById(R.id.menu_collect)).setText(text));
    }

    public interface WebMenuListener {
        void reload(PopupWindow pop);

        void collect(WebMenuPopupWindow pop);
    }

}
