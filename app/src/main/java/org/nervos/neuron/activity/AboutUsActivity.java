package org.nervos.neuron.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.nervos.neuron.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutUsActivity extends BaseActivity {

    private static final String GITHUB_URL = "https://github.com/cryptape/Neuron-Android";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abount_us);

        initView();
    }

    private void initView() {
        CircleImageView appImage = findViewById(R.id.app_photo);
        appImage.setImageResource(R.mipmap.ic_launcher);

        TextView versionText = findViewById(R.id.app_version);
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        findViewById(R.id.setting_source_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SimpleWebActivity.class);
                intent.putExtra(SimpleWebActivity.EXTRA_URL, GITHUB_URL);
                startActivity(intent);
            }
        });
    }

}
