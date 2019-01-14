package com.cryptape.cita_wallet.util.qrcode;

import com.cryptape.cita_wallet.util.AddressUtil;
import com.cryptape.cita_wallet.util.KeyStoreUtil;

import java.util.regex.Pattern;

/**
 * Created by BaojunCZ on 2018/8/29.
 */
public class QRResultCheck {
    public static int check(String msg) {
        if (AddressUtil.isAddressValid(msg)) {
            return CodeUtils.STRING_ADDRESS;
        } else if (KeyStoreUtil.isKeyStore(msg)) {
            return CodeUtils.STRING_KEYSTORE;
        } else if (isUrl(msg)) {
            return CodeUtils.STRING_WEB;
        } else if (isPrivateKey(msg)) {
            return CodeUtils.STRING_PRIVATE_KEY;
        } else {
            return CodeUtils.STRING_UNVALID;
        }
    }

    public static boolean isUrl(String msg) {
        Pattern pattern = Pattern
                .compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
        return pattern.matcher(msg).matches();
    }

    public static boolean isPrivateKey(String msg) {
        if (msg.startsWith("0x"))
            msg = msg.replace("0x", "");
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{64}");
        return pattern.matcher(msg).matches();
    }
}
