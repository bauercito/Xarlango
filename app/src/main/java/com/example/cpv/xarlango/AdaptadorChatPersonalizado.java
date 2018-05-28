package com.example.cpv.xarlango;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cpv.chatpruebas.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdaptadorChatPersonalizado extends BaseAdapter {
    ArrayList<String>conversacion=new ArrayList();
    ArrayList<String>persona=new ArrayList();
    ArrayList<String> hora;
    String telefonoPropio;
    Context actividad;

    public AdaptadorChatPersonalizado(ArrayList<String> conversacion,ArrayList<String> persona,String telefonoPropio,Context actividad,ArrayList<String> hora){
        this.conversacion=conversacion;
        this.persona=persona;
        this.telefonoPropio=telefonoPropio;
        this.actividad=actividad;
        this.hora=hora;
    }
    @Override
    public int getCount() {
        return conversacion.size();
    }
    @Override
    public Object getItem(int position) {
        if(conversacion!=null){
            return conversacion.get(position);
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater)actividad.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listview_conversacion, null);

        }else{
            TextView yo=(TextView)v.findViewById(R.id.yo);
            TextView tu=(TextView)v.findViewById(R.id.tu);
            TextView hora_yo=(TextView)v.findViewById(R.id.hora_yo);
            TextView hora_tu=(TextView)v.findViewById(R.id.hora_tu);
            yo.setText("");
            tu.setText("");
            hora_yo.setText("");
            hora_tu.setText("");
        }
        TextView yo=(TextView)v.findViewById(R.id.yo);
        TextView tu=(TextView)v.findViewById(R.id.tu);
        TextView hora_yo=(TextView)v.findViewById(R.id.hora_yo);
        TextView hora_tu=(TextView)v.findViewById(R.id.hora_tu);


        String[] formato=hora.get(position).split(" ");
        String[] horaFormateada=formato[1].split(":");

        //poner chats
        if(persona.get(position).equalsIgnoreCase(telefonoPropio)){
            yo.setVisibility(View.VISIBLE);
            tu.setVisibility(View.INVISIBLE);
            yo.setText(conversacion.get(position).toString());
            hora_yo.setText(horaFormateada[0]+":"+horaFormateada[1]+"    "+formato[0]);
        }else{
            yo.setVisibility(View.INVISIBLE);
            tu.setVisibility(View.VISIBLE);
            tu.setText(conversacion.get(position).toString());
            hora_tu.setText(horaFormateada[0]+":"+horaFormateada[1]+"    "+formato[0]);
        }
        return v;
    }
}
