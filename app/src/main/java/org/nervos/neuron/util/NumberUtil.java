package org.nervos.neuron.util;

import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class NumberUtil {

    public static String getDecimal_6(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.0000000");
        return fmt.format(value);
    }

    public static String getDecimal_4(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.0000000");
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

}
