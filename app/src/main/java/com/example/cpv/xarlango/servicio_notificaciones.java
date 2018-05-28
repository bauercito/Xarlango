package com.example.cpv.xarlango;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.cpv.chatpruebas.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class servicio_notificaciones extends Service {
    static boolean ESTADOAPP=false;
    @Override
    public void onCreate() {
        super.onCreate();
        ESTADOAPP=false;
        Notificacion_chat noti=new Notificacion_chat();
        noti.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public String getTelefono(){
        TelephonyManager tMgr;
        tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private class Notificacion_chat extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
                final String telefono = getTelefono();
                final ArrayList<String> chatsAbiertos = new ArrayList();
                //final ArrayList<String> nombres=new ArrayList();
                //final String[] ultima = new String[1];
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String nombreChat[] = new String[2];
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            nombreChat= child.getKey().split("_");
                            if (nombreChat[0].equalsIgnoreCase(telefono) || nombreChat[1].equalsIgnoreCase(telefono)) {
                                chatsAbiertos.add(nombreChat[0] + "_" + nombreChat[1]);
                                //nombres.add(getNombre(nombreChat));

                            }

                        }
                        ponerListenerChats(chatsAbiertos);
                        /*for (int i = 0; i < chatsAbiertos.size(); i++) {
                            listenerChats(ultima, chatsAbiertos, i, nombres);
                        }*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return null;
                //return null;
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
            /*NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    getBaseContext())
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Xarlango")
                    .setContentText(values[0])
                    .setWhen(System.currentTimeMillis());
            nManager.notify(12345, builder.build());*/
            //Log.d("PEPE",values[0]);


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


        public void listenerChats(final String[] ultima, ArrayList<String> chatsAbiertos, final int index, final ArrayList<String> nombres){
            FirebaseDatabase database01 = FirebaseDatabase.getInstance();
            DatabaseReference myRef01 = database01.getReference("message/" +chatsAbiertos.get(index));
            myRef01.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        ultima[0] =child.getValue().toString();
                    }
                    if(servicio_notificaciones.ESTADOAPP){
                        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                getBaseContext())
                                .setSmallIcon(R.drawable.chats)
                                .setContentTitle("Xarlango")
                                .setContentText(nombres.get(index)+" dice: "+ultima[0])
                                .setWhen(System.currentTimeMillis());
                        nManager.notify(index, builder.build());
                    }

                    //publishProgress(ultima[0]);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        public String getNombre (String[] nombreChat){
            String telefonoDestino;
            final String[] nombre = new String[1];
            if(nombreChat[0]!=getTelefono()){
                telefonoDestino=nombreChat[0];
            }else{
                telefonoDestino=nombreChat[1];
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users/"+telefonoDestino+"/nombre");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    nombre[0] =dataSnapshot.getValue().toString();


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return nombre[0];
        }
        public void ponerListenerChats(final ArrayList<String> chatsAbiertos){
            String telefonoDestino;
            //saco los nombres DEL TELEFONO DESTINO
            for(int i = 0; i<chatsAbiertos.size(); i++){
                final int index=i;
                String[] nombreChats_seperado=chatsAbiertos.get(i).split("_");
                if(nombreChats_seperado[0]!=getTelefono()){
                    telefonoDestino=nombreChats_seperado[0];
                }else{
                    telefonoDestino=nombreChats_seperado[1];
                }
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/"+telefonoDestino+"/nombre");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String nombre=dataSnapshot.getValue().toString();
                        ponerListenerChat(nombre,chatsAbiertos,index);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
        public void ponerListenerChat(final String nombre, final ArrayList<String> chatsAbiertos, final int i){
            FirebaseDatabase database01 = FirebaseDatabase.getInstance();
            DatabaseReference myRef01 = database01.getReference("message/" +chatsAbiertos.get(i));
            myRef01.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String ultima_frase = null;
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                         ultima_frase=child.getValue().toString();
                    }
                    if(servicio_notificaciones.ESTADOAPP){
                        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                getBaseContext())
                                .setSmallIcon(R.drawable.chats)
                                .setContentTitle("Xarlango")
                                .setContentText(nombre.toUpperCase()+" dice: "+ultima_frase)
                                .setWhen(System.currentTimeMillis());
                        String[] numeros=chatsAbiertos.get(i).split("_");
                        Intent targetIntent = new Intent(getApplicationContext(), Chat.class);
                        targetIntent.putExtra("nombreChat",chatsAbiertos.get(i));
                        targetIntent.putExtra("numero_usuario",numeros[0]);
                        targetIntent.putExtra("numero_destino",numeros[1]);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                                targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.setContentIntent(contentIntent);
                        builder.setAutoCancel(true);
                        long[] pattern = {500,500,500,500,500,500,500,500,500};
                        builder.setVibrate(pattern);
                        builder.setLights(Color.BLUE, 500, 500);
                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        builder.setSound(alarmSound);



                        nManager.notify(i, builder.build());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }


    }
}
