package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import org.nervos.neuron.R;
import org.nervos.neuron.fragment.TransactionFragment;
import org.nervos.neuron.util.Blockies;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBHistoryUtil;
import org.nervos.neuron.util.permission.PermissionUtil;
import org.nervos.neuron.util.permission.RuntimeRationale;
import org.nervos.neuron.util.web.WebAppUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.nervos.neuron.activity.TransactionDetailActivity.EXTRA_TRANSACTION;

public class AddWebsiteActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;
    private RecyclerView recyclerView;
    private WebAppAdapter adapter = new WebAppAdapter();
    private List<String> webAppList = new ArrayList<>();
    private AppCompatEditText websiteEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_website);

        webAppList = DBHistoryUtil.getAllHistory(mActivity);
        initView();
        initRecycler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webAppList = DBHistoryUtil.getAllHistory(mActivity);
        adapter.notifyDataSetChanged();
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


    private void initRecycler() {
        recyclerView = findViewById(R.id.scan_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                gotoWebViewWithUrl(webAppList.get(position));
            }
        });

    }


    private void gotoWebViewWithUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(mActivity, "请输入正确的网页地址", Toast.LENGTH_SHORT).show();
        } else {
            websiteEdit.setText(url);
            DBHistoryUtil.saveHistory(mActivity, url);
            Intent intent = new Intent(mActivity, AppWebActivity.class);
            intent.putExtra(AppWebActivity.EXTRA_URL, url);
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


    class WebAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_ITEM = 1;
        public static final int VIEW_TYPE_EMPTY = 0;

        public OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            if (viewType == VIEW_TYPE_EMPTY) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_history_view, parent, false);
                return new RecyclerView.ViewHolder(view){};
            }
            WebAppViewHolder holder = new WebAppViewHolder(LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_web_app, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof WebAppViewHolder) {
                WebAppViewHolder viewHolder = (WebAppViewHolder)holder;
                viewHolder.appNameText.setText(webAppList.get(position));
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            if (webAppList.size() == 0) return 1;
            return webAppList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (webAppList.size() == 0) return VIEW_TYPE_EMPTY;
            return VIEW_TYPE_ITEM;
        }

        class  WebAppViewHolder extends RecyclerView.ViewHolder {
            TextView appNameText;
            public WebAppViewHolder (View view) {
                super(view);
                view.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, (int)v.getTag());
                    }
                });
                appNameText = view.findViewById(R.id.app_name);
            }
        }
    }


    private interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

}
