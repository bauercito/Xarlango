package com.example.cpv.chatpruebas;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Land_activity extends AppCompatActivity implements Usuarios_fragment.OnFragmentInteractionListener,Perfil_fragment.OnFragmentInteractionListener,Sin_chats.OnFragmentInteractionListener {

    String numero;
    TelephonyManager tMgr;
    String nombre;
    String estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_activity);


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



    // 1º CONSIGO EL NUMERO DE TELEFONO DEL USUARIO
    public void getTelefono() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},0);

        }else{
            setTelefono();
        }

    }
    public void setTelefono(){
        numero= tMgr.getLine1Number();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(0){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    setTelefono();
                }else{
                    Toast.makeText(this, "Necesitas dar permisos", Toast.LENGTH_SHORT).show();
                }
        }
    }

//----------------------------------------------------------------------------
    /* 2º*/ //¿USUARIO EXISTE?
    public void buscarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equalsIgnoreCase(numero)){
                        nombre=child.child("nombre").getValue().toString();
                        estado=child.child("estado").getValue().toString();
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
    public void getNombre() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog01, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setCancelable(false);
        final EditText editText = (EditText) promptView.findViewById(R.id.nombre_usuario);
        final EditText estadoEditText = (EditText) promptView.findViewById(R.id.estado_usuario);
        ImageView empezarImagen=(ImageView) promptView.findViewById(R.id.boton_empezar);
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();
        empezarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().equals("")){
                    editText.setHint("Introduce un nombre!");
                }else if(estadoEditText.getText().toString().equals("")){
                    estadoEditText.setHint("¡Cuentanos algo!");
                }else{
                    nombre=editText.getText().toString();
                    estado=estadoEditText.getText().toString();
                    registrarUsuario();
                    alert.cancel();
                }
            }
        });
        // create an alert dialog

    }

    //REGISTRAR AL USUARIO
    public void registrarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+numero);
        myRef.child("nombre").setValue(nombre);
        myRef.child("estado").setValue(estado);


    }
//------------------------------------------------------------------------------------
    //Eventos onClick
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
    }
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
    }

//---------------------------------------------------------------------------------
    //TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_usuarios,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.aniadirUsuario){
            Intent i=new Intent(this,Buscar_contactos.class);
            i.putExtra("estado",estado);
            startActivity(i);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
