package com.example.cpv.chatpruebas;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorRecycleChat extends RecyclerView.Adapter<AdaptadorRecycleChat.ViewHolder> {
    ArrayList<String> conversacion=new ArrayList();
    ArrayList<String>persona=new ArrayList();
    String telefonoPropio;
    Context actividad;
    String hora;

    public AdaptadorRecycleChat(ArrayList<String> conversacion,ArrayList<String> persona,String telefonoPropio,Context actividad,String hora){
        this.conversacion=conversacion;
        this.persona=persona;
        this.telefonoPropio=telefonoPropio;
        this.actividad=actividad;
        this.hora=hora;
    }

    @NonNull
    @Override
    public AdaptadorRecycleChat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista= LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_conversacion,null);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorRecycleChat.ViewHolder holder, int position) {
        holder.rellenar(persona.get(position).toString(),conversacion.get(position).toString()); //metodo para mostrar los datos del restaurante en Views
    }

    @Override
    public int getItemCount() {
        if(conversacion!=null){
            return conversacion.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView yo=(TextView)itemView.findViewById(R.id.yo);
        TextView tu=(TextView)itemView.findViewById(R.id.tu);
        TextView hora_yo=(TextView)itemView.findViewById(R.id.hora_yo);
        TextView hora_tu=(TextView)itemView.findViewById(R.id.hora_tu);
        public ViewHolder(View itemView) {
            super(itemView);
            //referencio las Views

        }
        public void rellenar(String persona,String conversacion){
            //poner chats
            if(persona.equalsIgnoreCase(telefonoPropio)){
                yo.setText(conversacion.toString());
                hora_yo.setText(hora);
            }else{
                tu.setText(conversacion.toString());
                hora_tu.setText(hora);
            }
        }
    }
}
