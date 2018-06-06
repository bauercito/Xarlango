package com.example.cpv.xarlango.actividades;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;


import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.fragments.Perfil_fragment;
import com.example.cpv.xarlango.fragments.Sin_chats;
import com.example.cpv.xarlango.fragments.Usuarios_fragment;
import com.example.cpv.xarlango.gui.Conexion_dialog;
import com.example.cpv.xarlango.gui.NoDialog_dialog;
import com.example.cpv.xarlango.servicios.Service_conexionPermanente;
import com.example.cpv.xarlango.servicios.Servicio_notificaciones;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * clase que extiende de Activity. Establece la actividad principal de la aplicacion. Estara
 * compuesta por dos fragments, las cuales el usuario podra ir intercambiando entre una y otra
 */
public class Land_activity extends AppCompatActivity implements Usuarios_fragment.OnFragmentInteractionListener,Perfil_fragment.OnFragmentInteractionListener,Sin_chats.OnFragmentInteractionListener {

    String numero; //numero de telefono propio
    TelephonyManager tMgr; //necesario para recoger el numero propio
    String nombre; //nombre del usuario local
    String estado; //estado del usuario local
    private BroadcastReceiver mMessageReceiver = null; //para poner a la escucha la conexion
    // permanente a internet

    /**
     * Metodo de entrada de la actividad. Establecera la primera actividad de la aplicacion.
     * Se inciara el servicio de conexion permanente y podra a la escucha un broadcastReciver
     * para atender las peticiones de desconexion a internet. Establece una toolbar personalizada
     * y establece como primer fragment la lista de conversaciones del usuario. Tambien antes
     * de mostrar nada, comprobara si el usuario esta registrado en el servidor. Si la respuesta
     * fuese negativa solicitara al usuario que se registre
     * @param savedInstanceState bundle con datos primitivos
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_activity);

        //ACTIVAR SERVICIO
        Intent intent = new Intent(this, Service_conexionPermanente.class);
        startService(intent);



        //RECIBO MENSAJE DE DESCONEXION
            mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Conexion_dialog dialog = new Conexion_dialog();
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(),"tag");
            }
        };

        //PONER FONDO A LA TOOLBAR
        BitmapDrawable background = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(),
                        R.drawable.fondo_estandar));
        getSupportActionBar().setBackgroundDrawable(background);


        tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /*1º*/getTelefono();
       /*2º*/ buscarUsuario();
       /*3º*/Usuarios_fragment listaFragmento=new Usuarios_fragment();
            Bundle extras=new Bundle();
            extras.putString("numero",numero);
            extras.putString("nombre",nombre);
            listaFragmento.setArguments(extras);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout3, listaFragmento); //donde fragmentContainer_id es el ID del FrameLayout donde tu Fragment está contenido.
            fragmentTransaction.commit();

    }

    /**
     * Metodo sobreescrito que entra cuando la actividad pasa a primer plano. Registrara el
     * broadcastreciver de la conexion permanente y establecera una variable estatica para
     * el servicio de notificaciones
     */
    @Override
    public void onResume() {
        Servicio_notificaciones.ESTADOAPP=false;
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));
    }

    /**
     * Metodo sobrescrito que entrara cuando la actividad pase a segundo plano. Establecera una
     * variable estatica para el servicio de notificaciones
     */
    @Override
    protected void onPause() {
        super.onPause();
        Servicio_notificaciones.ESTADOAPP=true;
    }

    /**
     * Metodo sobrescrito que sera llamado cuando la actividad pase a segundo plano. Dejara de poner
     * a la escucha el boradcastReciver de la conexion permanente
     */
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    // 1º CONSIGO EL NUMERO DE TELEFONO DEL USUARIO
    /**
     * Compruebo si la aplicacion tiene permisos para leer el el estado del telefono y poder recoger
     * el numero de telefono. Si tiene permisos llamara al metodo setTelefono() sino los solicitara
     * al usuario
     */
    public void getTelefono() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},0);

        }else{
            setTelefono();
        }

    }

    /**
     * Metodo que establece el numero de telefono del usuario local. Tambien incia el servicio
     * de notificaciones
     */
    public void setTelefono(){

        numero= tMgr.getLine1Number();
        //LANZO EL SERVICIO DE NOTIFICACIONES
        Intent intent2 = new Intent(this, Servicio_notificaciones.class);
        startService(intent2);
    }

    /**
     * Metodo sobreescrito que pide permisos a los usuarios de lectura del estado del telefono. Si
     * el permiso es denegado cierra la aplicacion. Si no llama al emtodo SetTelefono()
     * @param requestCode numero de peticion de permiso
     * @param permissions tipo de permiso
     * @param grantResults resultado de permiso
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 0:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    setTelefono();
                }else{
                    finish();
                }
        }
    }

//----------------------------------------------------------------------------
    /* 2º*/ //¿USUARIO EXISTE?
    /**
     * Metodo que busca un usuario en la base de datos del servidor. Si existe llama al metodo
     * getNombre(). Si no lo encuentra sale del metodo y la ejecucion continua en el metodo
     * onCreate()
     */
    public void buscarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equalsIgnoreCase(numero)){
                        nombre=child.child("nombre").getValue().toString();
                        //estado=child.child("estado").getValue().toString();
                        return;
                    }
                }
                getNombre();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    //CONSEGUIR NOMBRE USUARIO SI NO EXISTE

    /**
     * Metodo que infla un dialog personalizado pidiendo al usuario un nombre con el que iniciar la
     * aplicacion. Establece los requisitos para que el usuario introduzca dicho dato. Si introduce
     * un nombre correcto se llama al metodos registrarUusuario()
     */
    public void getNombre() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog01, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setCancelable(false);
        final EditText editText = (EditText) promptView.findViewById(R.id.nombre_usuario);
        //final EditText estadoEditText = (EditText) promptView.findViewById(R.id.estado_usuario);
        ImageView empezarImagen=(ImageView) promptView.findViewById(R.id.boton_empezar);
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        empezarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().equals("")){
                    editText.setHint("Introduce un nombre!");
                }else{
                    nombre=editText.getText().toString();
                    //estado=estadoEditText.getText().toString();
                    registrarUsuario();
                    alert.cancel();
                }
            }
        });
        // VENTANA DE SALIR SI NO LO TIENE

        if(numero==null){
            NoDialog_dialog dialog = new NoDialog_dialog();
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(),"tag");
        }

    }

    /**
     * Metodo que registrara al usuario en la base de datos del servidor. Paso psoterior a que el
     * usuario haya metido su nombre en un dialog. Tabmien llenara valores e estado con un
     * valor inicial de 0
     */
    public void registrarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+numero);
        myRef.child("nombre").setValue(nombre);
        myRef.child("estado").setValue("0");


    }
//------------------------------------------------------------------------------------

    /**
     * Metodo que esta a la escucha de un ImageView. Al ser llamado cambiara de faragment a
     * Perfil_fragment. Este sera incializado enviandole un paquete bundle con datos como
     * nombre, estado y numero.
     * @param v vista del layout
     */
    public void perfil(View v){
        Perfil_fragment perfilFragmento=new Perfil_fragment();
        Bundle extras=new Bundle();
        extras.putString("numero",numero);
        extras.putString("nombre",nombre);
        extras.putString("estado",estado);
        perfilFragmento.setArguments(extras);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout3, perfilFragmento).addToBackStack(null); //donde fragmentContainer_id es el ID del FrameLayout donde tu Fragment está contenido.
        fragmentTransaction.commit();
        findViewById(R.id.imageView3).setBackgroundResource(R.drawable.rounded_imagen);
        findViewById(R.id.imageView2).setBackgroundResource(R.drawable.sin_rounded_imagen);
    }

    /**
     * Metodo que esta a la escucha de un ImageView. Al ser llamado cambiara de faragment a
     * Usuario_fragment. Este sera incializado enviandole un paquete bundle con datos como
     * nombre, estado y numero.
     * @param v vista del layout
     */
    public void chat(View v){
        Usuarios_fragment listaFragmento=new Usuarios_fragment();
        Bundle extras=new Bundle();
        extras.putString("numero",numero);
        extras.putString("nombre",nombre);
        listaFragmento.setArguments(extras);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout3, listaFragmento); //donde fragmentContainer_id es el ID del FrameLayout donde tu Fragment está contenido.
        fragmentTransaction.commit();
        findViewById(R.id.imageView2).setBackgroundResource(R.drawable.rounded_imagen);
        findViewById(R.id.imageView3).setBackgroundResource(R.drawable.sin_rounded_imagen);
    }

//---------------------------------------------------------------------------------
    //TOOLBAR

    /**
     * Metodo que infla un layaout para la toolbar
     * @param menu objeto toolbar
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_usuarios,menu);
        return true;
    }

    /**
     * Establece la opcion aniadirUsuario de la toolbar. Llamara a la actividad Buscar_contactos.
     * class. Enviara a traves de un bundele el estado a la nueva actividad
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        int id=item.getItemId();
        if(id==R.id.aniadirUsuario) {
            i = new Intent(this, Buscar_contactos.class);
            i.putExtra("estado", estado);
            startActivity(i);
            return true;
        }else if(id==R.id.ayuda)   {
            Uri uri = Uri.parse("https://1drv.ms/b/s!Ag1iPiMJ6kz1qX352CwgCqZa-WZb");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Metodo sin usar
     * @param uri uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
