package com.example.cpv.xarlango.servicios;

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

/**
 * Clase que extiende de Servicio. Esta clase orientada a estar en segundo plano sera llamada al
 * inicio de la aplicacion y se encontrara en tod_o momento en segundo plano. A traves de un hilo
 * lanzado comprobara cada pocos segundos si el usuario tiene conexion a internet. Si no establece
 * conexion con el servidor sera lanzado un BroadCastReciver a una clase especifica que generara
 * un dialog informando al usuario
 */
public class Service_conexionPermanente extends Service {
    int numero; //numero de telefono del usuario
    EstadoConexion tarea; //Objeto que contrenda el hilo lanzado para segundo plano

    /**
     * Contructor que inicializa las variables globales
     */
    public Service_conexionPermanente() {
        numero=0;
        tarea=new EstadoConexion();
    }

    /**
     * Metodo que es llamado al iniciar el servicio. Lanzara un hilo con la tarea asignada
     */
    @Override
    public void onCreate() {
        super.onCreate();
        tarea.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Metodo que sera llamado cuando vuelva a ser iniciado el servicio por segunda vez o mas. Ya
     * no pasara por el onCreate si no que pasara por este metodo
     * @param intent intent de donde fue llamado el servicio
     * @param flags marcador
     * @param startId id inicial
     * @return numero de como tiene que actuar el servicio. "inicio pegajoso" en este caso. No
     * finalizara el servicio
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    /**
     * Metodo que sera llamado cuando se destrulla el servicio. No usado
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Metodo no usado
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Clase interna que heredara de AsyncTask. Creara un hilo para lanzar una tarea en segundo
     * plano. Esta tarea sera la de comprobar la conexion a internet del usuario
     */
    private class EstadoConexion extends AsyncTask<String,String,String>{
        /**
         * Metodo que sera llamado cuando se lanze el hilo y realizara la tarea de comprobar el
         * estado de la conexion con internet cada 5 segundos. Si no existies dicha conexion
         * llamara al metodo onProgressUpdate()
         * @param strings no usado
         * @return no suado
         */
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

        /**
         * Metodo quie sera llamado antes de iniciar el hilo. No usado
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Metodo que sera llamado despues de acabar la tarea. No usado
         * @param s no usado
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        /**
         * Metodo que sera llamado cuando se especifique en la tarea lanzada. En este metodo se
         * podran realizar cambios en el primer plano de la aplicacion. Llamara a un
         * broadCastReciver para comunicar a otra clase que debe lanzar un dialog al usuario en
         * primer plano
         * @param values
         */
        @Override
        protected void onProgressUpdate(String... values) {
            doSendBroadcast("olaaaaaaaaa");
            //Toast.makeText(getApplicationContext(), "Sin conexionnnnnnnnnnnnnnnnnn", Toast.LENGTH_SHORT).show();

            super.onProgressUpdate(values);
        }

        /**
         * Metodo que sera llamado cuando se cancele la tarea. No usado
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }

    /**
     * Metodo que comprobara y devolvera si existe conexion con la red wifi o la red movil. Si no
     * existiese devolvera un boolean false
     * @param context contexto de la aplicacion
     * @return boolean con true si existe conexion a internet
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    /**
     * Envia un BroadcastReciver para comunicar a otra clase  que no existe conexion a internet. La
     * clase receptora enviara un Dialog al usuario
     * @param message
     */
    private void doSendBroadcast(String message) {
        Intent it = new Intent("EVENT_SNACKBAR");

        if (!TextUtils.isEmpty(message))
            it.putExtra("ola",message);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(it);
    }
}
