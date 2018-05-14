package com.example.cpv.chatpruebas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.BitSet;

public class Perfil extends AppCompatActivity {
    String nombre;
    String telefono;
    String estado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfil);

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

    //ABRO NUEVO CHAT CON EL BUTTON
    public void abrir_chat(View v){
        String numero=getTelefonoPropio() ;
        String nombreChat=numero+"_"+telefono;
        Intent i=new Intent(this,Chat.class);
        i.putExtra("nombreChat",nombreChat);
        i.putExtra("numero_usuario",numero);
        i.putExtra("numero_destino",telefono);
        startActivity(i);
    }

    //REVISO SI EXISTE ALGUN CHAT, SI LO HAY NO DEJO QUE ABRA CHAT
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

    //CONSIGO TELEFONO PROPIO
    public String getTelefonoPropio(){
        TelephonyManager tMgr=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    //CARGAR LA FOTO EN EL IMAGEVIEW
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
    //redondear imagen
    public RoundedBitmapDrawable redondear_imagen(Bitmap image){
        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), image);

        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(image.getHeight());

        return roundedDrawable;
    }


}
