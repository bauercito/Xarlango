package com.example.cpv.chatpruebas;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Buscar_contactos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buscar_contactos);
        obtenerPermisos();


    }

    //LEER LOS CONTACTOS Y PONERLOS EN EL LISTvIEW
    public void leerContactos() {
        ArrayList<Contacto> telefonos = new ArrayList();
        String phone = null;
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            // get the phone number
            Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (pCur.moveToNext()) {
                phone = pCur.getString(
                        pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            if (phone != null) {
                telefonos.add(new Contacto(name, phone));
            }
            pCur.close();

        }
        cursor.close();
        rellenarListaContactos(telefonos);

    }

    //RELLENAR LISTVIEW
    public void rellenarListaContactos(final ArrayList<Contacto> listaContactos) {
        ListView lista = findViewById(R.id.lista_contactos01);
        AdaptadorListaContactos adaptador = new AdaptadorListaContactos(this, listaContactos);
        lista.setAdapter(adaptador);


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nombreChat = listaContactos.get(position).toString();
                /*Intent i=new Intent(getApplicationContext(),Chat.class);
                i.putExtra("nombreChat",nombreChat);
                i.putExtra("numero_usuario",numero);
                String nombreDestino[]=nombreChat.split("_");
                i.putExtra("numero_destino",nombreDestino[1]);
                startActivity(i);*/
            }
        });
    }

    //OBTENER PERMISOS PARA RECOLECTAR CONTACTOS
    public void obtenerPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 0);
        } else {
            leerContactos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    leerContactos();
                } else {
                    Toast.makeText(this, "Necesitas dar permisos", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_buscar_contactos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.searchUsuario) {
            //MUESTRO DIALOG
            LayoutInflater linf = LayoutInflater.from(this);
            final View inflator = linf.inflate(R.layout.search_user_dialog02, null);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Tilte");
            alert.setMessage("Message");
            alert.setView(inflator);

            final EditText et1 = (EditText) inflator.findViewById(R.id.telefono_contacto);

            alert.setPositiveButton("Buscar usuario", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    String s1=et1.getText().toString();
                    buscar_usuario(s1);
                }
            });

            alert.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            });

            alert.show();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void buscar_usuario(String numero) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+numero);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String nombre=dataSnapshot.child("nombre").getValue().toString();
                    String telefono=dataSnapshot.getKey().toString();
                    iniciarPerfil(nombre,telefono);

                    //Toast.makeText(Buscar_contactos.this, "Existe", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Buscar_contactos.this, "No existe", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void iniciarPerfil(String nombre,String telefono){
        Intent i=new Intent(this,Perfil.class);
        i.putExtra("nombre",nombre);
        i.putExtra("telefono",telefono);
        startActivity(i);
    }
}
