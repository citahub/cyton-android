package com.cryptape.cita_wallet.util.fingerprint;

/**
 * Created by BaojunCZ on 2018/7/30.
 */

public class NotSupportFingerprintException extends RuntimeException {

    public NotSupportFingerprintException() {
        super();
    }

    public NotSupportFingerprintException(String message) {
        super(message);
    }

    public NotSupportFingerprintException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportFingerprintException(Throwable cause) {
        super(cause);
    }
}
