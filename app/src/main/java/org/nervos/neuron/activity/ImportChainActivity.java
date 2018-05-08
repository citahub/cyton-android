package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import org.nervos.neuron.R;

public class ImportChainActivity extends AppCompatActivity {

    private AppCompatEditText importChainEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_chain);

        importChainEdit = findViewById(R.id.edit_import_chain);
        findViewById(R.id.button_import_chain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }
}
