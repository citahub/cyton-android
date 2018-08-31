package org.nervos.neuron.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;


public class NumberUtil {

    public static String getDecimalValid_2(double value) {
        long integer = (long)value;
        double decimal = value - integer;
        BigDecimal b = new BigDecimal(decimal);
        BigDecimal divisor = BigDecimal.ONE;
        MathContext mc = new MathContext(2);
        decimal = b.divide(divisor, mc).doubleValue();
        return getDecimal8ENotation(integer + decimal);
    }

    public static String getDecimal8ENotation(double value) {
        if (value < 1) {
            double decimal = value - (long)value;
            if (decimal < 0.00000001) {
                return String.valueOf(value);
            }
        }
        DecimalFormat fmt = new DecimalFormat("0.########");
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

    public static int hexToInteger(String input, int def) {
        Integer value = hexToInteger(input);
        return value == null ? def : value;
    }

    @Nullable
    public static Integer hexToInteger(String input) {
        try {
            return Integer.decode(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static long hexToLong(String input, int def) {
        Long value = hexToLong(input);
        return value == null ? def : value;
    }

    @Nullable
    public static Long hexToLong(String input) {
        try {
            return Long.decode(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Nullable
    public static BigInteger hexToBigInteger(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        try {
            boolean isHex = Numeric.containsHexPrefix(input);
            if (isHex) {
                input = Numeric.cleanHexPrefix(input);
            }
            return new BigInteger(input, isHex ? 16 : 10);
        } catch (NullPointerException | NumberFormatException ex) {
            return null;
        }
    }

    public static String toLowerCaseWithout0x(String hex) {
        return Numeric.cleanHexPrefix(hex).toLowerCase();
    }

    @NonNull
    public static BigInteger hexToBigInteger(String input, BigInteger def) {
        BigInteger value = hexToBigInteger(input);
        return value == null ? def : value;
    }


    @Nullable
    public static String hexToDecimal(@Nullable String value) {
        BigInteger result = hexToBigInteger(value);
        return result == null ? null : result.toString(10);
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

    public static BigInteger getWeiFromEth(double value) {
        return Convert.toWei(String.valueOf(value), Convert.Unit.ETHER).toBigInteger();
    }

    public static String getEthFromWeiForStringDecimal8(String value) {
        return getDecimal8ENotation(getEthFromWeiForDouble(value));
    }

    public static String getEthFromWeiForStringDecimal8(BigInteger value) {
        return getDecimal8ENotation(getEthFromWei(value));
    }

    public static double getEthFromWeiForDouble(String value) {
        if (TextUtils.isEmpty(value)) return 0.0;
        if (Numeric.containsHexPrefix(value)) {
            return getEthFromWei(Numeric.toBigInt(value));
        } else {
            return getEthFromWei(new BigInteger(value));
        }
    }

    public static double getEthFromWei(BigInteger value) {
        return Convert.fromWei(value.toString(), Convert.Unit.ETHER).doubleValue();
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
