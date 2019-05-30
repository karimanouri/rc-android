package com.cps.rotationdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RotationAngleDetector.RotationAngleListener rotationAngleListener;

    private TextView txtY, txtZ;
    private EditText edtIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtY = findViewById(R.id.txt_y);
        txtZ = findViewById(R.id.txt_z);
        edtIpAddress = findViewById(R.id.edt_ip_address);

        Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_UI);

        rotationAngleListener = new RotationAngleDetector.RotationAngleListener() {
            @Override
            public void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ) {
                txtY.setText(String.valueOf(angleInAxisY));
                txtZ.setText(String.valueOf(angleInAxisZ));
                UdpSend udpSend = new UdpSend(MainActivity.this, edtIpAddress.getText().toString());
                udpSend.execute(angleInAxisY, angleInAxisZ);
            }
        };

        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stopRotationAngleDetection(rotationAngleListener);
        Sensey.getInstance().stop();
    }
}
