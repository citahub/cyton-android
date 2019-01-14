package cyton;

import org.junit.Test;
import com.cryptape.cita_wallet.util.CurrencyUtil;
import com.cryptape.cita_wallet.util.NumberUtil;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link NumberUtil}
 */
public class NumberUtilTest {

    @Test
    public void testDecimalValid_2() {
        assertEquals("1.23", NumberUtil.getDecimalValid_2(1.2345));
        assertEquals("1.0", NumberUtil.getDecimalValid_2(1.0045));
        assertEquals("1.01", NumberUtil.getDecimalValid_2(1.0065));
        assertEquals("0.01", NumberUtil.getDecimalValid_2(0.0065));
        assertEquals("0.0", NumberUtil.getDecimalValid_2(0.0045));
    }

    @Test
    public void testDecimal8ENotation() {
        assertEquals("1.0E-10", NumberUtil.getDecimalValid_8(0.0000000001));
        assertEquals("0.00000001", NumberUtil.getDecimalValid_8(0.00000001));
        assertEquals("1.00000001", NumberUtil.getDecimalValid_8(1.00000001));
        assertEquals("1.12345678", NumberUtil.getDecimalValid_8(1.123456789));
        assertEquals("1", NumberUtil.getDecimalValid_8(1.0000000001));
    }

    @Test
    public void testIsHex() {
        assertTrue(NumberUtil.isHex("0x123"));
        assertTrue(NumberUtil.isHex("123"));
        assertTrue(NumberUtil.isHex("0xabc12df"));
        assertTrue(NumberUtil.isHex("abc12df"));
        assertFalse(NumberUtil.isHex("0xabc12wer"));
        assertFalse(NumberUtil.isHex("abc12wer"));
        assertFalse(NumberUtil.isHex("0xabc12@#"));
        assertFalse(NumberUtil.isHex("0xabc12@#"));
    }


    @Test
    public void testIsNumeric() {
        assertFalse(NumberUtil.isNumeric("0x123"));
        assertTrue(NumberUtil.isNumeric("123"));
        assertFalse(NumberUtil.isNumeric("12.087"));
    }

    @Test
    public void testHexToUtf8() {
        assertEquals("hello", NumberUtil.hexToUtf8("0x68656c6c6f"));
    }

    @Test
    public void testUtf8ToHex() {
        assertEquals("68656c6c6f", NumberUtil.utf8ToHex("hello"));
    }

    @Test
    public void testHexToDecimal() {
        assertEquals(Integer.valueOf(4660), NumberUtil.hexToInteger("0x1234"));
        assertEquals(Long.valueOf(305419896), NumberUtil.hexToLong("0x12345678"));
        assertEquals(new BigInteger("78187493520"), NumberUtil.hexToBigInteger("0x1234567890"));
        assertEquals("4660", NumberUtil.hexToDecimal("0x1234"));
        assertEquals("0x1234", NumberUtil.decimalToHex("4660"));
    }

    @Test
    public void testToLowerCaseWithout0x() {
        assertEquals("12a3abce", NumberUtil.toLowerCaseWithout0x("0x12A3aBcE"));
    }


    @Test
    public void testGetEthFromWeiForString() {
        assertEquals("1.3117672900331703", NumberUtil.getEthFromWeiForString("0x1234556676676788"));
        assertEquals("1.31176729", NumberUtil.getEthFromWeiForStringDecimal8(Numeric.toBigInt("0x1234556676676788")));
    }


    @Test
    public void testIsPasswordOk() {
        assertTrue(NumberUtil.isPasswordOk("123sddASD"));
        assertTrue(NumberUtil.isPasswordOk("1234sdhd#"));
        assertTrue(NumberUtil.isPasswordOk("123&ASDDD"));
        assertTrue(NumberUtil.isPasswordOk("@sddASDDD"));

        assertFalse(NumberUtil.isPasswordOk("123sdddss"));
        assertFalse(NumberUtil.isPasswordOk("123456ASDF"));
        assertFalse(NumberUtil.isPasswordOk("123456$$$$"));
        assertFalse(NumberUtil.isPasswordOk("123456"));
    }

    @Test
    public void testFmtMicrometer() {
        assertEquals("100,000,000", CurrencyUtil.fmtMicrometer("100000000"));
        assertEquals("0.00101", CurrencyUtil.fmtMicrometer("0.00101"));
        assertEquals("10.1", CurrencyUtil.fmtMicrometer("10.1"));
        assertEquals("1,000.1", CurrencyUtil.fmtMicrometer("1000.1"));
    }
}