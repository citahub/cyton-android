package org.nervos.neuron.util.sensor

/**
 * Created by BaojunCZ on 2018/10/12.
 */
object SensorIDRandomUtils {

    val id: String
        get() {
            var x = ""
            for (n in 0..50) {
                x += (10 * Math.random()).toInt()
            }
            x += System.currentTimeMillis()
            return x
        }

    val ipId:String
        get() {
            var x = ""
            for (n in 0..63) {
                x += (10 * Math.random()).toInt()
            }
            return x
        }

}
