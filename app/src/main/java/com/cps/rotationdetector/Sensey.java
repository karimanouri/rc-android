package com.cps.rotationdetector;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Sensey {

    private static class LazyHolder {

        private static final Sensey INSTANCE = new Sensey();
    }

    /**
     * The constant SAMPLING_PERIOD_FASTEST.
     */
    public static final int SAMPLING_PERIOD_FASTEST = SensorManager.SENSOR_DELAY_FASTEST;

    /**
     * The constant SAMPLING_PERIOD_GAME.
     */
    public static final int SAMPLING_PERIOD_GAME = SensorManager.SENSOR_DELAY_GAME;

    /**
     * The constant SAMPLING_PERIOD_NORMAL.
     */
    public static final int SAMPLING_PERIOD_NORMAL = SensorManager.SENSOR_DELAY_NORMAL;

    /**
     * The constant SAMPLING_PERIOD_UI.
     */
    public static final int SAMPLING_PERIOD_UI = SensorManager.SENSOR_DELAY_UI;

    private final Map<Object, SensorDetector> defaultSensorsMap = new HashMap<>();

    private int samplingPeriod = SAMPLING_PERIOD_NORMAL;

    private SensorManager sensorManager;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Sensey getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Sensey() {
    }

    /**
     * Init the lib
     *
     * @param context        the context
     * @param samplingPeriod the sampling period
     */
    public void init(Context context, int samplingPeriod) {
        init(context);
        this.samplingPeriod = samplingPeriod;
    }

    /**
     * Init the lib
     *
     * @param context the context
     */
    public void init(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Start rotation angle detection.
     *
     * @param rotationAngleListener the rotation angle listener
     */
    public void startRotationAngleDetection(RotationAngleDetector.RotationAngleListener rotationAngleListener) {
        startLibrarySensorDetection(new RotationAngleDetector(rotationAngleListener),
                rotationAngleListener);
    }

    /**
     * Stop.
     */
    public void stop() {
        this.sensorManager = null;
    }

    /**
     * Stop rotation angle detection.
     *
     * @param rotationAngleListener the rotation angle listener
     */
    public void stopRotationAngleDetection(RotationAngleDetector.RotationAngleListener rotationAngleListener) {
        stopLibrarySensorDetection(rotationAngleListener);
    }

    /**
     * Check hardware boolean.
     *
     * @param context  the context
     * @param hardware the hardware
     * @return the boolean
     */
    boolean checkHardware(Context context, String hardware) {
        return context.getPackageManager().hasSystemFeature(hardware);
    }

    /**
     * Check permission boolean.
     *
     * @param context    the context
     * @param permission the permission
     * @return the boolean
     */
    boolean checkPermission(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean areAllSensorsValid(Iterable<Sensor> sensors) {
        for (Sensor sensor : sensors) {
            if (sensor == null) {
                return false;
            }
        }

        return true;
    }

    private Iterable<Sensor> convertTypesToSensors(int... sensorTypes) {
        Collection<Sensor> sensors = new ArrayList<>();
        if (sensorManager != null) {
            for (int sensorType : sensorTypes) {
                sensors.add(sensorManager.getDefaultSensor(sensorType));
            }
        }
        return sensors;
    }

    private void registerDetectorForAllSensors(SensorDetector detector, Iterable<Sensor> sensors) {
        for (Sensor sensor : sensors) {
            sensorManager.registerListener(detector, sensor, samplingPeriod);
        }
    }

    private void startLibrarySensorDetection(SensorDetector detector, Object clientListener) {
        if (!defaultSensorsMap.containsKey(clientListener)) {
            defaultSensorsMap.put(clientListener, detector);
            startSensorDetection(detector);
        }
    }

    private void startSensorDetection(SensorDetector detector) {
        final Iterable<Sensor> sensors = convertTypesToSensors(detector.getSensorTypes());
        if (areAllSensorsValid(sensors)) {
            registerDetectorForAllSensors(detector, sensors);
        }
    }

    private void stopLibrarySensorDetection(Object clientListener) {
        SensorDetector detector = defaultSensorsMap.remove(clientListener);
        stopSensorDetection(detector);
    }

    private void stopSensorDetection(SensorDetector detector) {
        if (detector != null && sensorManager != null) {
            sensorManager.unregisterListener(detector);
        }
    }
}

