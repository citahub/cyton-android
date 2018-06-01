package org.nervos.neuron.util;

public class NumberUtil {

    public static String getDecimal_6(Double value) {
        return String.valueOf((int)(value * 1000_000)/1000000.0);
    }

}
