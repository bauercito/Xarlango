package com.example.cpv.xarlango.adaptadores;

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

/**
 * Clase que hereda de BaseAdapter y establece un adaptador para mostrar las conversaciones
 * en una conversacion de un usuario. Recogera todos los datos de la coversacion con el usuario
 * local y destino y los msotrara de manera ordenada en pantalla de la actividad
 */
public class AdaptadorListaChat extends BaseAdapter {
    ArrayList<String> chats=new ArrayList(); //listado de las conversaciones de un chat
    Activity actividad; //actividad que se usara para sacar el contexto de la misma
    String numero; //numero de telefono

    /**
     * Contructor que inicializara las variables globales a traves de los paramatros pasados
     * @param actividad actividad que se usara para sacar el contexto de la misma
     * @param chats listado de las conversaciones de un chat
     * @param numero numero de telefono
     */
    public AdaptadorListaChat(Activity actividad, ArrayList<String> chats,String numero){
        this.chats=chats;
        this.actividad=actividad;
        this.numero=numero;
    }

    /**
     * Metodo que devuelve el numero total del tamaño de la lista
     * @return devuelve el numero total del array
     */
    @Override
    public int getCount() {
        return chats.size();
    }

    /**
     * Devuelve el objeto de la posicion dada por el arrayList
     * @param position posicion del arrayList
     * @return objecto del arrayList
     */
    @Override
    public Object getItem(int position) {
        return chats.get(position);
    }

    /**
     * Metodo que devuelve la posicion del Arrylist por el que se encuentra el adaptador
     * @param position posicion del arrayList
     * @return posicion del arrayList
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Metodo que buscara en la base de datos todas las conversaciones que esten dentro de un chat y
     * las pondra een forma de lista en el Listview. Inflara un layaout personalizado para establecer
     * a traves de un textView esos datos recogidos. Tambien mostrara la fecha recogida en cada
     * elemento de la lista
     * @param position posicion en la que se encuentra el arrayList
     * @param convertView vista del layaout
     * @param parent padre de la vista del layout
     * @return devuelve la vista creada por el adaptador
     */
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
