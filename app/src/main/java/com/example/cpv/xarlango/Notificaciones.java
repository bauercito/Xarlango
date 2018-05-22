package com.example.cpv.xarlango;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Notificaciones extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,  servicio_notificaciones.class);
        context.startService(service);
    }
}
