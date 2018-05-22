package com.example.cpv.xarlango;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.example.cpv.chatpruebas.R;

public class Conexion_dialog extends DialogFragment {
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
