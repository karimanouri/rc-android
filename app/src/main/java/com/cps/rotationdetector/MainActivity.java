package com.cps.rotationdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private RotationAngleDetector.RotationAngleListener rotationAngleListener;

    private TextView txtAngle, txtZ, txtSpeed;
    private EditText edtIpAddress;

    private DatagramSocket socket;

    Intent startSettingsActivity;

    private int speed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUiComponent();

        Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_UI);

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        rotationAngleListener = new RotationAngleDetector.RotationAngleListener() {
            @Override
            public void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ) {
                txtAngle.setText(String.valueOf(convertToAngle(angleInAxisY)));
                txtZ.setText(String.valueOf(angleInAxisZ));
                UdpSend udpSend = new UdpSend(socket, edtIpAddress.getText().toString());
                udpSend.execute(convertToAngle(angleInAxisY), speed);
            }
        };

        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);

        initStartSettingsIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_settings)
            startActivity(startSettingsActivity);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stopRotationAngleDetection(rotationAngleListener);
        Sensey.getInstance().stop();
        socket.close();
    }

    private int convertToAngle(float y) {
        int angle = 90 + (int) y;
        if(angle > 180)
            angle = 180;
        else if(angle < 0)
            angle = 0;
        return angle;
    }

    private View.OnClickListener btnSpeedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_plus:
                    speed = (speed < 4) ? speed + 1 : speed;
                    Log.v("SpeedListener", String.valueOf(speed));
                    break;
                case R.id.btn_minus:
                    speed = (speed > 1) ? speed - 1 : speed;
                    Log.v("SpeedListener", String.valueOf(speed));
                    break;
            }
            txtSpeed.setText(String.valueOf(speed));
        }
    };

    private void initUiComponent() {
        edtIpAddress = findViewById(R.id.edt_ip_address);
        txtAngle = findViewById(R.id.txt_angle);
        txtZ = findViewById(R.id.txt_z);
        txtSpeed = findViewById(R.id.txt_speed);
        Button btnPlusSpeed = findViewById(R.id.btn_plus);
        btnPlusSpeed.setOnClickListener(btnSpeedListener);
        Button btnMinusSpeed = findViewById(R.id.btn_minus);
        btnMinusSpeed.setOnClickListener(btnSpeedListener);
        txtSpeed.setText(String.valueOf(speed));
    }

    private void initStartSettingsIntent() {
        startSettingsActivity = new Intent(this, SettingsActivity.class);
        startSettingsActivity.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        startSettingsActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.SettingsPreferenceFragment.class.getName());
    }
}
