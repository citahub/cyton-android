package org.nervos.neuron.view.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.nervos.neuron.R;
import org.nervos.neuron.view.button.FillButton;

/**
 * Created by BaojunCZ on 2018/9/11.
 */
public class SelectorDialog {

    private TextView titleText;
    private ImageView closeImg;
    private RecyclerView recyclerView;
    private FillButton okBtn;
    private Context context;
    private BottomSheetDialog dialog;
    private View view;

    public SelectorDialog(@NonNull Context context) {
        this.context = context;
        dialog = new BottomSheetDialog(context);
        view = LayoutInflater.from(context).inflate(R.layout.view_bottom_selector, null);
        dialog.setContentView(view);
        dialog.show();
        initView();
        initData();
        initAction();
    }

    private void initView() {
        titleText = view.findViewById(R.id.tv_title);
        closeImg = view.findViewById(R.id.iv_close);
        recyclerView = view.findViewById(R.id.recycler);
        okBtn = view.findViewById(R.id.btn_ok);
    }

    private void initData() {

    }

    private void initAction() {
        closeImg.setOnClickListener(view -> dialog.dismiss());
    }

    public void setTitleText(String title) {
        titleText.setText(title);
    }

    public void setOkBtn(String text) {
        okBtn.setText(text);
    }

    public void setOkListner(View.OnClickListener listner) {
        okBtn.setOnClickListener(listner);
    }

    public void setRecyclerView(RecyclerView.Adapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    public void dismiss() {
        dialog.dismiss();
    }

}
