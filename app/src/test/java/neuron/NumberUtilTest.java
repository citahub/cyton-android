package neuron;

import org.junit.Test;
import org.nervos.neuron.util.NumberUtil;

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
       assertEquals("1.0E-10", NumberUtil.getDecimal8ENotation(0.0000000001));
       assertEquals("0.00000001", NumberUtil.getDecimal8ENotation(0.00000001));
       assertEquals("1.00000001", NumberUtil.getDecimal8ENotation(1.00000001));
       assertEquals("1.12345678", NumberUtil.getDecimal8ENotation(1.123456789));
       assertEquals("1", NumberUtil.getDecimal8ENotation(1.0000000001));
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

}