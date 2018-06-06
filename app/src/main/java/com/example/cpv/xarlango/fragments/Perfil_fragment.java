package com.example.cpv.xarlango.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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

import com.example.cpv.chatpruebas.R;
import com.example.cpv.xarlango.actividades.Chat;
import com.example.cpv.xarlango.servicios.Service_conexionPermanente;
import com.example.cpv.xarlango.servicios.Servicio_notificaciones;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;


/**
 *Clase que hereda de Fragment. Establece un fragment el cual sera usado sobre una actividad,
 * Land_activity. Mostrara el perfil del usuario con sus datos recogidos del servidor. Tambien
 * data la opcion de modificar estos datos cuando el usuario desee
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

    String nombre; //nombre del usuario
    String telefono; //telefono del usuario
    String descripcion; //estado del usuario


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

    /**
     * Metodo que inicializa el fragment
     * @param savedInstanceState bundel con datos primitivos
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    /**
     * metodo que pasa cuando el fragment esta en primer plano. Establece una variable estatica
     * de apoyo para el servicio de notificaciones
     */
    @Override
    public void onResume() {
        Servicio_notificaciones.ESTADOAPP=false;
        super.onResume();
    }

    /**
     * Metodo que es llamado cuando el fragment entra en segundo plano. Establece una variable
     * estatica de apoyo para el servicio de notificaciones
     */
    @Override
    public void onPause() {
        super.onPause();
        Servicio_notificaciones.ESTADOAPP=true;
    }

    /**
     * Metodo que infla el frgament de un layout proporcionado. Devolvera una vista con el contenido
     * del layout modificado. Se rellanara un snniper cone estados, nombre, telefono y foto del
     * usuario
     * @param inflater inflado del layout
     * @param container vista del contenedor del fragment
     * @param savedInstanceState bundle con datos guardados que rescatara el fragment si fuese necesario
     * @return devuelve vista para ser cargada en la actividad sobre la que se lanza
     */
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
        if(Service_conexionPermanente.isOnline(getContext())){
            foto_perfil(v);
        }

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


    /**
     * metodo sin usar vista
     * @param v
     */
    public void abrir_chat(View v){
        String numero=getTelefonoPropio() ;
        String nombreChat=numero+"_"+telefono;
        Intent i=new Intent(getActivity(),Chat.class);
        i.putExtra("nombreChat",nombreChat);
        i.putExtra("numero_usuario",numero);
        i.putExtra("numero_destino",telefono);
        startActivity(i);
    }

    /**
     * metodo sin usar
     * @param telefono_perfil telefono
     * @param v vista
     */
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

    /**
     * metodo que recogel el numero de telefono del usuario local
     * @return devuelve numero de telefono local
     */
    public String getTelefonoPropio(){
        TelephonyManager tMgr=(TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    /**
     * Metodo que es llamado cuando el usuario pulsa sobre la vista de aceptar confirmacion en
     * el layout. Muestra un dialog personalizado pidiendo la confirmacion de los cambios.
     * Si el usuario acepta los cambios, seran grabados en el servidor
     * @param nombre nombre del usuario
     * @param estado estado del usuario
     */
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

    /**
     * metodo sin usar
     * @param v vista
     */
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
                estado_perfil.setSelection(Integer.parseInt(estado));

                //estado_perfil.setSelection(Integer.parseInt(descripcion));
                //estado_perfil.setText(descripcion);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Metodo que pone a la escucha un imageview para llamar a createDialogConfirm si el usuario lo
     * pulsa. Tiene que pasar antes por una validacion antes de llamar al metodo mencionado
     * @param vista_fragment vista del fragmento lanzado
     */
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


    /**
     * Metodo que grabara en el servidor la imagen establecido por el usuario
     */
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
                ImageView foto_perfil=getView().findViewById(R.id.imageView);
                foto_perfil.setImageResource(R.drawable.aspa);
                Toast.makeText(getActivity(), "Fallo en la subida de la foto, intentelo de nuevo", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }

    /**
     * Metodo que establecera el perfil del usuario recuperada desde el servidor. Tambien pondra a
     * la escucha dos ImageView, tanto para tener acceso a la camara de fotos como para tener
     * acceso a la galeria de fotos del usuario local
     * @param v vista de la actividad
     */
    public void foto_perfil(View v){
        //RECUPERO LA FOTO DE FIREBASE
        StorageReference storageRef =FirebaseStorage.getInstance().getReference();
        StorageReference Ref = storageRef.child(telefono+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        final ImageView image = (ImageView) v.findViewById(R.id.imageView);
        Ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                image.setImageDrawable(redondear_imagen(bmp));
                //image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),image.getHeight(), false));
                image.getRootView().findViewById(R.id.carga_perfil_fragment_layout).setVisibility(View.INVISIBLE);
                image.getRootView().findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Bitmap bmp = ((BitmapDrawable)image.getDrawable()).getBitmap();
                image.setImageDrawable(redondear_imagen(bmp));
                //image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),image.getHeight(), false));
                image.getRootView().findViewById(R.id.carga_perfil_fragment_layout).setVisibility(View.INVISIBLE);
                image.getRootView().findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            }
        });

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

    /**
     * Comprube si la aplicacion tiene permisos de acceso a la camara de fotos del movil.
     * Si os tiene llama al metodo hacerFoto(), si no los solicita al usuario
     */
    public void permisosCamara(){
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},0);
        }else{
            hacerFoto();
        }
    }

    /**
     * Pide al usuario permisos para acceder a la camara de fotos del dispositivos. Si los da llama
     * al metodo hacerFoto(), si no se le comunica al usuario que sin permisos no hay foto de
     * perfil
     * @param requestCode numero de solicitud
     * @param permissions tipo de permiso a proporcionar
     * @param grantResults numero de resultado
     */
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

    /**
     * Llama a la actividad que lanza la camara de fotos
     */
    public void hacerFoto(){
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(i.resolveActivity(getActivity().getPackageManager())!=null){
            startActivityForResult(i,0);
        }
    }

    /**
     * Metodo que devuelve la  foto de la camara de fotos o de la galeria de imagenes del
     * dispositivo. Una vez obtenida la imagen se establece en un ImageView
     * @param requestCode codigo de peticion
     * @param resultCode resultado de la peticion
     * @param data intentn del que fue llamado
     */
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

    /**
     * Metodo que redondea la imagen pasada por parametros
     * @param image imagen en formato bitmap
     * @return devuelve la imagen redondeada
     */
    public RoundedBitmapDrawable redondear_imagen(Bitmap image){
        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), image);

        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(image.getHeight());

        return roundedDrawable;
    }

}
