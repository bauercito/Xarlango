package com.example.cpv.chatpruebas;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        //recoger datos
        Bundle extras = getIntent().getExtras();
        nombreChat = extras.getString("nombreChat");
        numeroOrigen = extras.getString("numero_usuario");
        numeroDestino = extras.getString("numero_destino");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LEER DATOS
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message/" + nombreChat);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);

                ArrayList<String> palabras = new ArrayList();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    palabras.add(child.getValue().toString());
                }
                restablecerListView(palabras);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("no", "Failed to read value.", error.toException());
            }
        });
    }

    public void restablecerListView(ArrayList<String> palabras) {
        ListView lista;
        lista = (ListView) findViewById(R.id.listview);
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, palabras);
        lista.setAdapter(adaptador);
        lista.setSelection(palabras.size() - 1);
    }


    public void enviar(View enviar) {
        TextView input = findViewById(R.id.texto1);
        //Se crea el chat en message
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message/" + nombreChat + "/" + obtenerFecha());
        myRef.setValue(input.getText().toString());


        //se crea los metadatos en el usuario emisor y receptor
        DatabaseReference myRef01 = database.getReference("users/" + numeroOrigen + "/chats/" + nombreChat + "/" + "ultimo_uso");
        myRef01.setValue(obtenerFecha());
        DatabaseReference myRef02 = database.getReference("users/" + numeroDestino + "/chats/" + nombreChat + "/" + "ultimo_uso");
        myRef02.setValue(obtenerFecha());

        /*FirebaseDatabase database02 = FirebaseDatabase.getInstance();
        DatabaseReference myRef02 = database02.getReference("users/"+numeroDestino+"/chats/"+nombreChat);
        myRef02.push().setValue(input.getText().toString());*/
        input.setText("");

    }

    public String obtenerFecha() {
        String[] hosts = new String[] {"cuco.rediris.es"};

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
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date); //tuFechaBase es un Date;
                calendar.add(Calendar.HOUR,   6); //horasASumar es int.
                String out = OutPutFormat.format(calendar.getTime());
                return out;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client.close();
        return null;

    }

}
