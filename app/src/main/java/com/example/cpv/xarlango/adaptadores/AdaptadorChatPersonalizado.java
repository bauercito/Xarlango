package com.example.cpv.xarlango.adaptadores;

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

/**
 * clase que hereda de BaseAdapter y que realiza la funcion de adaptador para el listview de msotrar el listado de las
 * conversaciones en la actividad Land_activity. En este se encontraran los metodos que cargaran
 * un layout especifico y listara conversacion por conversacion que tenga el usuario añadido
 * al servidor
 */
public class AdaptadorChatPersonalizado extends BaseAdapter {
    ArrayList<String>conversacion=new ArrayList(); //lista de conversaciones del usuario
    ArrayList<String>persona=new ArrayList(); // lista de personas con las que tiene conversaciones el usuario
    ArrayList<String> hora; //hora de registro de las conversaciones
    String telefonoPropio; //telefono del usuario local
    Context actividad; //contexto de la actividad

    /**
     * Contructor que inicializara las variables globales por los parametros pasados
     * @param conversacion lista de conversaciones del usuario
     * @param persona lista de personas con las que tiene conversaciones el usuario
     * @param telefonoPropio telefono del usuario local
     * @param actividad contexto de la actividad
     * @param hora hora de registro de las conversaciones
     */
    public AdaptadorChatPersonalizado(ArrayList<String> conversacion,ArrayList<String> persona,String telefonoPropio,Context actividad,ArrayList<String> hora){
        this.conversacion=conversacion;
        this.persona=persona;
        this.telefonoPropio=telefonoPropio;
        this.actividad=actividad;
        this.hora=hora;
    }

    /**
     * Metodo que devuelve el numero de conversaciones registradas en el servidor
     * @return numero de conversaciones
     */
    @Override
    public int getCount() {
        return conversacion.size();
    }

    /**
     * Metodo que devuelve el objeto de la posicion  en la que se encuentra el ArrayList de las
     * conversaciones recogidas en el servidor
     * @param position numero de posicion
     * @return numero de posicion
     */
    @Override
    public Object getItem(int position) {
        if(conversacion!=null){
            return conversacion.get(position);
        }
        return 0;
    }

    /**
     * Metodo que devuelve la posicion en la que se encuentra el ArrayList de las conversaciones
     * recogidas en el servidor
     * @param position numero de posicion
     * @return numero de posicion
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Metodo que inflara la vista con el layaout personalizado por cada elemento de la lista
     * proporcionada. En este caso sera una lista ArrayList. Establecera en cada elemento una serie
     * de datos recogidos del servidor para que puedan ser infladas y mostradas
     * @param position posicion
     * @param convertView vista del layaout inflado
     * @param parent padre de la vista del layout inflado
     * @return
     */
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
