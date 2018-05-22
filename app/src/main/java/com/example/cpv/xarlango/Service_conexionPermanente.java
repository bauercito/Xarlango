package com.example.cpv.xarlango;

import android.app.DialogFragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Service_conexionPermanente extends Service {
    int numero;
    EstadoConexion tarea;
    public Service_conexionPermanente() {
        numero=0;
        tarea=new EstadoConexion();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tarea.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class EstadoConexion extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            boolean entro=false;
            while (true) {
                if(!isOnline(getApplicationContext())&&!entro){
                    entro=true;
                    publishProgress();
                }else if(isOnline(getApplicationContext())&&entro){
                    entro=false;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            doSendBroadcast("olaaaaaaaaa");
            //Toast.makeText(getApplicationContext(), "Sin conexionnnnnnnnnnnnnnnnnn", Toast.LENGTH_SHORT).show();

            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
    private void doSendBroadcast(String message) {
        Intent it = new Intent("EVENT_SNACKBAR");

        if (!TextUtils.isEmpty(message))
            it.putExtra("ola",message);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
    }
}
