package com.cps.rotationdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RotationAngleDetector.RotationAngleListener {

    private static final int PORT = 4110;
    private TextView txtAngle, txtZ, txtSpeed;
    private LinearLayout linearLayoutAngle, linearLayoutZ;

    private DatagramSocket socket;
    private Intent startSettingsActivity;
    private SharedPreferences preferences;
    private InetAddress address;
    private PacketMan packetMan;
    private ResponseListener responseListener;

    private int speed = 1;
    private float angleY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        packetMan = new PacketMan();
        responseListener = new ResponseListener(socket, packetMan);
        new Thread(responseListener).start();
        initUiComponent();
        initRotationSensor();
        initStartSettingsIntent();
        initSocket();
        initAddress();
//        new CountDownTimer(3000, 1) {
//            @Override
//            public void onTick(long l) {
//                byte[] packet = packetMan.intervalRun();
//                if(packet != null)
//                    new UdpSend(this, socket).execute(makeDatagram(packet));
//            }
//
//            @Override
//            public void onFinish() {
//                this.start();
//            }
//        }.start();
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
        initAddress();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stopRotationAngleDetection(this);
        Sensey.getInstance().stop();
        socket.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_plus:
                speed = (speed < 4) ? speed + 1 : speed;
                Log.v("SpeedListener", String.valueOf(speed));
                send();
                break;
            case R.id.btn_minus:
                speed = (speed > 1) ? speed - 1 : speed;
                Log.v("SpeedListener", String.valueOf(speed));
                send();
                break;
        }
        txtSpeed.setText(String.valueOf(speed));
    }

    @Override
    public void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ) {
        angleY = angleInAxisY;
        txtAngle.setText(String.valueOf(convertToAngle(angleY)));
        txtZ.setText(String.valueOf(angleInAxisZ));
        send();
    }

    private void initUiComponent() {
        linearLayoutAngle = findViewById(R.id.line_angle);
        linearLayoutZ = findViewById(R.id.line_z);
        debugMode(preferences.getBoolean(getString(R.string.preference_key_debug), false));
        txtAngle = findViewById(R.id.txt_angle);
        txtZ = findViewById(R.id.txt_z);
        txtSpeed = findViewById(R.id.txt_speed);
        Button btnPlusSpeed = findViewById(R.id.btn_plus);
        btnPlusSpeed.setOnClickListener(this);
        Button btnMinusSpeed = findViewById(R.id.btn_minus);
        btnMinusSpeed.setOnClickListener(this);
        txtSpeed.setText(String.valueOf(speed));
    }

    private void initStartSettingsIntent() {
        startSettingsActivity = new Intent(this, SettingsActivity.class);
        startSettingsActivity.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        startSettingsActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.SettingsPreferenceFragment.class.getName());
    }

    private void initSocket() {
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void initRotationSensor() {
        Sensey.getInstance().init(this, Sensey.SAMPLING_PERIOD_UI);
        Sensey.getInstance().startRotationAngleDetection(this);
    }

    private void debugMode(boolean isDebug) {
        linearLayoutAngle.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        linearLayoutZ.setVisibility(isDebug ? View.VISIBLE : View.GONE);
    }

    private int convertToAngle(float y) {
        int angle = 90 + (int) y;
        if(angle > 180)
            angle = 180;
        else if(angle < 0)
            angle = 0;
        return angle;
    }

    private DatagramPacket makeDatagram(byte[] packet) {
        return new DatagramPacket(packet, packet.length, address, 4210);
    }

    private void send() {
        byte[] packet = packetMan.createPacket(speed, convertToAngle(angleY), PacketMode.MOVE);
        if(packet != null) {
            new UdpSend(this, socket).execute(makeDatagram(packet));
            Log.v("MainActivity", Packet.byte_to_str(packet));
        }
    }

    private void initAddress() {
        try {
            address = InetAddress.getByName(preferences.getString(getString(R.string.preference_key_ip), getString(R.string.default_ip)));
        } catch (UnknownHostException e) {
            Log.e("MainActivity", e.getMessage());
            Toast.makeText(this, R.string.unknown_ip, Toast.LENGTH_LONG).show();
        }
    }
}
