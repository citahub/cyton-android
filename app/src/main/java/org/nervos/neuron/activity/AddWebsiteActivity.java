package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.R;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.web.WebAppUtil;

public class AddWebsiteActivity extends BaseActivity {

    public static final String EXTRA_URL = "extra_url";
    private static final int REQUEST_CODE = 0x01;
    private RecyclerView recyclerView;
    private AppCompatEditText websiteEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_website);

        initView();
    }

    private void initView() {
        websiteEdit = findViewById(R.id.title_input);
        findViewById(R.id.title_bar_left).setOnClickListener(v -> finish());
        findViewById(R.id.title_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(mActivity)
                    .runtime().permission(Permission.Group.CAMERA)
                    .rationale(new RuntimeRationale())
                    .onGranted(permissions -> {
                        Intent intent = new Intent(mActivity, QrCodeActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                    })
                    .onDenied(permissions -> PermissionUtil.showSettingDialog(mActivity, permissions))
                    .start();

            }
        });

        recyclerView = findViewById(R.id.scan_history);

        websiteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                                && KeyEvent.ACTION_DOWN == event.getAction())) {
                    gotoWebViewWithUrl(websiteEdit.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });
    }

    private void gotoWebViewWithUrl(String url) {
        if (TextUtils.isEmpty(url) || !WebAppUtil.isHttpUrl(url)) {
            Toast.makeText(mActivity, "请输入正确的网页地址", Toast.LENGTH_SHORT).show();
        } else {
            websiteEdit.setText(url);
            Intent intent = new Intent(mActivity, AppWebActivity.class);
            intent.putExtra(EXTRA_URL, url);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (data != null && data.getExtras() != null) {
                Bundle bundle = data.getExtras();
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    gotoWebViewWithUrl(bundle.getString(CodeUtils.RESULT_STRING));
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(mActivity, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
