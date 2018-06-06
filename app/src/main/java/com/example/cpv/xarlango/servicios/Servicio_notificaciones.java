package com.example.cpv.xarlango.servicios;

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

import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.actividades.Chat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Clase encargada del sistema de notificaciones de la aplicacion. Hereda de servicice, por lo que
 * se encontrara en segundo plano en cualquier momento de la aplicacion lanzada a traves de un hilo
 * con AsyncTask. Tendra una variable estatica de apoyo repartido en todas las actividades de la
 * aplicacion para saber cuando la aplicacione sta en segundo plano o en primero
 */
public class Servicio_notificaciones extends Service {
    public static boolean ESTADOAPP=false; //variable estatica que ira cambiando segun la aplicacion
                                            // entre en primer plano o en segundo

    /**
     * Metodo que sera llamado al iniciar el servicio. Inicializara la variable estatica ESTADOAPP
     * a false e iniciara un hilo para cargar el sistema de notificaciones
     */
    @Override
    public void onCreate() {
        super.onCreate();
        ESTADOAPP=false;
        Notificacion_chat noti=new Notificacion_chat();
        noti.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Metodo que recoge el numero de telefono del usuario local
     * @return devuelve un string con el numero de telefono
     */
    public String getTelefono(){
        TelephonyManager tMgr;
        tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
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
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Clase interna que heredara de AsyncTask. Creara un hilo para lanzar una tarea en segundo
     * plano. Esta tarea comprobara si ha habido algun cambio en alguna conversacion del usuario.
     * Si es asi, comprobara si se encuentra en segundo plano la aplicacion, lanzando una
     * notificacion al area de notificaciones del dispositivo en caso afirmativo
     */
    private class Notificacion_chat extends AsyncTask<String,String,String> {
        /**
         * Metodo que lanza la tarea en segundo plano. Comprobara las conversaciones que tiene
         * abiertas el usuario. Una vez echo esto llamara al metodo ponerListenerChats() pasandole
         * por parametros un arrayList con las conversaciones abiertas del usuario
         * @param strings sin usar
         * @return retorno nulo
         */
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
         * podran realizar cambios en el primer plano de la aplicacion. No usada
         * @param values sin usar
         */
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
        /**
         * Metodo que sera llamado cuando se cancele la tarea. No usado
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        /**
         * Metodo que sacara el nombre del telefono destino para luego pasarselo al metodo
         * ponerListnersChat
         * @param chatsAbiertos
         */
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

        /**
         * Metodo que pondra a la escucha cada conversacion que tenga abierta el usuario local.
         * Si se detecta un cambio en la conversacion esta creara una notificacion comprobando
         * previamente si la aplicacion se encuentra en segundo plano. Esta notificacion se
         * encontrara en el area de notificaciones y ira acompañada por un sonido, la persona
         * quien escribio y el texto que escribio
         * @param nombre nombre de la persona destino
         * @param chatsAbiertos chat que ha manteniddo con la persona
         * @param i index
         */
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
                    if(Servicio_notificaciones.ESTADOAPP){
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
