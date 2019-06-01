package com.cps.rotationdetector;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UdpSend extends AsyncTask<Float, Void, Boolean> {

    private String serverAddress;
    private Context activity;

    UdpSend(Context activity, String serverAddress) {
        this.activity = activity;
        this.serverAddress = serverAddress;
    }

    @Override
    protected Boolean doInBackground(Float... params) {
        String strMessage;
        final int server_port = 4210;
        byte[] message;
        message = this.makePacket(params[0], params[1]);
        try {
            DatagramSocket s = new DatagramSocket();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket p = new DatagramPacket(message, message.length, address, server_port);
            s.send(p);
        } catch (IOException e) {
            Log.e("UdpSend", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
//        if(!success)
//            Toast.makeText(activity, "Please enter the ip address...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPreExecute() {
    }

    private byte[] makePacket(float y, float z) {
        int _y;
        _y = 90-(int)y;
        if(_y>180)_y=180;
        if(_y<0)_y=0;
        _y /= 3;
        return make_packet(2,_y,Mode.MOVE);
    }
    //speed 1-4 angle 0-180
    private byte[] make_packet(int speed,int angle,Mode mode){
        byte[] return_value = new byte[2];
        return_value[0] = (byte)((int)((speed-1)<<6) + (int)((angle/3)&0x3F));
        return_value[1] = (byte)(mode.ordinal()<<5);
        return return_value;
    }
    public static enum Mode{
        STOP,MOVE,REVERSE_MOVE,GO_STRAIGHT,GO_BACK,TURN_RIGHT,TURN_LEFT
    }
}