package com.cryptape.cita_wallet.util;

import android.support.annotation.Nullable;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.regex.Pattern;


public class NumberUtil {

    public static String getDecimalValid_2(double value) {
        return String.valueOf(new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    public static String getDecimalValid_8(double value) {
        if (checkDecimal8(value)) {
            return String.valueOf(value);
        }
        DecimalFormat fmt = new DecimalFormat("0.########");
        fmt.setRoundingMode(RoundingMode.FLOOR);
        BigDecimal big = BigDecimal.valueOf(value);
        return fmt.format(big);
    }

    public static boolean checkDecimal8(double value) {
        if (value < 1) {
            double decimal = value - (long) value;
            if (decimal < 0.00000001 && decimal > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * WARNING: not very sure
     *
     * @param value
     * @return
     */
    public static boolean isHex(String value) {
        if (Strings.isEmpty(value)) {
            return false;
        }
        value = Numeric.cleanHexPrefix(value);
        for (int i = 0; i < value.length(); i++) {
            if (('0' > value.charAt(i) || '9' < value.charAt(i))
                    && ('A' > value.charAt(i) || 'F' < value.charAt(i))
                    && ('a' > value.charAt(i) || 'f' < value.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    public static boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static String hexToUtf8(String hex) {
        hex = Numeric.cleanHexPrefix(hex);
        ByteBuffer buff = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 2) {
            buff.put((byte) Integer.parseInt(hex.substring(i, i + 2), 16));
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
        if (Strings.isEmpty(input)) {
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


    @Nullable
    public static String hexToDecimal(@Nullable String value) {
        BigInteger result = hexToBigInteger(value);
        return result == null ? null : result.toString(10);
    }

    @Nullable
    public static String decimalToHex(@Nullable String value) {
        BigInteger result = new BigInteger(value);
        return Numeric.prependHexPrefix(result.toString(16));
    }

    public static String utf8ToHex(String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        for (byte x : bytes) {
            sb.append(Integer.toHexString(x & 0xFF));
        }
        return sb.toString();
    }

    public static BigInteger getWeiFromEth(String value) {
        return Convert.toWei(value, Convert.Unit.ETHER).toBigInteger();
    }


    public static String getEthFromWeiForStringDecimal8(BigInteger value) {
        return getDecimalValid_8(getEthFromWei(value));
    }

    public static double getEthFromWeiForDouble(String hex) {
        if (Strings.isEmpty(hex))
            return 0.0;
        hex = Numeric.cleanHexPrefix(hex);
        return getEthFromWei(Numeric.toBigInt(hex));
    }

    public static String getEthFromWeiForString(String hex) {
        if (Strings.isEmpty(hex))
            return "0";
        return String.valueOf(getEthFromWeiForDouble(hex));
    }

    public static double getEthFromWei(BigInteger value) {
        return Convert.fromWei(value.toString(), Convert.Unit.ETHER).doubleValue();
    }

    public static String getGWeiFromWeiForString(BigInteger num) {
        return Convert.fromWei(num.toString(), Convert.Unit.GWEI).toString();
    }

    public static BigInteger getWeiFromGWeiForBigInt(double num) {
        return Convert.toWei(BigDecimal.valueOf(num), Convert.Unit.GWEI).toBigInteger();
    }


    public static String divideDecimalSub(BigDecimal value, int decimal) {
        return getDecimalValid_8(value.divide(BigDecimal.TEN.pow(decimal), decimal, BigDecimal.ROUND_FLOOR).doubleValue());
    }

    public static boolean isPasswordOk(String password) {
        int len = password.length();
        if (len < 8)
            return false;
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
