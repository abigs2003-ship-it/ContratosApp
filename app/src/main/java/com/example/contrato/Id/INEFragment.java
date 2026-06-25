package com.example.contrato.Id;

import static java.lang.System.out;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contrato.ContratoModelo;
import com.example.contrato.R;
import com.example.contrato.SharedContratoViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;


public class INEFragment extends Fragment {


    private PreviewView ine1;
    private PreviewView ine2;
    private MaterialButton btnGuardarFoto1;
    private MaterialButton btnGuardarFoto2;


    private IDOverlayView overlay1;
    private IDOverlayView overlay2;

    private ImageView imgINE1;
    private ImageView imgINE2;

    private MaterialButton btnINE1;
    private MaterialButton btnINE2;
    private ImageButton regresa;

    private SharedContratoViewModel viewModel;
    private ContratoModelo.Persona persona;

    private ImageCapture imageCapture;

    private ProcessCameraProvider cameraProvider;


    private boolean frenteCapturada = false;
    private TextView titulo;
    private boolean reversoCapturado = false;


    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        View view = inflater.inflate(
                R.layout.fragment_id,
                container,
                false
        );
        titulo = view.findViewById(R.id.TVtitulo);

        if(getArguments()!=null){
            persona = (ContratoModelo.Persona) getArguments().getSerializable("persona");
        }else{
            persona = viewModel.getPersonaParaINE();
        }
        if(persona != null) {
            String nombre = persona.nombre + " " + persona.paterno + " " + persona.materno;
            titulo.setText(esIngles() ? "Scan your ID : " + nombre : "Escanea tu INE : " + nombre);
        }

        ine1 = view.findViewById(R.id.INE1);
        ine2 = view.findViewById(R.id.INE2);


        overlay1 = view.findViewById(R.id.overlay1);
        overlay2 = view.findViewById(R.id.overlay2);

        imgINE1 = view.findViewById(R.id.imgINE1);
        imgINE2 = view.findViewById(R.id.imgINE2);
        regresa = view.findViewById(R.id.regresa);


        btnINE1 = view.findViewById(R.id.btnINE1);

        btnINE2 = view.findViewById(R.id.btnINE2);

        ine2.setVisibility(View.GONE);
        overlay2.setVisibility(View.GONE);

        btnINE2.setEnabled(false);
        btnINE2.setAlpha(0.5f);

        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    10
            );

        }else{

            iniciarCamaraFrente();

        }

        btnINE1.setOnClickListener(v -> {

            btnINE1.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_otravez));
            btnINE1.setBackgroundColor(getResources().getColor(R.color.botonFirma));
            if(frenteCapturada){

                frenteCapturada = false;
                imgINE1.setVisibility(View.GONE);

                ine1.setVisibility(View.VISIBLE);
                overlay1.setVisibility(View.VISIBLE);

                iniciarCamaraFrente();

            }else{
                tomarFoto(
                        true,
                        "INE_FRENTE.jpg"
                );

            }

        });

        btnINE2.setOnClickListener(v -> {
            btnINE2.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_otravez));
            btnINE2.setBackgroundColor(getResources().getColor(R.color.botonFirma));

            if(reversoCapturado){
                reversoCapturado = false;
                imgINE2.setVisibility(View.GONE);

                ine2.setVisibility(View.VISIBLE);
                overlay2.setVisibility(View.VISIBLE);

                iniciarCamaraReverso();

            }else{

                tomarFoto(false, "INE_REVERSO.jpg");

            }


        });
        regresa.setOnClickListener(v -> {navegaRegreso();});


        return view;

    }


    private void iniciarCamaraFrente(){


        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());


        future.addListener(() -> {


            try{
                cameraProvider = future.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(ine1.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );


            }catch(Exception e){
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }
    private void navegaRegreso(){
        Navigation.findNavController(this.requireView())
                .navigate(R.id.action_ine_a_titulares);
    }

    private void iniciarCamaraReverso(){


        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {
            try{
                cameraProvider = future.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(ine2.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );


            }catch(Exception e){

                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));

    }

    private void tomarFoto(
            boolean frente,
            String nombre
    ){

        if(imageCapture == null)
            return;

        File file = new File(requireContext().getExternalFilesDir(null), nombre);

        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file)
                .build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback(){

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults output
                    ) {

                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        Bitmap crop;

                        if (frente) {
                            crop = recortar(bitmap, overlay1);

                        } else {
                            crop = recortar(bitmap, overlay2);

                        }
                        guardar(crop, nombre);

                        requireActivity()
                                .runOnUiThread(() -> {

                                    if (frente) {

                                        imgINE1.setImageBitmap(crop);
                                        ine1.setVisibility(View.GONE);
                                        overlay1.setVisibility(View.GONE);

                                        imgINE1.setVisibility(View.VISIBLE);

                                        frenteCapturada = true;

                                        ine2.setVisibility(View.VISIBLE);
                                        overlay2.setVisibility(View.VISIBLE);

                                        btnINE2.setEnabled(true);
                                        btnINE2.setAlpha(1f);

                                        iniciarCamaraReverso();

                                    } else {

                                        imgINE2.setImageBitmap(crop);
                                        ine2.setVisibility(View.GONE);
                                        overlay2.setVisibility(View.GONE);
                                        imgINE2.setVisibility(View.VISIBLE);

                                        reversoCapturado = true;
                                        Toast.makeText(requireContext(), "INE guardada, puede regresar.", Toast.LENGTH_LONG).show();

                                    }

                                });
                    }


                    @Override
                    public void onError(
                            @NonNull ImageCaptureException e
                    ){

                        e.printStackTrace();

                    }

                }
        );


    }

    private Bitmap recortar(
            Bitmap bitmap,
            IDOverlayView overlay
    ){

        RectF rect =
                overlay.getCardRect();

        float sx =
                (float) bitmap.getWidth()
                        /
                        overlay.getWidth();

        float sy =
                (float) bitmap.getHeight()
                        /
                        overlay.getHeight();
        return Bitmap.createBitmap(
                bitmap,
                (int)(rect.left * sx),
                (int)(rect.top * sy),
                (int)(rect.width() * sx),
                (int)(rect.height() * sy)
        );

    }
    private void guardar(Bitmap bitmap, String lado) {
        if (persona == null) return;

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

            boolean esFrente = lado.equals("INE_FRENTE.jpg");

            if (esFrente) {
                persona.imagenINEFrente = base64;
            } else {
                persona.imagenINEReverso = base64;
            }

            ContratoModelo contrato = viewModel.getContratoValue();
            if (contrato == null) return;

            // Update matching person in both lists
            for (ContratoModelo.Persona p : contrato.getTitulares()) {
                if (p.id.equals(persona.id)) {
                    if (esFrente) p.imagenINEFrente = base64;
                    else          p.imagenINEReverso = base64;
                    break;
                }
            }

            for (ContratoModelo.Persona p : contrato.getBeneficiarios()) {
                if (p.id.equals(persona.id)) {
                    if (esFrente) p.imagenINEFrente = base64;
                    else          p.imagenINEReverso = base64;
                    break;
                }
            }

            viewModel.setContrato(contrato);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ){


        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );



        if(requestCode == 10 &&
                grantResults.length > 0 &&
                grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){


            iniciarCamaraFrente();

        }


    }



    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }


    @Override
    public void onDestroyView(){

        if(cameraProvider != null){

            cameraProvider.unbindAll();

        }

        super.onDestroyView();

    }

}