package com.cryptape.cita_wallet.util.fingerprint;

/**
 * Created by BaojunCZ on 2018/7/30.
 */

public interface AuthenticateResultCallback {
    void onAuthenticationError(String errorMsg);
    void onAuthenticationSucceeded();
    void onAuthenticationFailed();
}
