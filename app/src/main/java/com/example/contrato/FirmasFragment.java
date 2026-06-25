package com.example.contrato;
import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.contrato.databinding.FragmentFirmaBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import se.warting.signatureview.views.SignaturePad;
public class FirmasFragment extends Fragment {

    private FragmentFirmaBinding binding;

    private SignaturePad signaturePad;
    private SharedContratoViewModel viewModel;

    private ContratoModelo.Persona persona;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirmaBinding.inflate(
                inflater,
                container,
                false
        );
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

        signaturePad = binding.signaturePad;

        binding.btnLimpiar.setOnClickListener(v -> {
            signaturePad.clear();
        });
        binding.btnGuardarFirma.setOnClickListener(v -> {
            Bitmap bitmap = signaturePad.getSignatureBitmap();

            if (bitmap == null) {
                Toast.makeText(requireContext(), "Firma vacía", Toast.LENGTH_SHORT).show();return;}
            guardarFirma(bitmap);
        });

        if(getArguments()!=null){
            persona = (ContratoModelo.Persona) getArguments().getSerializable("persona");
        }else{
            persona = viewModel.getPersonaParaFirma();
        }
        if(persona != null) {
            String nombre = persona.nombre + " " + persona.paterno + " " + persona.materno;

            binding.nombreTitular.setText(nombre);
        }

        binding.btnRegresaTitulares.setOnClickListener(v -> {navegaRegreso();});
    }

    private void navegaRegreso(){
        Navigation.findNavController(this.requireView())
                .navigate(R.id.action_firmas_a_titulares);
    }
    private void guardarFirma(Bitmap bitmap){

        if(persona == null)
            return;

        ByteArrayOutputStream stream =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                stream
        );
        byte[] bytes =
                stream.toByteArray();

        persona.imagenFirmaBase64 =
                Base64.encodeToString(
                        bytes,
                        Base64.NO_WRAP
                );

        // guardar en contrato del ViewModel
        ContratoModelo contrato =
                viewModel.getContratoValue();
        if(contrato != null){

            for(ContratoModelo.Persona p :
                    contrato.getTitulares()){

                if(p.id.equals(persona.id)){

                    p.imagenFirmaBase64 =
                            persona.imagenFirmaBase64;

                    break;
                }
            }

            for(ContratoModelo.Persona p :
                    contrato.getBeneficiarios()){

                if(p.id.equals(persona.id)){

                    p.imagenFirmaBase64 =
                            persona.imagenFirmaBase64;

                    break;
                }
            }


            viewModel.setContrato(contrato);
        }



        Toast.makeText(
                requireContext(),
                "Firma guardada",
                Toast.LENGTH_SHORT
        ).show();



        NavHostFragment
                .findNavController(this)
                .popBackStack();
    }


    @Override
    public void onDestroyView() {

        super.onDestroyView();


        binding = null;

        signaturePad = null;

    }

}