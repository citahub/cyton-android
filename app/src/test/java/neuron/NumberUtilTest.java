package neuron;

import org.junit.Test;
import org.nervos.neuron.util.NumberUtil;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link NumberUtil}
 */
public class NumberUtilTest {

    @Test
    public void testDecimalValid_2() {
        assertEquals("1.23", NumberUtil.getDecimalValid_2(1.2345));
//        assertEquals("1.00", NumberUtil.getDecimalValid_2(1.0045));

        BigDecimal bg = new BigDecimal(1.0085);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.print(f1);

    }

}