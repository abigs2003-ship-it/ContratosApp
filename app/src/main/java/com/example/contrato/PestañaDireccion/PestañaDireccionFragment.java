package com.example.contrato.PestañaDireccion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentCanada;
import com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentMexico;
import com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentOtro;
import com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.DomicilioFragmentUSA;
import com.example.contrato.R;
import com.example.contrato.databinding.FragmentDireccionBinding;

public class PestañaDireccionFragment extends Fragment {

    private FragmentDireccionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDireccionBinding.inflate(inflater, container, false);

        binding.rgPais.setOnCheckedChangeListener((group, id) -> {
            if (id == R.id.rbMexico) {
                cargaFragmentoDireccion(new DomicilioFragmentMexico());
            } else if (id == R.id.rbUSA) {
                cargaFragmentoDireccion(new DomicilioFragmentUSA());
            }else if(id == R.id.rbCanada){
                cargaFragmentoDireccion(new DomicilioFragmentCanada());
            }else{
                cargaFragmentoDireccion(new DomicilioFragmentOtro());
            }
        });

        binding.rbMexico.setChecked(true);

        return binding.getRoot();
    }

    private void cargaFragmentoDireccion(Fragment fragmento) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_domicilioformato, fragmento)
                .commit();
    }

    private void quitaFragmentoDireccion() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_domicilioformato);
        if (fragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}