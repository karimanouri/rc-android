package com.cps.rotationdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

abstract class SensorDetector implements SensorEventListener {

    private final int[] sensorTypes;

    SensorDetector(int... sensorTypes) {
        this.sensorTypes = sensorTypes;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isSensorEventBelongsToPluggedTypes(sensorEvent)) {
            onSensorEvent(sensorEvent);
        }
    }

    int[] getSensorTypes() {
        return sensorTypes;
    }

    void onSensorEvent(SensorEvent sensorEvent) {

    }

    private boolean isSensorEventBelongsToPluggedTypes(SensorEvent sensorEvent) {
        for (int sensorType : sensorTypes) {
            if (sensorEvent.sensor.getType() == sensorType) {
                return true;
            }
        }
        return false;
    }
}

