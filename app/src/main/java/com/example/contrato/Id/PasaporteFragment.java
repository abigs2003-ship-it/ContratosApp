package com.example.contrato.Id;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import androidx.navigation.Navigation;

import com.example.contrato.R;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;


public class PasaporteFragment extends Fragment {


    private PreviewView pasaporte;

    private PasaporteOverlayView overlay;
    private ImageView imgPasaporte;

    private MaterialButton btnPasaporte;

    private ImageCapture imageCapture;

    private ProcessCameraProvider cameraProvider;




    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {


        View view = inflater.inflate(
                R.layout.fragment_pasaporte,
                container,
                false
        );

        pasaporte = view.findViewById(R.id.PVpasaporte);

        overlay = view.findViewById(R.id.overlay3);

        imgPasaporte = view.findViewById(R.id.imgPasaporte);
        btnPasaporte = view.findViewById(R.id.btnPasaporte);


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
        view.findViewById(R.id.btnRegresaTitulares2).setOnClickListener(v -> {navegaRegreso();});


        btnPasaporte.setOnClickListener(v -> {

            btnPasaporte.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_otravez));
            btnPasaporte.setBackgroundColor(getResources().getColor(R.color.botonFirma));
                tomarFoto(
                        true,
                        "PASAPORTE.jpg"
                );



        });


        return view;
    }


private void navegaRegreso(){
    Navigation.findNavController(this.requireView())
            .navigate(R.id.action_pasaporte_a_titulares);
}

    private void iniciarCamaraFrente(){


        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());


        future.addListener(() -> {


            try{
                cameraProvider = future.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(pasaporte.getSurfaceProvider());

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

                        crop = recortar(bitmap, overlay);

                        guardar(crop, nombre);

                        requireActivity()
                                .runOnUiThread(() -> {

                                        imgPasaporte.setImageBitmap(crop);
                                        pasaporte.setVisibility(View.GONE);
                                        overlay.setVisibility(View.GONE);

                                        imgPasaporte.setVisibility(View.VISIBLE);

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
            PasaporteOverlayView overlay
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
    private void guardar(
            Bitmap bitmap,
            String nombre
    ){


        try{
            File file = new File(requireContext().getExternalFilesDir(null), nombre);

            FileOutputStream out = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            out.flush();
            out.close();

            Toast.makeText(requireContext(), "Guardado " + nombre, Toast.LENGTH_SHORT).show();



        }catch(Exception e){
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






    @Override
    public void onDestroyView(){

        if(cameraProvider != null){

            cameraProvider.unbindAll();

        }

        super.onDestroyView();

    }

}