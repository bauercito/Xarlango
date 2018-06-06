package com.example.cpv.xarlango.gui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.cpv.chatpruebas.R;

/**
 * Clase que extiende de DialogFragemt. Crea un dialog personalizado que sera llamado a traves de
 * un BroadCastReciver para mostrar al usuario que no tiene conexion a internet. Sera informativo
 * para el usuario y estara obligado para cuntinuar en pulsar en el button afimativo
 */
public class Conexion_dialog extends DialogFragment {
    /**
     * Metodo que sera llamado para crear un dialog personalizado. Inflara un layaout previamente
     * creado y solo dara una opcion para elegir. Esa opcion cerrara la aplicacion
     * @param savedInstanceState bundle con datos primitivos
     * @return devuelve un objeto de tipo dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.sin_conexion_dialog,null);
        //content.findViewById(R.id.);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(content)
                // Add action buttons
                .setPositiveButton("Cerrar ahora"/*Mensaje para el botón positivo*/, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finishAffinity();

                    }
                });

        return builder.create();

    }
}
