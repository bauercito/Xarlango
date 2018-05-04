package com.example.cpv.chatpruebas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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


}
