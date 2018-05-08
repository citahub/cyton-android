package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import org.nervos.neuron.R;

public class ExportChainActivity extends AppCompatActivity {

    private AppCompatEditText exportChainEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_chain);

        exportChainEdit = findViewById(R.id.edit_export_chain);
        findViewById(R.id.button_export_chain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
