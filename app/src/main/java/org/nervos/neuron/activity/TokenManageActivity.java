package org.nervos.neuron.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.nervos.neuron.R;
import org.nervos.neuron.service.http.HttpUrls;
import org.nervos.neuron.util.AddressUtil;
import org.nervos.neuron.view.TitleBar;
import org.nervos.neuron.event.TokenRefreshEvent;
import org.nervos.neuron.item.TokenEntity;
import org.nervos.neuron.item.TokenItem;
import org.nervos.neuron.util.FileUtil;
import org.nervos.neuron.util.db.DBTokenUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.web3j.crypto.Keys;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TokenManageActivity extends BaseActivity {

    private static final int REQUEST_CODE = 0x01;
    public static final int RESULT_CODE = 0x01;

    private TitleBar titleBar;
    private RecyclerView recyclerView;
    private List<TokenEntity> tokenList = new ArrayList<>();
    private TokenAdapter adapter = new TokenAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        initView();
        initData();
    }

    private void initData() {
        String tokens = FileUtil.loadRawFile(mActivity, R.raw.tokens_eth);
        Type type = new TypeToken<List<TokenEntity>>() {
        }.getType();
        tokenList = new Gson().fromJson(tokens, type);
        for (TokenEntity entity : tokenList) {
            entity.chainId = -1;
        }
        addCustomToken();
        adapter.notifyDataSetChanged();
    }

    private void addCustomToken() {
        List<TokenItem> customList = DBTokenUtil.getAllTokens(mActivity);
        if (customList != null && customList.size() > 0) {
            for (int i = 0; i < customList.size(); i++) {
                tokenList.add(i, new TokenEntity(customList.get(i)));       // add front of list
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        titleBar = findViewById(R.id.title);
        titleBar.setOnRightClickListener(new TitleBar.OnRightClickListener() {
            @Override
            public void onRightClick() {
                startActivityForResult(new Intent(mActivity, AddTokenActivity.class), REQUEST_CODE);
            }
        });
        titleBar.setOnLeftClickListener(new TitleBar.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                postTokenRefreshEvent();
                finish();
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TokenViewHolder holder = new TokenViewHolder(LayoutInflater.from(
                    mActivity).inflate(R.layout.item_token_info, parent,
                    false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenViewHolder) {
                TokenViewHolder viewHolder = (TokenViewHolder) holder;
                if (tokenList.get(position).logo == null ||
                        TextUtils.isEmpty(tokenList.get(position).logo.src)) {
                    String address = tokenList.get(position).address;
                    if (AddressUtil.isAddressValid(address))
                        address = Keys.toChecksumAddress(address);
                    RequestOptions options = new RequestOptions()
                            .error(R.drawable.ether_big);
                    Glide.with(mActivity)
                            .load(Uri.parse(HttpUrls.TOKEN_LOGO.replace("@address", address)))
                            .apply(options)
                            .into(viewHolder.tokenImage);
                } else {
                    Glide.with(mActivity)
                            .load(Uri.parse(tokenList.get(position).logo.src))
                            .into(viewHolder.tokenImage);
                }
                viewHolder.tokenName.setText(tokenList.get(position).name);
                viewHolder.tokenSymbol.setText(tokenList.get(position).symbol);
                viewHolder.tokenContractAddress.setText(tokenList.get(position).address);
                tokenList.get(position).isSelected =
                        DBWalletUtil.checkTokenInCurrentWallet(mActivity, tokenList.get(position).symbol);
                viewHolder.tokenSelectImage.setImageResource(tokenList.get(position).isSelected ?
                        R.drawable.ic_setting_onoff_on : R.drawable.ic_setting_onoff_off);
                viewHolder.tokenSelectImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int mPosition = holder.getAdapterPosition();
                        tokenList.get(mPosition).isSelected = !tokenList.get(mPosition).isSelected;
                        viewHolder.tokenSelectImage.setImageResource(tokenList.get(mPosition).isSelected ?
                                R.drawable.ic_setting_onoff_on : R.drawable.ic_setting_onoff_off);
                        if (tokenList.get(mPosition).isSelected) {
                            DBWalletUtil.addTokenToCurrentWallet(mActivity,
                                    new TokenItem(tokenList.get(mPosition)));
                        } else {
                            DBWalletUtil.deleteTokenFromCurrentWallet(mActivity,
                                    new TokenItem(tokenList.get(mPosition)));
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


        class TokenViewHolder extends RecyclerView.ViewHolder {
            ImageView tokenImage;
            TextView tokenName;
            TextView tokenSymbol;
            TextView tokenContractAddress;
            ImageView tokenSelectImage;

            public TokenViewHolder(View view) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            initData();
        }
    }
}
