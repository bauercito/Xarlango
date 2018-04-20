package com.example.cpv.chatpruebas;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Land_activity extends AppCompatActivity {

    String numero;
    TelephonyManager tMgr;
    String nombre;

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
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
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
    //¿USUARIO EXISTE?
    public void buscarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        //EMPIEZA POR AKI nombre=myRef.get
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equalsIgnoreCase(numero)){
                        Toast.makeText(Land_activity.this, "Bienvenido "+child.child("nombre").getValue().toString(), Toast.LENGTH_LONG).show();
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
    //CONSEGUIR NOMBRE USUARIO
    public void getNombre() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog01, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        nombre=editText.getText().toString();
                        registrarUsuario();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //REGISTRAR AL USUARIO
    public void registrarUsuario(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+numero);
        myRef.child("nombre").setValue(nombre);

    }
//------------------------------------------------------------------------------------
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
            startActivity(i);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}
