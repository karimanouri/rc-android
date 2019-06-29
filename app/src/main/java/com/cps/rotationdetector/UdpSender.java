package com.cps.rotationdetector;

import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class UdpSender extends AsyncTask<DatagramPacket, Void, String> {

    private DatagramSocket socket;
    private WeakReference<MainActivity> mainActivity;

    UdpSender(MainActivity activity, DatagramSocket socket) {
        this.mainActivity = new WeakReference<>(activity);
        this.socket = socket;
    }

    @Override
    protected String doInBackground(DatagramPacket... params) {
        try {
            socket.send(params[0]);
        } catch (IOException e) {
            return e.getMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        super.onPostExecute(error);
        if(error != null)
            mainActivity.get().handleError("UdpSender", error);
    }
}