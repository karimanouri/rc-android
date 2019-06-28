package com.cps.rotationdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, RotationAngleDetector.RotationAngleListener, Runnable {

    private static final int PORT = 4110;
    private TextView txtAngleDebug, txtZDebug, txtSpeed, txtSpeedDebug, txtLastPacketDebug, txtAckDebug;
    private LinearLayout linearLayoutAngle, linearLayoutZ, linearLayoutSpeed, linearLayoutLastPacket, linearLayoutACK;
    Button btnPlusSpeed, btnMinusSpeed;

    private DatagramSocket socket;
    private Intent startSettingsActivity;
    private SharedPreferences preferences;
    private InetAddress address;
    private PacketMan packetMan;
    private Handler handler;

    private int speedText = 1;
    private float angleY, angleZ;
    private boolean isButtonMode;
    private boolean isForward, move;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isForward = true;
        move = false;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        packetMan = new PacketMan();
        handler = new Handler();
        initUiComponent();
        initRotationSensor();
        initStartSettingsIntent();
        initSocket();
        initAddress();
        new Thread(this).start();
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
        isButtonMode = preferences.getBoolean(getString(R.string.preference_key_control), true);
        buttonMode(isButtonMode);
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

    // speed buttons
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_plus:
                speedText = (speedText < 4) ? speedText + 1 : speedText;
                Log.v("SpeedListener", String.valueOf(speedText));
                if(move)
                    send(packetMan.createPacket(isButtonMode ? speedText : angleToSpeed(angleZ), convertToAngle(angleY), (isForward) ? PacketMode.MOVE : PacketMode.REVERSE_MOVE));
                break;
            case R.id.btn_minus:
                speedText = (speedText > 1) ? speedText - 1 : speedText;
                Log.v("SpeedListener", String.valueOf(speedText));
                if(move)
                    send(packetMan.createPacket(isButtonMode ? speedText : angleToSpeed(angleZ), convertToAngle(angleY), (isForward) ? PacketMode.MOVE : PacketMode.REVERSE_MOVE));
                break;
            case R.id.btn_reverse:
                isForward = !isForward;
                Log.v("MainActivity", isForward ? "FORWARD" : "BACKWARD");
                break;
        }
        txtSpeed.setText(String.valueOf(speedText));
    }

    // send STOP when released
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                view.performClick();
                send(packetMan.createPacket(isButtonMode ? speedText : angleToSpeed(angleZ), 0, PacketMode.STOP));
                Log.v("MainActivity", "released!");
                move = false;
                Log.v("MainActivity", "moving stopped");
                break;

            case MotionEvent.ACTION_DOWN:
                view.performClick();
                send(packetMan.createPacket(isButtonMode ? speedText : angleToSpeed(angleZ), convertToAngle(angleY), (isForward) ? PacketMode.MOVE : PacketMode.REVERSE_MOVE));
                move = true;
                Log.v("MainActivity", "moving started");
                break;

        }
        return true;
    }

    @Override
    public void onRotation(float angleInAxisX, float angleInAxisY, float angleInAxisZ) {
        angleY = angleInAxisY;
        angleZ = angleInAxisZ;
        txtAngleDebug.setText(String.valueOf(convertToAngle(angleY)));
        txtZDebug.setText(String.valueOf(angleInAxisZ));
        txtSpeedDebug.setText(String.valueOf(angleToSpeed(angleZ)));
        if(move)
            send(packetMan.createPacket(isButtonMode ? speedText : angleToSpeed(angleZ), convertToAngle(angleY), (isForward) ? PacketMode.MOVE : PacketMode.REVERSE_MOVE));
    }

    // listen for ack response
    @Override
    public void run() {
        byte[] receive = new byte[100];
        DatagramPacket dpPacket = new DatagramPacket(receive, receive.length);
        byte[] ackPack = new byte[2];
        boolean error = false;
        while (!error) {
            try {
                socket.receive(dpPacket);
            } catch (IOException e) {
                error = true;
                // TODO handle error
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, R.string.receiver_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            ackPack[0] = receive[0];
            ackPack[1] = receive[1];
            final int seq = packetMan.ackedPacket(ackPack);
            Log.v("MainActivity", "receive ack: " + Packet.byte_to_str(ackPack));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtAckDebug.setText(String.valueOf(seq));
                }
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUiComponent() {
        linearLayoutAngle = findViewById(R.id.line_angle);
        linearLayoutZ = findViewById(R.id.line_z);
        linearLayoutSpeed = findViewById(R.id.line_speed);
        linearLayoutLastPacket = findViewById(R.id.line_last_packet);
        linearLayoutACK = findViewById(R.id.line_ack);
        isButtonMode = preferences.getBoolean(getString(R.string.preference_key_control), true);
        txtAngleDebug = findViewById(R.id.txt_angle);
        txtLastPacketDebug = findViewById(R.id.txt_last_packet);
        txtZDebug = findViewById(R.id.txt_z);
        txtSpeed = findViewById(R.id.txt_speed);
        txtSpeedDebug = findViewById(R.id.txt_speed_debug);
        txtAckDebug = findViewById(R.id.txt_ack);
        btnPlusSpeed = findViewById(R.id.btn_plus);
        btnPlusSpeed.setOnClickListener(this);
        btnMinusSpeed = findViewById(R.id.btn_minus);
        btnMinusSpeed.setOnClickListener(this);
        ToggleButton btnReverse = findViewById(R.id.btn_reverse);
        btnReverse.setOnClickListener(this);
        txtSpeed.setText(String.valueOf(speedText));
        LinearLayout layoutMain = findViewById(R.id.layout_secondary);
        layoutMain.setOnTouchListener(this);
        debugMode(preferences.getBoolean(getString(R.string.preference_key_debug), false));
        buttonMode(isButtonMode);
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
        linearLayoutSpeed.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        linearLayoutLastPacket.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        linearLayoutAngle.setVisibility(isDebug ? View.VISIBLE : View.GONE);
        linearLayoutACK.setVisibility(isDebug ? View.VISIBLE : View.GONE);
    }

    private void buttonMode(boolean isButtonMode) {
        btnPlusSpeed.setVisibility(isButtonMode ? View.VISIBLE : View.GONE);
        btnMinusSpeed.setVisibility(isButtonMode ? View.VISIBLE : View.GONE);
        txtSpeed.setVisibility(isButtonMode ? View.VISIBLE : View.GONE);
    }

    private int convertToAngle(float y) {
        int angle = 90 + (int) y;
        if(angle > 180)
            angle = 180;
        else if(angle < 0)
            angle = 0;
        return angle;
    }

    private int angleToSpeed(float z) {
        if(z >= 45)
            return 1;
        else if(z < 45 && z > 30)
            return 2;
        else if(z < 30 && z > 15)
            return 3;
        else
            return 4;
    }

    private DatagramPacket makeDatagram(byte[] packet) {
        return new DatagramPacket(packet, packet.length, address, 4210);
    }

    private void send(byte[] packet) {
        if(packet != null) {
            txtLastPacketDebug.setText(Packet.byte_to_str(packet));
            new UdpSender(this, socket).execute(makeDatagram(packet));
            manageHandler();
            Log.v("MainActivity", Packet.byte_to_str(packet));
        }
    }

    // resend
    private void manageHandler() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v("MainActivity", "RESEND!");
                send(packetMan.intervalRun());
            }
        }, 200 );
    }

    private void initAddress() {
        try {
            address = InetAddress.getByName(preferences.getString(getString(R.string.preference_key_ip), getString(R.string.default_ip)));
        } catch (UnknownHostException e) {
            // TODO handle error
            Log.e("MainActivity", e.getMessage());
            Toast.makeText(this, R.string.unknown_ip, Toast.LENGTH_SHORT).show();
        }
    }

    private void restart() {
        finish();
        startActivity(getIntent());
    }
}
