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
        try {
            strMessage = this.makePacket(params[0], params[1]).toString();
        } catch (JSONException e) {
            Log.e("UdpSend", e.getMessage());
            return false;
        }

        final int server_port = 4210;
        int msg_length = strMessage.length();
        byte[] message = strMessage.getBytes();

        try {
            DatagramSocket s = new DatagramSocket();
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket p = new DatagramPacket(message, msg_length, address, server_port);
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

    private JSONObject makePacket(float y, float z) throws JSONException {
        JSONObject jsnAngle = new JSONObject();
        jsnAngle.put("y", y);
        jsnAngle.put("z", z);
        return jsnAngle;
    }
}