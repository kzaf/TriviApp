package com.zaf.triviapp.threads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class NetworkUtilTask extends AsyncTask<Void, Void, Void> {
    Context context;
    public static final String WIFI = "WIFI";
    public static final String MOBILE = "MOBILE";
    private AsyncTaskCompleteListener callback;
    private boolean hasConnection;

    public NetworkUtilTask(Context context, AsyncTaskCompleteListener callback){
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        hasConnection = hasActiveInternetConnection(this.context);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callback.onTaskComplete(hasConnection);
    }

    private boolean hasActiveInternetConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(WIFI))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase(MOBILE))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public interface AsyncTaskCompleteListener {
        void onTaskComplete(boolean hasInternet);
    }
}