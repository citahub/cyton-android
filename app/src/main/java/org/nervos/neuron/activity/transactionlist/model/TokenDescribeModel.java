package org.nervos.neuron.activity.transactionlist.model;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.nervos.neuron.item.EthErc20TokenInfoItem;
import org.nervos.neuron.util.url.HttpUrls;
import org.nervos.neuron.service.http.HttpService;
import org.nervos.neuron.util.AddressUtil;
import org.web3j.crypto.Keys;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by BaojunCZ on 2018/10/9.
 */
public class TokenDescribeModel {

    private TokenDescribeModelImpl listener;

    public TokenDescribeModel(TokenDescribeModelImpl listener) {
        this.listener = listener;
    }

    public void get(String address) {
        if (AddressUtil.isAddressValid(address))
            address = Keys.toChecksumAddress(address);
        String url = String.format(HttpUrls.TOKEN_DESC, address);
        final Request ethRequest = new Request.Builder().url(url).build();
        Call ethCall = HttpService.getHttpClient().newCall(ethRequest);
        ethCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.error();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String body = response.body().string();
                    response.close();
                    if (!TextUtils.isEmpty(body)) {
                        EthErc20TokenInfoItem item = new Gson().fromJson(body, EthErc20TokenInfoItem.class);
                        listener.success(item);
                    } else {
                        listener.error();
                    }
                } catch (Exception e) {
                    listener.error();
                    e.printStackTrace();
                }
            }
        });
    }

    public interface TokenDescribeModelImpl {
        void success(EthErc20TokenInfoItem item);

        void error();
    }

}
