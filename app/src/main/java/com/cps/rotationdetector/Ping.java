package com.cps.rotationdetector;

import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;

public class Ping extends AsyncTask<Integer, Void, String> {

    private WeakReference<MainActivity> mainActivity;
    private InetAddress address;

    Ping(MainActivity activity, InetAddress address) {
        this.mainActivity = new WeakReference<>(activity);
        this.address = address;
    }

    @Override
    protected String doInBackground(Integer... params) {
        try {
            if(!address.isReachable(params[0]))
                return "address not reachable";
        } catch (IOException e) {
            return e.getMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String error) {
        super.onPostExecute(error);
        if(error != null)
            mainActivity.get().handleError("Ping", error);
    }
}
