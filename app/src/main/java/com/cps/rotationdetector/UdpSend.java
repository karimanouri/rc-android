package com.cps.rotationdetector;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UdpSend extends AsyncTask<Integer, Void, Boolean> {

    private String serverAddress;
    private DatagramSocket socket;

    public enum Mode{
        STOP, MOVE, REVERSE_MOVE, GO_STRAIGHT, GO_BACK, TURN_RIGHT, TURN_LEFT
    }

    UdpSend(DatagramSocket socket, String serverAddress) {
        this.socket = socket;
        this.serverAddress = serverAddress;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        final int server_port = 4210;
        byte[] message;
        message = this.makePacket(params[0], params[1], Mode.MOVE);
        try {
            InetAddress address = InetAddress.getByName(serverAddress);
            DatagramPacket p = new DatagramPacket(message, message.length, address, server_port);
            socket.send(p);
        } catch (IOException e) {
            Log.e("UdpSend", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {

    }

    @Override
    protected void onPreExecute() {
    }

    private byte[] makePacket(int angle, int speed, Mode mode) {
        byte[] return_value = new byte[2];
        return_value[0] = (byte)(((speed - 1) << 6) + ((angle / 3) & 0x3F));
        return_value[1] = (byte)(mode.ordinal() << 5);
        return return_value;
    }
}