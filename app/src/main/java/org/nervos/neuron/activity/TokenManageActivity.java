package org.nervos.neuron.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.fragment.WalletFragment;
import org.nervos.neuron.item.TokenEntity;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.item.WalletItem;
import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.web.WebAppUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TokenManageActivity extends BaseActivity {

    private TitleBar titleBar;
    private RecyclerView recyclerView;
    private List<TokenEntity> tokenList = new ArrayList<>();
    private List<String> tokenNames = new ArrayList<>();
    private TokenAdapter adapter = new TokenAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        initView();
        initData();
    }

    private void initData() {
        tokenNames = DBWalletUtil.getAllWalletName(mActivity);
        String tokens = WebAppUtil.getFileFromAsset(mActivity, "tokens-eth.json");
        Type type = new TypeToken<List<TokenEntity>>() {}.getType();
        tokenList = new Gson().fromJson(tokens, type);
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        titleBar = findViewById(R.id.title);
        titleBar.setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivity(new Intent(mActivity, AddTokenActivity.class));
            }
        });
        titleBar.setOnLeftClickListener(new TitleBar.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                finish();
                postTokenRefreshEvent();
            }
        });
        recyclerView = findViewById(R.id.token_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(adapter);
    }

    private void postTokenRefreshEvent() {
        EventBus.getDefault().post(new TokenRefreshEvent());
    }

    class TokenAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    mActivity).inflate(R.layout.item_token_info, parent,
                    false));
            return holder;
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder)holder;
                if (TextUtils.isEmpty(tokenList.get(position).logo.src)) {
                    viewHolder.tokenImage.setImageResource(R.drawable.ether_big);
                } else {
                    viewHolder.tokenImage.setImageURI(Uri.parse(tokenList.get(position).logo.src));
                }
                viewHolder.tokenName.setText(tokenList.get(position).name);
                viewHolder.tokenSymbol.setText(tokenList.get(position).symbol);
                viewHolder.tokenContractAddress.setText(tokenList.get(position).address);
                tokenList.get(position).isSelected =
                        DBWalletUtil.checkTokenInCurrentWallet(mActivity, tokenList.get(position).symbol);
                viewHolder.tokenSelectImage.setImageResource(tokenList.get(position).isSelected?
                        R.drawable.circle_selected:R.drawable.circle_unselect);
                viewHolder.tokenSelectImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tokenList.get(position).isSelected = !tokenList.get(position).isSelected;
                        viewHolder.tokenSelectImage.setImageResource(tokenList.get(position).isSelected?
                                R.drawable.circle_selected:R.drawable.circle_unselect);
                        if (tokenList.get(position).isSelected) {
                            DBWalletUtil.addTokenToCurrentWallet(mActivity,
                                    new TokenItem(tokenList.get(position)));
                        } else {
                            DBWalletUtil.deleteTokenFromCurrentWallet(mActivity,
                                    new TokenItem(tokenList.get(position)));
                        }
                    }
                });
                viewHolder.itemView.setTag(position);
            }
        }

        @Override
        public int getItemCount() {
            return tokenList.size();
        }


        class  TokenViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView tokenImage;
            TextView tokenName;
            TextView tokenSymbol;
            TextView tokenContractAddress;
            ImageView tokenSelectImage;

            public TokenViewHolder (View view) {
                super(view);
                tokenImage = view.findViewById(R.id.token_image);
                tokenName = view.findViewById(R.id.token_name_text);
                tokenSymbol = view.findViewById(R.id.token_symbol_text);
                tokenContractAddress = view.findViewById(R.id.token_contract_address);
                tokenSelectImage = view.findViewById(R.id.image_token_select);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            postTokenRefreshEvent();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
