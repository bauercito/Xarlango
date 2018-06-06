package com.example.cpv.xarlango.adaptadores;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.modelo.Contacto;

import java.util.ArrayList;

/**
 * Clase que hereda de BaseAdapter. Establece un adaptador para mostrar los contactos que se
 * encuentran en el servidor de los contactos locales del usuario. Estos datos locales se pasan
 * por un ArrayList a traves del contructor
 */
public class AdaptadorListaContactos extends BaseAdapter {
    ArrayList<Contacto> listaContactos=new ArrayList(); //lista de contactos locales del usuario local
    Activity actividad; //actividad para sacar el cotexto de la misma

    /**
     * Contructor ue inicializara las variables globales cuyos datos seran pasadas por parametros
     * @param actividad actividad para sacar el cotexto de la misma
     * @param listaContactos lista de contactos locales del usuario local
     */
    public AdaptadorListaContactos(Activity actividad,ArrayList<Contacto> listaContactos){
        this.listaContactos=listaContactos; //lista de contactos locales del usuario local
        this.actividad=actividad; //actividad para sacar el cotexto de la misma
    }

    /**
     * Metodo que devuelve el numero total de elementos de pasados por el ArrayList
     * @return numero total de contactos
     */
    @Override
    public int getCount() {
        return listaContactos.size();
    }

    /**
     * Metodo que devuelve el elemento del arrayList por posicion dada
     * @param position posicion del arrayList
     * @return objeto del ArrayList
     */
    @Override
    public Object getItem(int position) {
        return listaContactos.get(position);
    }

    /**
     * Metodo que devuelve el numero de la posicion por el que se encuentra el adaptador
     * @param position posicion del adaptador
     * @return numero de la posicion del adaptador
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Metodo que inflara el layaout personalizado para mostrar los elementos rescatados del
     * ArrayList. En el se cargaran el telefono y el nombre de los contactos contenedios en la
     * lista y devolvera una vista para que se vayan mostrando uno a uno
     * @param position posicion del arrayList
     * @param convertView vista del layaout
     * @param parent padre de la vista del layout
     * @return vista inflada
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout rl=(RelativeLayout)actividad.findViewById(R.id.carga_contactos_layout);
        rl.bringToFront();
        View v = convertView;
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) actividad.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.listview_contactos, null);
        }

        TextView nombre=v.findViewById(R.id.nombre_contactos);
        TextView telefono=v.findViewById(R.id.telefono_contactos);
        nombre.setText(listaContactos.get(position).getNombre());
        telefono.setText(listaContactos.get(position).getNumero());
        return v;
    }
}
