package org.nervos.neuron.util.fingerprint;

/**
 * Created by 包俊 on 2018/7/30.
 */

public interface AuthenticateResultCallback {
    void onAuthenticationError(String errorMsg);
    void onAuthenticationSucceeded();
    void onAuthenticationFailed();
}
