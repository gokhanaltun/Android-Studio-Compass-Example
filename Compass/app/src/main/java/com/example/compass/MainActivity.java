package com.example.compass;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.example.compass.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private Sensor rotationVector;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float rotation = 0;
    private float xRot = 0f;
    private boolean vertical = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorEventListener = new SensorEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                    float[] xRotAngles = new float[9];
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                    SensorManager.getOrientation(rotationMatrix, xRotAngles);

                    xRot = (float) Math.round(Math.toDegrees(xRotAngles[1]));

                    if (xRot <= -70 && xRot>= -80){
                        vertical = true;
                    }else if (xRot >= 70 && xRot <= 80){
                        vertical = true;
                    }else if (xRot <= -10 && xRot >= -20){
                        vertical =false;
                    } else if (xRot >= 10 && xRot <= 20) {
                        vertical = false;
                    }

                    if (vertical){
                        float[] verticalMatrix = new float[9];
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_X,
                                SensorManager.AXIS_Z,
                                verticalMatrix);
                        SensorManager.getOrientation(verticalMatrix, orientationAngles);
                        xRot = (float) Math.toDegrees(orientationAngles[0]);
                    }else {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles);
                        xRot = (float) Math.toDegrees(orientationAngles[1]);
                    }

                    float rot = (float) (Math.toDegrees(orientationAngles[0])+360)%360;
                    rotateCompass(rotation, rot);
                    rotation = rot;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
        sensorManager.registerListener(sensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    @SuppressLint("SetTextI18n")
    private void rotateCompass(float lastRotation, float currentRotation){
        RotateAnimation ra = new RotateAnimation(
                -lastRotation,
                -currentRotation,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setRepeatCount(0);
        ra.setFillAfter(true);
        ra.setInterpolator(new LinearInterpolator());
        ra.setDuration(500);

        binding.imgCompassIn.setAnimation(ra);
        binding.txtDegree.setText(Math.round(currentRotation) + "°");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, rotationVector, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }
}