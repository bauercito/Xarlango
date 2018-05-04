package com.example.cpv.chatpruebas;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Perfil_fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Perfil_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Perfil_fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    String nombre;
    String telefono;
    String descripcion;
    static boolean pregunta=false;

    public Perfil_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Perfil_fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Perfil_fragment newInstance(String param1, String param2) {
        Perfil_fragment fragment = new Perfil_fragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v=inflater.inflate(R.layout.perfil_fragment, container, false);
        Bundle extras=this.getArguments();
        nombre=extras.getString("nombre");
        telefono=extras.getString("numero");
        descripcion=extras.getString("estado");

        Spinner spinner_estados = (Spinner) v.findViewById(R.id.descripcion_contacto);
        ArrayAdapter spinner_adapter = ArrayAdapter.createFromResource( getContext(), R.array.estados , android.R.layout.simple_spinner_item);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_estados.setAdapter(spinner_adapter);

        TextView telefono_perfil=v.findViewById(R.id.telefono_perfil);


        telefono_perfil.setText(telefono);


        getNombre_estado(v);
        modificar(v);
        foto_perfil(v);
        comprobarExisteChat(telefono,v);
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


    //ABRO NUEVO CHAT CON EL BUTTON
    public void abrir_chat(View v){
        String numero=getTelefonoPropio() ;
        String nombreChat=numero+"_"+telefono;
        Intent i=new Intent(getActivity(),Chat.class);
        i.putExtra("nombreChat",nombreChat);
        i.putExtra("numero_usuario",numero);
        i.putExtra("numero_destino",telefono);
        startActivity(i);
    }

    //REVISO SI EXISTE ALGUN CHAT, SI LO HAY NO DEJO QUE ABRA CHAT
    public void comprobarExisteChat(final String telefono_perfil, final View v){
        final String telefonoPropio=getTelefonoPropio();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if(child.getKey().equalsIgnoreCase(telefono_perfil+"_"+telefonoPropio)||child.getKey().equalsIgnoreCase(telefonoPropio+"_"+telefono_perfil)){
                        v.findViewById(R.id.abrirChat).setVisibility(View.INVISIBLE);
                        v.findViewById(R.id.chatAbierto).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //CONSIGO TELEFONO PROPIO
    public String getTelefonoPropio(){
        TelephonyManager tMgr=(TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    //EVENTOS ONCLICK
    /*public void modificar_nombre(View v){
        ImageView modifyNombre=(ImageView)v.findViewById(R.id.modificar_nombre);
        modifyNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog("Nombre",v);

            }
        });

    }*/
    /*public void modificar_estado(){

    }*/
    //MOSTRAR DIALOG DE CONFIRMACION
    public void createDialogConfirm(final String nombre, final int estado){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Modificar perfil")
                .setMessage("¿Esta seguro que desea continuar con los cambios?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //cambiar nombre y estado
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("users/"+telefono+"/nombre");
                        myRef.setValue(nombre);
                        DatabaseReference myRef2 = database.getReference("users/"+telefono+"/estado");
                        myRef2.setValue(estado);
                        Toast.makeText(getContext(), "Datos cambiados correctamente", Toast.LENGTH_SHORT).show();
                        //cambiar foto perfil
                        guardarImagenFireBase();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //GET NOMBRE Y ESTADO tambien lo pongo en sus correspondientes editView
    public void getNombre_estado(final View v){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+telefono);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String nombre=dataSnapshot.child("nombre").getValue().toString();
               String estado=dataSnapshot.child("estado").getValue().toString();

                EditText nombre_perfil=v.findViewById(R.id.nombre_perfil);
                Spinner estado_perfil=v.findViewById(R.id.descripcion_contacto);

                nombre_perfil.setText(nombre);
                estado_perfil.setSelection(Integer.parseInt(descripcion));
                //estado_perfil.setText(descripcion);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //BOTON DE MODIFICAR
    public void modificar(final View vista_fragment){
        vista_fragment.findViewById(R.id.modificar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nombre_editText=vista_fragment.findViewById(R.id.nombre_perfil);
                Spinner estado_spinner=vista_fragment.findViewById(R.id.descripcion_contacto);
                int estado=estado_spinner.getSelectedItemPosition();
                String nombre=nombre_editText.getText().toString();
                boolean valido=true;
                if(nombre.equalsIgnoreCase("")){
                    nombre_editText.setText("");
                    nombre_editText.setHint("Campo vacio");
                    valido=false;
                }else if(nombre.length()<3|| nombre.length()>21){
                    nombre_editText.setText("");
                    nombre_editText.setHint("Introduce entre 3 y 20 caracteres");
                    valido=false;
                }


                if(valido){
                    createDialogConfirm(nombre,estado);
                }
            }
        });

    }


    //PARA PONER FOTO AL PERFIL
    //OBTENER ACCESO A LA CAMARA Y POSTERIORES
    public void guardarImagenFireBase(){
        StorageReference storageRef =FirebaseStorage.getInstance().getReference();
        StorageReference mountainsRef = storageRef.child(telefono+".jpg");
        ImageView foto_perfil=getView().findViewById(R.id.imageView);
        //Bitmap bitmap = ((BitmapDrawable)foto_perfil.getDrawable()).getBitmap();
        foto_perfil.setDrawingCacheEnabled(true);
        foto_perfil.buildDrawingCache();
        Bitmap bitmap = foto_perfil.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }
    public void foto_perfil(View v){
        //pongo a la escucha imagen para hacer fotos
        ImageView image_camera=(ImageView)v.findViewById(R.id.camera);
        image_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permisosCamara();
            }
        });

        //pongo a la escucha imagen para la galeria
        ImageView image_gallery=(ImageView)v.findViewById(R.id.gallery);
        image_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1);
            }
        });


    }
    public void permisosCamara(){
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},0);
        }else{
            hacerFoto();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(0){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    hacerFoto();
                }else{
                    Toast.makeText(getActivity(), "Si no hay permisos no hay foto de perfil", Toast.LENGTH_SHORT).show();
                }
        }
    }
    public void hacerFoto(){
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(i.resolveActivity(getActivity().getPackageManager())!=null){
            startActivityForResult(i,0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0&&resultCode==RESULT_OK){
            Bundle paquete=data.getExtras();
            Bitmap img=(Bitmap)paquete.get("data");
            ImageView iw=(ImageView)getView().findViewById(R.id.imageView);
            iw.setImageBitmap(img);
        }
        if(requestCode==1&&resultCode==RESULT_OK){
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap img = BitmapFactory.decodeStream(imageStream);
                ImageView iw=(ImageView)getView().findViewById(R.id.imageView);
                iw.setImageBitmap(img);
            } catch (FileNotFoundException e) {
                //aki deberia mostrar un alert de error
                e.printStackTrace();
            }

        }
    }
}
