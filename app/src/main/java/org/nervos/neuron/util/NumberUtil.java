package org.nervos.neuron.util;

import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NumberUtil {

    public static String getDecimal_6(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.000000");
        return fmt.format(value);
    }

    public static String getDecimal_4(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.0000");
        return fmt.format(value);
    }

    public static String getDecimal_2(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.00");
        return fmt.format(value);
    }

    public static String hexToUtf8(String hex) {
        hex = Numeric.cleanHexPrefix(hex);
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);
        return cb.toString();
    }

    public static String utf8ToHex(String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (byte x: bytes) {
            sb.append(Integer.toHexString(x & 0xFF));
        }
        return sb.toString();
    }

    public static boolean isPasswordOk(String password) {
        int len = password.length();
        if (len < 8) return false;
        int flag = 0;
        for (int i = 0; i < len; i++) {
            char c = password.charAt(i);
            if (c >= 'a' & c <= 'z') {
                flag |= 0b0001;
            } else if (c >= 'A' & c <= 'Z') {
                flag |= 0b0010;
            } else if (c >= '0' & c <= '9') {
                flag |= 0b0100;
            } else if ((c >= '!' & c <= '/') || (c >= ':' & c <= '@')
                    || (c >= '[' & c <= '`') || (c >= '{' & c <= '~')) {
                flag |= 0b1000;
            } else {
                return false;
            }
        }
        return Integer.bitCount(flag) >= 3;
    }

}
