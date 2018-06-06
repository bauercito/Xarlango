package com.example.cpv.xarlango.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.cpv.xarlango.servicios.Servicio_notificaciones;

/**
 * Clase que hereda de BradCastReciver. Sera una clase de apoyo la cual esta a la escucha para que
 * que sea llamada y iniciar un servicio al inicio de la aplicacion
 */
public class Notificaciones extends BroadcastReceiver {

    /**
     * Metodo que inicia el servicio de notificaciones al inicar la aplicacion
     * @param context contexto de la aplicacion
     * @param intent intent del que fue llamado
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,  Servicio_notificaciones.class);
        context.startService(service);
    }
}
