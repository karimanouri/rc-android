package com.cps.rotationdetector;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class UdpSender extends AsyncTask<DatagramPacket, Void, Boolean> {

    private DatagramSocket socket;
    private WeakReference<MainActivity> mainActivity;

    UdpSender(MainActivity activity, DatagramSocket socket) {
        this.mainActivity = new WeakReference<>(activity);
        this.socket = socket;
    }

    @Override
    protected Boolean doInBackground(DatagramPacket... params) {
        try {
            socket.send(params[0]);
        } catch (IOException e) {
            Log.e("UdpSender", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean send) {
        super.onPostExecute(send);
        // TODO handle error
//        if(!send)
//            Toast.makeText(mainActivity.get(), mainActivity.get().getString(R.string.sending_failed), Toast.LENGTH_SHORT).show();
    }
}