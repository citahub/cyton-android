package org.nervos.neuron.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import org.nervos.neuron.R;

public class QuickBackupActivity extends BaseActivity {

    private AppCompatEditText passwordEdit;
    private AppCompatEditText rePasswordEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_backup);

        passwordEdit = findViewById(R.id.edit_backup_password);
        rePasswordEdit = findViewById(R.id.edit_backup_repassword);

        findViewById(R.id.quick_backup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backup();
            }
        });
    }


    private void backup() {

    }
}
