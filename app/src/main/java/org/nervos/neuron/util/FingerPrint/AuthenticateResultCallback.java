package org.nervos.neuron.util.FingerPrint;

/**
 * 指纹认证回调
 * Created by 包俊 on 2018/7/30.
 */

public interface AuthenticateResultCallback {
    void onAuthenticationError(String errorMsg);
    void onAuthenticationSucceeded();
    void onAuthenticationFailed();
}
