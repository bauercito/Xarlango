package com.example.cpv.xarlango.actividades;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cpv.chatpruebas.R;
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

/**
 * Clase que extiende de Activity. Se centrara en mostrar ciertos datos recogidos del servidor como
 * foto, nombre, estado, numero de telefono. Esta actividad solo mostrara datos de un usuario
 * destino que quiera ver el usuario local
 */
public class Perfil extends AppCompatActivity {
    String nombre;//nombre del usuario destino
    String telefono;//telefono del usuario destino
    String estado;//estado del usuario destino
    private BroadcastReceiver mMessageReceiver = null; //broadcastReciver para poner a la escucha el servicio de conexion permanente

    /**
     * Metodo de entrada cuando se crea la actividad. Este metodo cargara el layout al cual esta
     * asiciado. Pondra a la escucha un BroadcastReciver para poder tener el servicio de conexion
     * permanente. Establecera y personalizara la toolbar. Tambien cargara incialmente todos los
     * datos del servidor del usuario destino (foto,estado,nombre,numero). Por ultimo comprobara
     * en el servidor si este usuario tiene un chat abierto con el usuario destino. Si no es asi
     * le dejara abrir una nueva conversacion con el
     * @param savedInstanceState paquete bundle con datos primitivos
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

        //RECIBO MENSAJE DE DESCONEXION
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Conexion_dialog dialog = new Conexion_dialog();
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(),"tag");
            }
        };

        BitmapDrawable background = new BitmapDrawable(
                BitmapFactory.decodeResource(getResources(),
                        R.drawable.fondo_estandar));
        getSupportActionBar().setBackgroundDrawable(background);

        Bundle extras = getIntent().getExtras();
        nombre=extras.getString("nombre");
        telefono=extras.getString("telefono");
        estado=extras.getString("estado");

        cargarFoto();

        TextView nombre_perfil=findViewById(R.id.nombre_perfil);
        TextView telefono_perfil=findViewById(R.id.telefono_perfil);
        TextView estado_perfil=findViewById(R.id.descripcion_contacto);

        nombre_perfil.setText(nombre);
        telefono_perfil.setText(telefono);
        switch (Integer.parseInt(estado)){
            case 0:
                estado_perfil.setText("Disponible");
                break;
            case 1:
                estado_perfil.setText("No disponible");
                break;
            case 2:
                estado_perfil.setText("En una reunion");
                break;
            case 3:
                estado_perfil.setText("De copas");
                break;
            case 4:
                estado_perfil.setText("Estudiando");
                break;
            case 5:
                estado_perfil.setText("No me hables");
                break;
            case 6:
                estado_perfil.setText("Con gente importante");
                break;
        }


        comprobarExisteChat(telefono);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Metodo que sera llamado si la aplicacion se encuentra en primer plano. Pondra a la escucha el
     * servicio  de notificaciones a traves de un BroadcastReciver. Tambiene stablecera una variable
     * estatica para el apoyo de este
     */
    @Override
    public void onResume() {
        super.onResume();
        Servicio_notificaciones.ESTADOAPP=false;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));
    }

    /**
     * Metodo que sera llamado si la aplicacion se encuentra en segundo plano. Dejara de registrar
     * el broadCastReciver del servicio de notificaciones
     */
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    /**
     * Metodo que es llamado cuando la aplicacion se encuentra en segundo plano. Modifica una
     * variable estatica.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Servicio_notificaciones.ESTADOAPP=true;
    }

    /**
     * Metodo que se encuentra a la escucha de una ImageView. Inicia la actividad de chat.class.
     * Enviara por un bundle a la nueva actividad el nombre del chat, numero y telefono
     * @param v vista del layout
     */
    public void abrir_chat(View v){
        String numero=getTelefonoPropio() ;
        String nombreChat=numero+"_"+telefono;
        Intent i=new Intent(this,Chat.class);
        i.putExtra("nombreChat",nombreChat);
        i.putExtra("numero_usuario",numero);
        i.putExtra("numero_destino",telefono);
        startActivity(i);
    }



    /**
     * Metodo que es llamado al inicio de la actividad. Comprueba en el servidor si la persona
     * destino y la local ya tienen una conversacion registrada en el servidor. Si no la tiene
     * dejara al usuario abrir una conversacion con el usuario destino
     * @param telefono_perfil telefono del usuario destino
     */
    public void comprobarExisteChat(final String telefono_perfil){
        final String telefonoPropio=getTelefonoPropio();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equalsIgnoreCase(telefono_perfil+"_"+telefonoPropio)||child.getKey().equalsIgnoreCase(telefonoPropio+"_"+telefono_perfil)){
                        findViewById(R.id.abrirChat).setVisibility(View.INVISIBLE);
                        findViewById(R.id.chatAbierto).setVisibility(View.VISIBLE);
                        return;
                    }else{
                        findViewById(R.id.abrirChat).setVisibility(View.VISIBLE);
                        findViewById(R.id.chatAbierto).setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Metodo que recoge el numero de telefono personal del usuario
     * @return devielve el numero de telefono del usuario
     */
    public String getTelefonoPropio(){
        TelephonyManager tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    /**
     * Metodo llamado al inicio de la actividad. Establece en un imageView la foto elegida por el
     * usuario destino. Sera recogida del servidor e implementada en el layout
     */
    public void cargarFoto(){
        //RECUPERO LA FOTO DE FIREBASE
        StorageReference storageRef =FirebaseStorage.getInstance().getReference();
        StorageReference Ref = storageRef.child(telefono+".jpg");
        final long ONE_MEGABYTE = 1024 * 1024;
        Ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView image = (ImageView) findViewById(R.id.imageView);
                image.setImageDrawable(redondear_imagen(bmp));
                //image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),image.getHeight(), false));
                findViewById(R.id.carga_perfil_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                findViewById(R.id.carga_perfil_layout).setVisibility(View.INVISIBLE);
                ImageView image = (ImageView) findViewById(R.id.imageView);
                image.buildDrawingCache();
                Bitmap bmap = image.getDrawingCache();
                image.setImageDrawable(redondear_imagen(bmap));
                image.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Metodo que redondea la imagen pasada por parametros
     * @param image imagen bitmap para rendondear
     * @return
     */
    public RoundedBitmapDrawable redondear_imagen(Bitmap image){
        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), image);

        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(image.getHeight());

        return roundedDrawable;
    }


}
