package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;

public class TokenManageActivity extends AppCompatActivity {

    private TitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        initView();
    }

    private void initView() {
        titleBar = findViewById(R.id.title);
        titleBar.setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivity(new Intent(TokenManageActivity.this, AddTokenActivity.class));

            }
        });
    }
}
