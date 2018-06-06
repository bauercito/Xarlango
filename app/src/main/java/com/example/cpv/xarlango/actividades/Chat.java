package com.example.cpv.xarlango.actividades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.adaptadores.AdaptadorChatPersonalizado;
import com.example.cpv.xarlango.gui.Conexion_dialog;
import com.example.cpv.xarlango.servicios.Servicio_notificaciones;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Clase que establece los metodos necesarios para crear una sala de conversacion con el que dos
 * usuarios con la aplicacion instalada puedan conversar
 */
public class Chat extends AppCompatActivity {
    String nombreChat; //nombre del chat compuesto por los dos telefonos de los usuarios
    String numeroOrigen; //numero de quien creo la covnersacion
    String numeroDestino; //numero de quien no creo la conversacion
    AdaptadorChatPersonalizado adaptador; //adaptador del listvoew
    ListView lista; //listview
    ArrayList<String> palabras; //array usado de apoyo
    ArrayList<String> persona; //nombres las dos personas
    String[] hora_contacto; //fecha de las conversaciones
    ArrayList<String> hora; //decha de las conversaciones
    private BroadcastReceiver mMessageReceiver = null; //broadcastreciver para la conexion erpmanente
    TelephonyManager tMgr; //necesario para sacar el telefono local
    String numeroPropio; //numero de la persona local

    /**
     * Metodo que es llamado al iniciar la actividad. Establece el layout. Pone a la escucha la
     * conexion permanente llama al metodo get telefono apra establecer la variable global
     * numeroPropio. Establece la tollbar personalizada y pone a la escucha la foto ey el nombre de
     * perfil del usuario destino. Tambien llama al adaptador de chatPersonalizado para cargar
     * la conversacion y las futuras covnersaciones que vayan apareciendo
     * @param savedInstanceState bundle que recogera datos primitivos si la aplicacion pasaes a
     *                           segundo plano
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        getTelefono();

        //RECIBO MENSAJE DE DESCONEXION
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Conexion_dialog dialog = new Conexion_dialog();
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(),"tag");
            }
        };


        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        BitmapDrawable background = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(),
                        R.drawable.fondo_estandar));
        getSupportActionBar().setBackgroundDrawable(background);

        LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
        View mCustomView = mInflater.inflate(R.layout.toolbar_custom, null);
        getSupportActionBar().setCustomView(mCustomView);

        //PONER FOTO Y NOMBRE PERFIL A LA ESCUCHA PARA ABRIR PERFIL
        mCustomView.findViewById(R.id.fotoPerfil_circular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarPerfilDesdeFoto();
            }
        });
        mCustomView.findViewById(R.id.nombre_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarPerfilDesdeFoto();
            }
        });


        //recoger datos
        Bundle extras = getIntent().getExtras();
        nombreChat = extras.getString("nombreChat");
        numeroOrigen = extras.getString("numero_usuario");
        numeroDestino = extras.getString("numero_destino");

        cargarFotoDestino();
        obtenerNombreDestino();


        //LEER DATOS
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message/" + nombreChat);


        lista = (ListView) findViewById(R.id.listview);
        lista.setDivider(null);

        palabras = new ArrayList();
        persona=new ArrayList();
        hora=new ArrayList();
        hora_contacto = new String[2];


        adaptador=new AdaptadorChatPersonalizado(palabras,persona,numeroOrigen,this, hora);
        lista.setAdapter(adaptador);
        lista.setSelection(palabras.size() - 1);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                palabras.clear();
                persona.clear();
                hora.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    palabras.add(child.getValue().toString());
                    hora_contacto=child.getKey().toString().split("_");
                    persona.add(hora_contacto[1]);
                    hora.add(hora_contacto[0]);
                }
                adaptador.notifyDataSetChanged();
                lista.setSelection(palabras.size() - 1);
                restablecerListView(palabras,persona, hora);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("no", "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Metodo sobreescrito que sera llamado cuando la actividad este en primer plano. Pone a la
     * escucha un broadCastReciver y establece una variable estatica para el estaod de las
     * notificaciones
     */
    @Override
    public void onResume() {
        super.onResume();
        Servicio_notificaciones.ESTADOAPP=false;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));
    }

    /**
     * Metod sobreescrito que es llamado cuando la aplicacion entra en segundo plano. Deja de
     * registrar el broadcastReciver
     */
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    /**
     * Metodo sobreescrito que es llamado cuando la aplicacion entra en segundo plano. Establece
     * una variable estatica para el servicio de notificaciones
     */
    @Override
    protected void onPause() {
        super.onPause();
        Servicio_notificaciones.ESTADOAPP=true;
    }

    /**
     * Llama al adaptador para que establezca en el listView todas las conversaciones tomadas por
     * los usuarios en la covnersacion
     * @param palabras conversaciones pasadas a un arrayList
     * @param persona las personas de la conversacion
     * @param hora hora de cada conversacion enviada
     */
    public void restablecerListView(ArrayList<String> palabras,ArrayList<String> persona,ArrayList<String> hora) {

        adaptador=new AdaptadorChatPersonalizado(palabras,persona,numeroOrigen,this,hora);

        lista.setAdapter(adaptador);
        lista.setSelection(palabras.size() - 1);

    }

    /**
     * Metodo que pone a la escucha un ImageView que enviara al servidor lo ultimo escrito en
     * la conversacion por el usuario. Si el usuario no excribe en el Input proporcionado sera
     * infiormado. Tambien escribira en el servidor una seria de metadaros para saber la fecha
     * de el texto enviado
     * @param enviar vista del layout
     */
    public void enviar(View enviar) {
        TextView input = findViewById(R.id.texto1);
        if(input.getText().toString().equalsIgnoreCase("")){
            Toast toast = Toast.makeText(getApplicationContext(), "Escribe algo!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }else{
            //Se crea el chat en message
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("message/" + nombreChat + "/" + obtenerFecha2()+"_"+numeroOrigen);
            myRef.setValue(input.getText().toString());


            //se crea los metadatos en el usuario emisor y receptor
            DatabaseReference myRef01 = database.getReference("users/" + numeroOrigen + "/chats/" + nombreChat + "/" + "ultimo_uso");
            myRef01.setValue(obtenerFecha2());
            DatabaseReference myRef02 = database.getReference("users/" + numeroDestino + "/chats/" + nombreChat + "/" + "ultimo_uso");
            myRef02.setValue(obtenerFecha2());

           /* //se introduce sonido
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(this, R.raw.pop);
            mediaPlayer.start();*/

            input.setText("");
        }


    }

    /**
     * Metodo que devulve la fecha proporcionada por un servidor
     * @return
     */
    public String obtenerFecha() {
        String[] hosts = new String[] {"0.pool.ntp.org"};

        NTPUDPClient client = new NTPUDPClient();
        // We want to timeout if a response takes longer than 5 seconds
        client.setDefaultTimeout(2000);
        SimpleDateFormat OutPutFormat = new SimpleDateFormat(
                "dd-M-yyyy HH:mm:ss:SSS", java.util.Locale.getDefault());
        for (String host : hosts) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
                StrictMode.setThreadPolicy(policy);
                InetAddress hostAddr = InetAddress.getByName(host);
                TimeInfo info = client.getTime(hostAddr);
                Date date = new Date(info.getReturnTime());
                /*Calendar calendar = Calendar.getInstance();
                calendar.setTime(date); //tuFechaBase es un Date;
                calendar.add(Calendar.HOUR,   6); //horasASumar es int.
                String out = OutPutFormat.format(calendar.getTime());*/
                return OutPutFormat.format(date);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.close();
        return null;

    }

    /**
     * Metodo que devuleve la fecha actual del dispositivo movil
     * @return
     */
    public String obtenerFecha2(){
        Date date=new Date();
        SimpleDateFormat OutPutFormat = new SimpleDateFormat(
                "dd-M-yyyy HH:mm:ss:SSS", java.util.Locale.getDefault());
        return OutPutFormat.format(date);
    }

    /**
     * Metodo que establece el nombre del usuario con el que se conversa y lo pone en la toolbar
     */
    public void obtenerNombreDestino(){
        FirebaseDatabase database02 = FirebaseDatabase.getInstance();
        DatabaseReference myRef02=null;
        String[] telefonosPorSeperado=nombreChat.split("_");
        if(numeroPropio.equalsIgnoreCase(telefonosPorSeperado[0])){
            myRef02 = database02.getReference("users/" +telefonosPorSeperado[1]+"/nombre");
        }else{
            myRef02 = database02.getReference("users/" +telefonosPorSeperado[0]+"/nombre");
        }
        myRef02.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //getSupportActionBar().setTitle(dataSnapshot.getValue().toString());
                TextView img=(TextView) getSupportActionBar().getCustomView().findViewById(R.id.nombre_toolbar);
                img.setText(dataSnapshot.getValue().toString());

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    /**
     * Metodo que establece la foto del usuario con el que se conversa en la toolbar
     */
    public void cargarFotoDestino(){
        //RECUPERO LA FOTO DE FIREBASE
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String[] telefonosPorSeperado=nombreChat.split("_");
        StorageReference Ref ;
        if(numeroPropio.equalsIgnoreCase(telefonosPorSeperado[0])){
            Ref = storageRef.child(telefonosPorSeperado[1]+".jpg");
        }else{
            Ref = storageRef.child(telefonosPorSeperado[0]+".jpg");
        }

        final long ONE_MEGABYTE = 1024 * 1024;
        Ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bmp2=Bitmap.createScaledBitmap(bmp, 75, 75, false);
                //Resources res = getResources();
                //BitmapDrawable icon = new BitmapDrawable(res,bmp2);
                getSupportActionBar().setDisplayShowCustomEnabled(true);
                ImageView img=(ImageView)getSupportActionBar().getCustomView().findViewById(R.id.fotoPerfil_circular);
                img.setImageDrawable(redondear_imagen(bmp2));
                //getSupportActionBar().setIcon(icon);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception){
                getSupportActionBar().setDisplayShowCustomEnabled(true);
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        R.drawable.sin_perfil);
                ImageView img=(ImageView)getSupportActionBar().getCustomView().findViewById(R.id.fotoPerfil_circular);
                img.setImageDrawable(redondear_imagen(icon));
            }

        });
    }

    /**
     * Metodo que devuelve una imagen sin esquinas y redondeada
     * @param image bitmap de la imagen a redondear
     * @return imagen redondeada
     */
    public RoundedBitmapDrawable redondear_imagen(Bitmap image){
        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), image);

        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(image.getHeight());

        return roundedDrawable;
    }
    //TOOLBAR

    /**
     * Infla una tollbar personalizada
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_chat,menu);
        return true;
    }

    /**
     * Establece una opcion en la toolbar llamada perfil. Iniciara la actividad perfil.class que
     * sera el perfil del usuario con el que se conversa
     * @param item opciones del menu
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.perfil){
            FirebaseDatabase database02 = FirebaseDatabase.getInstance();
            DatabaseReference myRef02=null;
            String[] telefonosPorSeperado=nombreChat.split("_");
            if(numeroPropio.equalsIgnoreCase(telefonosPorSeperado[0])){
                myRef02 = database02.getReference("users/" +telefonosPorSeperado[1]);
                numeroDestino=telefonosPorSeperado[1];
            }else{
                myRef02 = database02.getReference("users/" +telefonosPorSeperado[0]);
                numeroDestino=telefonosPorSeperado[0];
            }
            myRef02.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String nombre=dataSnapshot.child("nombre").getValue().toString();
                    String estado=dataSnapshot.child("estado").getValue().toString();

                    Intent i=new Intent(getApplicationContext(),Perfil.class);
                    i.putExtra("nombre",nombre);
                    i.putExtra("telefono",numeroDestino);
                    i.putExtra("estado",estado);
                    startActivity(i);

                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * establece el numero local del telefono en la variable global telefonopropio
     */
    public void getTelefono(){

        numeroPropio= tMgr.getLine1Number();
        //LANZO EL SERVICIO DE NOTIFICACIONES
        Intent intent2 = new Intent(this, Servicio_notificaciones.class);
        startService(intent2);
    }

    /**
     * Iniciara la actividad perfil.class que sera el perfil del usuario con el que se conversa
     */
    public void cargarPerfilDesdeFoto(){
        FirebaseDatabase database02 = FirebaseDatabase.getInstance();
        DatabaseReference myRef02=null;
        String[] telefonosPorSeperado=nombreChat.split("_");
        if(numeroPropio.equalsIgnoreCase(telefonosPorSeperado[0])){
            myRef02 = database02.getReference("users/" +telefonosPorSeperado[1]);
            numeroDestino=telefonosPorSeperado[1];
        }else{
            myRef02 = database02.getReference("users/" +telefonosPorSeperado[0]);
            numeroDestino=telefonosPorSeperado[0];
        }
        myRef02.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nombre=dataSnapshot.child("nombre").getValue().toString();
                String estado=dataSnapshot.child("estado").getValue().toString();

                Intent i=new Intent(getApplicationContext(),Perfil.class);
                i.putExtra("nombre",nombre);
                i.putExtra("telefono",numeroDestino);
                i.putExtra("estado",estado);
                startActivity(i);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
