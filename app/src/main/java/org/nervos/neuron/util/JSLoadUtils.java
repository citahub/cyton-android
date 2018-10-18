package org.nervos.neuron.util;

/**
 * Created by BaojunCZ on 2018/10/18.
 */
public class JSLoadUtils {
    public static String loadFunc(final String func) {
        return "javascript:" + func + "()";
    }

    public static String loadFunc(final String func, final String arg1) {
        return "javascript:" + func + "('" + arg1 + "')";
    }

    public static String loadFunc(final String func, final String arg1, final String arg2) {
        return "javascript:" + func + "('" + arg1 + "','" + arg2 + "')";
    }
}
