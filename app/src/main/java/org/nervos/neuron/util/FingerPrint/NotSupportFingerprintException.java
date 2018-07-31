package org.nervos.neuron.util.FingerPrint;

/**
 * 不支持指纹异常
 * Created by 包俊 on 2018/7/30.
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
