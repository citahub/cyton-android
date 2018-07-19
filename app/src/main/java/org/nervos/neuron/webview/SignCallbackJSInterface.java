package org.nervos.neuron.webview;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.nervos.neuron.util.LogUtil;
import org.nervos.neuron.util.NumberUtil;
import org.nervos.neuron.webview.item.Address;
import org.nervos.neuron.webview.item.Message;
import org.nervos.neuron.webview.item.Transaction;
import org.nervos.neuron.webview.item.TypedData;
import org.web3j.utils.Numeric;

public class SignCallbackJSInterface {

    private final WebView webView;
    @NonNull
    private final OnSignTransactionListener onSignTransactionListener;
    @NonNull
    private final OnSignMessageListener onSignMessageListener;
    @NonNull
    private final OnSignPersonalMessageListener onSignPersonalMessageListener;
    @NonNull
    private final OnSignTypedMessageListener onSignTypedMessageListener;

    public SignCallbackJSInterface(
            WebView webView,
            @NonNull OnSignTransactionListener onSignTransactionListener,
            @NonNull OnSignMessageListener onSignMessageListener,
            @NonNull OnSignPersonalMessageListener onSignPersonalMessageListener,
            @NonNull OnSignTypedMessageListener onSignTypedMessageListener) {
        this.webView = webView;
        this.onSignTransactionListener = onSignTransactionListener;
        this.onSignMessageListener = onSignMessageListener;
        this.onSignPersonalMessageListener = onSignPersonalMessageListener;
        this.onSignTypedMessageListener = onSignTypedMessageListener;
    }

    @JavascriptInterface
    public void signTransaction(
            int callbackId,
            String recipient,
            String value,
            String nonce,
            String gasLimit,    // quota
            String gasPrice,    // validUntilBlock
            String data,
            String chainId,
            String version,
            String chainType) {
        Transaction transaction = new Transaction(
                TextUtils.isEmpty(recipient) ? Address.EMPTY : new Address(recipient),
                null,
                value,
                gasLimit,
                gasPrice,
                NumberUtil.hexToLong(nonce, -1),
                data,
                NumberUtil.hexToLong(chainId, -1),
                NumberUtil.hexToInteger(version, 0),
                chainType,
                callbackId);
        onSignTransactionListener.onSignTransaction(transaction);
    }

    @JavascriptInterface
    public void signMessage(int callbackId, String data, String chainType) {
        Transaction transaction = new Transaction(data, chainType);
        webView.post(() -> onSignMessageListener.onSignMessage(new Message<>(transaction, getUrl(), callbackId)));
    }

    @JavascriptInterface
    public void signPersonalMessage(int callbackId, String data, String chainType) {
        Transaction transaction = new Transaction(data, chainType);
        LogUtil.d("signPersonalMessage: " + data + " " + chainType);
        webView.post(() -> onSignPersonalMessageListener.onSignPersonalMessage(
                new Message<>(transaction, getUrl(), callbackId)));
    }

    @JavascriptInterface
    public void signTypedMessage(int callbackId, String data) {
        webView.post(() -> {
            TrustProviderTypedData[] rawData = new Gson().fromJson(data, TrustProviderTypedData[].class);
            int len = rawData.length;
            TypedData[] typedData = new TypedData[len];
            for (int i = 0; i < len; i++) {
                typedData[i] = new TypedData(rawData[i].name, rawData[i].type, rawData[i].value);
            }
            onSignTypedMessageListener.onSignTypedMessage(new Message<>(typedData, getUrl(), callbackId));
        });
    }

    private String getUrl() {
        return webView == null ? "" : webView.getUrl();
    }

    private static class TrustProviderTypedData {
        public String name;
        public String type;
        public Object value;
    }
}
