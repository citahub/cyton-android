package org.nervos.neuron.util.sensor;

/**
 * Created by BaojunCZ on 2018/10/12.
 */
public class SensorIDRandomUtils {

    public static String getID() {
        String x = "";
        for (int n = 0; n < 51; n++) {
            x += (int) (10 * (Math.random()));
        }
        x += System.currentTimeMillis();
        return x;
    }

}
