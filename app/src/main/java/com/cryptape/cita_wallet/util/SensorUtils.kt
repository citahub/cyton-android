package com.cryptape.cita_wallet.util

import android.content.Context
import android.hardware.*

/**
 * Created by BaojunCZ on 2018/11/15.
 */
class SensorUtils(var context: Context) : SensorEventListener {

    companion object {
        val INTERVAL_GAME = "game"
        val INTERVAL_UI = "ui"
        val INTERVAL_NORMAL = "nomal"
    }

    private var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var r = FloatArray(9)
    private var values = FloatArray(5)
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var motionListener: OnMotionListener? = null
    private var gyroscopeListener: OnGyroscopeListener? = null

    fun startDeviceMotionListening(interval: Int, motionListener: OnMotionListener) {
        this.motionListener = motionListener
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), interval)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), interval)
    }

    fun startGyroscopeListening(interval: Int, gyroscopeListener: OnGyroscopeListener) {
        this.gyroscopeListener = gyroscopeListener
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), interval)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    private fun handleValue() {
        if (gravity != null && geomagnetic != null) {
            if (SensorManager.getRotationMatrix(r, values, gravity, geomagnetic)) {
                SensorManager.getOrientation(r, values)
                motionListener!!.motionListener(values)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event!!.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values
                handleValue()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values
                handleValue()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeListener!!.gyroscopeListener(event.values)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    interface OnMotionListener {
        fun motionListener(values: FloatArray)

    }

    interface OnGyroscopeListener {
        fun gyroscopeListener(values: FloatArray)
    }

}