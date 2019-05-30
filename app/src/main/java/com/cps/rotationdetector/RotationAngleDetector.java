package com.cps.rotationdetector;

import static android.hardware.Sensor.TYPE_ROTATION_VECTOR;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class RotationAngleDetector extends SensorDetector {


    /**
     * The interface Rotation angle listener.
     */
    public interface RotationAngleListener {

        /**
         * On rotation.
         *
         * @param angleInAxisX the angle in axis x
         * @param angleInAxisY the angle in axis y
         * @param angleInAxisZ the angle in axis z
         */
        void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ);
    }

    private final RotationAngleListener rotationAngleListener;

    /**
     * Instantiates a new Rotation angle detector.
     *
     * @param rotationAngleListener the rotation angle listener
     */
    public RotationAngleDetector(RotationAngleListener rotationAngleListener) {
        super(TYPE_ROTATION_VECTOR);
        this.rotationAngleListener = rotationAngleListener;
    }

    @Override
    protected void onSensorEvent(SensorEvent sensorEvent) {
        // Get rotation matrix
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);

        // Convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);

        // Convert values in radian to degrees
        for (int i = 0; i < 3; i++) {
            orientations[i] = (float) (Math.toDegrees(orientations[i]));
        }

        rotationAngleListener.onRotation(orientations[0], orientations[1], orientations[2]);
    }
}

