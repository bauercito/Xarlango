package com.example.cpv.chatpruebas;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {
    String nombreChat;
    String numeroOrigen;
    String numeroDestino;
    AdaptadorChatPersonalizado adaptador;
    ListView lista;
    ArrayList<String> palabras;
    ArrayList<String> persona;
    String[] hora_contacto;
    ArrayList<String> hora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recoger datos
        Bundle extras = getIntent().getExtras();
        nombreChat = extras.getString("nombreChat");
        numeroOrigen = extras.getString("numero_usuario");
        numeroDestino = extras.getString("numero_destino");

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

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void restablecerListView(ArrayList<String> palabras,ArrayList<String> persona,ArrayList<String> hora) {
        /*RecyclerView vista=(RecyclerView)findViewById(R.id.recyclerView);
        vista.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        vista.setAdapter(new AdaptadorRecycleChat(palabras,persona,numeroOrigen,this,hora));*/

        adaptador=new AdaptadorChatPersonalizado(palabras,persona,numeroOrigen,this,hora);

        lista.setAdapter(adaptador);
        lista.setSelection(palabras.size() - 1);

    }


    public void enviar(View enviar) {
        TextView input = findViewById(R.id.texto1);
        if(input.getText().toString().equalsIgnoreCase("")){
            Toast toast = Toast.makeText(getApplicationContext(), "Escribe algo!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP,0,0);
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

            input.setText("");
        }


    }

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
    public String obtenerFecha2(){
        Date date=new Date();
        SimpleDateFormat OutPutFormat = new SimpleDateFormat(
                "dd-M-yyyy HH:mm:ss:SSS", java.util.Locale.getDefault());
        return OutPutFormat.format(date);
    }

}
