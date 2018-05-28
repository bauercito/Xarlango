package com.example.cpv.xarlango;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.cpv.chatpruebas.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Buscar_contactos extends AppCompatActivity {
    ArrayList<Contacto> telefonos;
    String estado;
    boolean dentro=false;
    private BroadcastReceiver mMessageReceiver = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buscar_contactos);

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

        telefonos=new ArrayList();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new hilo_cargar_contactos().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }

    @Override
    public void onResume() {
        servicio_notificaciones.ESTADOAPP=false;
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("EVENT_SNACKBAR"));
    }
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        servicio_notificaciones.ESTADOAPP=true;
    }

    //LEER LOS CONTACTOS Y PONERLOS EN EL LISTvIEW
    public void leerContactos() {
        String phone = null;
        final Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            final String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            // get the phone number
            final Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (pCur.moveToNext()) {
                phone = pCur.getString(
                        pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            if (phone != null && !phone.contains(".")&&!phone.contains("#")&&!phone.contains("$")&&!phone.contains("[")&&!phone.contains("]")) {
                //-------------------------------------------------------
                if (!phone.contains("+34")) {
                    phone="+34"+phone;
                }
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/"+phone);
                final String finalPhone = phone;
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            estado=dataSnapshot.child("estado").getValue().toString();
                            telefonos.add(new Contacto(name, finalPhone,estado));
                            pCur.close();
                            //cursor.close();
                            listaContactosLocal_usuariosFireBase(telefonos);
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
            pCur.close();
        }
        if(telefonos.size()==0){
            dentro=true;

        }


    }
    //CONTRASTAR CONTACTOS CON USUARIOS DE LA BASE DE DATOS
    public void listaContactosLocal_usuariosFireBase(final ArrayList<Contacto> contactos){


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Contacto> listaDefinitivaFireBase=new ArrayList();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (int i=0;i<contactos.size();i++){
                        if(child.getKey().equalsIgnoreCase(contactos.get(i).getNumero().trim())||child.getKey().equalsIgnoreCase("+34"+contactos.get(i).getNumero().trim())){
                            listaDefinitivaFireBase.add(contactos.get(i));
                        }
                    }
                }
                if(listaDefinitivaFireBase.size()==0){
                    RelativeLayout rl=(RelativeLayout)findViewById(R.id.sin_contactos);
                    rl.setVisibility(View.VISIBLE);
                }else{
                    rellenarListaContactos(listaDefinitivaFireBase);
                    RelativeLayout rl=(RelativeLayout)findViewById(R.id.sin_contactos);
                    rl.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       /* if(!entro[0]){

            RelativeLayout rl=(RelativeLayout)findViewById(R.id.sin_contactos);
            rl.setVisibility(View.VISIBLE);
        }*/
    }


    //RELLENAR LISTVIEW
    public void rellenarListaContactos(final ArrayList<Contacto> listaContactos) {
        ListView lista = findViewById(R.id.lista_contactos01);
        lista.setDivider(null);
        AdaptadorListaContactos adaptador = new AdaptadorListaContactos(this, listaContactos);
        lista.setAdapter(adaptador);


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nombre = listaContactos.get(position).getNombre();
                String telefono = listaContactos.get(position).getNumero();
                Intent i=new Intent(getApplicationContext(),Perfil.class);
                i.putExtra("nombre",nombre);
                i.putExtra("telefono",telefono);
                i.putExtra("estado",listaContactos.get(position).getEstado());
                startActivity(i);
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
            alert.setView(inflator);

            final EditText texto= (EditText) inflator.findViewById(R.id.telefono_contacto);
            texto.setText("+34 ");
            texto.setSelection(texto.getText().length());

            alert.setPositiveButton("Buscar usuario", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    String respu=texto.getText().toString();
                    if(respu.equalsIgnoreCase("")||respu.equalsIgnoreCase("+34 ")){
                        dialog.dismiss();
                        Toast.makeText(Buscar_contactos.this, "Introduce algun numero de telefono", Toast.LENGTH_SHORT).show();
                    }else{
                        buscar_usuario(respu.replaceAll(" ",""));
                    }
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
                    String estado=dataSnapshot.child("estado").getValue().toString();
                    String telefono=dataSnapshot.getKey().toString();
                    iniciarPerfil(nombre,telefono,estado);
                }else{
                    Toast.makeText(Buscar_contactos.this, "El usuario no se encuentra en Xarlango", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void iniciarPerfil(String nombre,String telefono,String estado){
        Intent i=new Intent(this,Perfil.class);
        i.putExtra("nombre",nombre);
        i.putExtra("telefono",telefono);
        i.putExtra("estado",estado);
        startActivity(i);
    }

     //hilos
    private class hilo_cargar_contactos extends AsyncTask<Void, Integer, Void>

    {
        @Override
        protected Void doInBackground(Void... params)
        {
            obtenerPermisos();
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            RelativeLayout rl=(RelativeLayout)findViewById(R.id.carga_contactos_layout);
            rl.setVisibility(View.INVISIBLE);
            if(dentro){
                RelativeLayout rl2=(RelativeLayout)findViewById(R.id.sin_contactos);
                rl2.setVisibility(View.VISIBLE);
            }
        }
    }

}
