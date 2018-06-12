package org.nervos.neuron.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String getDecimal_6(Double value) {
        DecimalFormat fmt = new DecimalFormat("##0.0000000");
        return fmt.format(value);
    }

}
