package com.cps.rotationdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private RotationAngleDetector.RotationAngleListener rotationAngleListener;

    private TextView txtAngle, txtZ, txtSpeed;
    private LinearLayout linearLayoutAngle, linearLayoutZ;

    private DatagramSocket socket;

    Intent startSettingsActivity;
    SharedPreferences preferences;

    private int speed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        initUiComponent();
        initSocket();
        initRotationSensor();
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
    protected void onResume() {
        debugMode(preferences.getBoolean(getString(R.string.preference_key_debug), false));
        super.onResume();
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
        linearLayoutAngle = findViewById(R.id.line_angle);
        linearLayoutZ = findViewById(R.id.line_z);
        debugMode(preferences.getBoolean(getString(R.string.preference_key_debug), false));
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

    private void initSocket() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void initRotationSensor() {
        Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_UI);
        rotationAngleListener = new RotationAngleDetector.RotationAngleListener() {
            @Override
            public void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ) {
                txtAngle.setText(String.valueOf(convertToAngle(angleInAxisY)));
                txtZ.setText(String.valueOf(angleInAxisZ));
                UdpSend udpSend = new UdpSend(socket, preferences.getString(getString(R.string.preference_key_ip), getString(R.string.default_ip)));
                udpSend.execute(convertToAngle(angleInAxisY), speed);
            }
        };
        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);
    }

    private void debugMode(boolean isDebug) {
        linearLayoutAngle.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        linearLayoutZ.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        Log.v("MainActivity", String.valueOf(isDebug));
    }
}
