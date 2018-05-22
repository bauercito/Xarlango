package com.example.cpv.xarlango;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cpv.chatpruebas.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class AdaptadorListaChat extends BaseAdapter {
    ArrayList<String> chats=new ArrayList();
    Activity actividad;
    String numero;

    public AdaptadorListaChat(Activity actividad, ArrayList<String> chats,String numero){
        this.chats=chats;
        this.actividad=actividad;
        this.numero=numero;
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Object getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) actividad.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.listview_chats, null);
        }

        //PONER NOMBRE A CADA CHAT
        final TextView title = (TextView) v.findViewById(R.id.titulo);
        String[] numeroOtro=chats.get(position).split("_");
        DatabaseReference myRef01;
        if(numero.equalsIgnoreCase(numeroOtro[0])){
            myRef01 = database.getReference("users/"+numeroOtro[1]+"/nombre");
        }else{
            myRef01 = database.getReference("users/"+numeroOtro[0]+"/nombre");
        }

        myRef01.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nombre=dataSnapshot.getValue().toString();
                title.setText(nombre);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("no", "Failed to read value.", error.toException());
            }
        });

        //SACAR Y PONER ULTIMA CONVERSACION EN CADA CHAT
        final TextView description = (TextView) v.findViewById(R.id.subtitulo);
        final TextView fecha = (TextView) v.findViewById(R.id.fecha);
        DatabaseReference myRef02 = database.getReference("message/"+chats.get(position).toString());
        myRef02.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    String[] fechaFormateada=child.getKey().toString().split(":");
                    fecha.setText(fechaFormateada[0]+":"+fechaFormateada[1]+":"+fechaFormateada[2]);
                    description.setText(child.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("no", "Failed to read value.", error.toException());
            }
        });

        return v;
    }
}
