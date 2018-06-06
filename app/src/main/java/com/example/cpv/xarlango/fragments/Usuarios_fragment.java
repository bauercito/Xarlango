package com.example.cpv.xarlango.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.actividades.Chat;
import com.example.cpv.xarlango.adaptadores.AdaptadorListaChat;
import com.example.cpv.xarlango.servicios.Servicio_notificaciones;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 *Clase que mostrara el listado de conversaciones disponibles en el servidor del usuario. Se mostrara
 * en forma de lista.
 */
public class Usuarios_fragment extends Fragment implements Sin_chats.OnFragmentInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    String nombre;
    String numero;

    public Usuarios_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Usuarios_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Usuarios_fragment newInstance(String param1, String param2) {
        Usuarios_fragment fragment = new Usuarios_fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    /**
     * Metodo que es llamado al estar el fragment en primer plano. Establece una variable estatica
     * de apoyo al servicio de notificaciones
     */
    @Override
    public void onResume() {
        Servicio_notificaciones.ESTADOAPP=false;
        super.onResume();
    }

    /**
     * Metodo que es llamado al estar el fragment en segundo plano. Establece una variable estatica
     * de apoyo al servicio de notificaciones
     */
    @Override
    public void onPause() {
        super.onPause();
        Servicio_notificaciones.ESTADOAPP=true;
    }

    /**
     * Metodo que inflara el fragment a partir de un layout. Llamara al metodo actualizarChat y
     * recogera del bandle pasado por parametros el numero u nombre del usuario
     * @param inflater inflado de layout
     * @param container vista del cotenedor
     * @param savedInstanceState bundle que contiene datos primitivos
     * @return devuelve la vista del fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.usuarios_layaout, container, false);
        Bundle extras=this.getArguments();
        numero=extras.getString("numero");
        nombre=extras.getString("nombre");
        actualizarChat(v);
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Metodo que recoge del servidor los chats que el usuario tiene abiertos. Son guardados en un
     * ArrayList y enviados al metodo que es llamado restablecerLsitView el cual sera llamado cada
     * vez que exista un cambio de datos en el servidor con la direcion proporcionada en la
     * referencia de firebase
     * @param v vista
     */
    public void actualizarChat(final View v){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+numero+"/"+"chats");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> chats=new ArrayList();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    chats.add(child.getKey().toString());
                }
                restablecerListView(chats,v);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("no", "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Metodo que llamara al adaptador para establecera la vista cada vez que en el servidor se
     * detecten cambios. Mostrara en forma de lista todos los chats abiertos por el usuario mostrando
     * el nombre de la persona con la que conversa, la fecha de la ultima creacion y el ultimo
     * comentario realizado en la conversacion. Tambien llamara a un fragment: Sin_chats si el
     * arrayList que contiene las conversaciones fuese 0. Pone a la escucha cada apartado de la
     * lista para llevar a una actividad que sera la conversacion establecida por los usuarios
     * @param chats arraylist que contiene las conversaciones abiertas por el usuario
     * @param v vista de la actividad que da soporte al fragment
     */
    public void restablecerListView(final ArrayList<String> chats,View v){
        ListView lista = (ListView)v.findViewById(R.id.listview);
        lista.setDivider(null);
        if(chats.size()==0){
            Sin_chats sinChatsFragmento=new Sin_chats();
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout3, sinChatsFragmento).addToBackStack(null); //donde fragmentContainer_id es el ID del FrameLayout donde tu Fragment está contenido.
            fragmentTransaction.commit();
        }else{
            AdaptadorListaChat adaptador = new AdaptadorListaChat(getActivity(),chats,numero);
            lista.setAdapter(adaptador);
            lista.setSelection(chats.size() - 1);
        }
        RelativeLayout tl=(RelativeLayout)v.findViewById(R.id.carga_chats_layout);
        tl.setVisibility(View.INVISIBLE);



        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nombreChat=chats.get(position).toString();
                Intent i=new Intent(getContext(),Chat.class);
                i.putExtra("nombreChat",nombreChat);
                i.putExtra("numero_usuario",numero);
                String nombreDestino[]=nombreChat.split("_");
                i.putExtra("numero_destino",nombreDestino[1]);
                startActivity(i);
            }
        });
    }
}
