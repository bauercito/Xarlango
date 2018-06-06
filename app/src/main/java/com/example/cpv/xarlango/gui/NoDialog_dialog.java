package com.example.cpv.xarlango.gui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.cpv.chatpruebas.R;

/**
 * Clase que hereda de DialogFragment. Crea un dialog para msotrar al usuario y mostrarle una
 * informacion especifica. El dialog solo tiene una opcion para elegir y cierra la aplicacion
 */
public class NoDialog_dialog extends DialogFragment {
    /**
     * Metodo que es llamado cuando no detecta la aplicacion ningun numero de telefono en una
     * tarjeta SIM. Solo permite un boton positivo y cierra la aplicacion
     * @param savedInstanceState bundle con datos primitivos
     * @return objeto de tipo dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.no_numero_dialog,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(content)
                // Add action buttons
                .setPositiveButton("Entiendo"/*Mensaje para el botón positivo*/, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finishAffinity();

                    }
                });

        return builder.create();

    }
}
