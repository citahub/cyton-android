package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import org.nervos.neuron.R;

public class AddAppWebsiteActivity extends BaseActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app_website);

        initView();
    }

    private void initView() {
        findViewById(R.id.title_bar_left).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.scan_history);
    }

}
