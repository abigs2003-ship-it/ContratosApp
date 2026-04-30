package com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato.CMRFormatFragment;
import com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato.POFormatFragment;
import com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato.StandardFormatFragment;
import com.example.contrato.R;
import com.example.contrato.databinding.FragmentDomiciliousaBinding;

public class DomicilioFragmentUSA extends Fragment {
    private FragmentDomiciliousaBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliousaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rgFormat.setOnCheckedChangeListener((group, id) -> {
            if (id == R.id.rbStandard) {
                cargaFragmentoFormato(new StandardFormatFragment());
            } else if(id == R.id.rbPObox){
                cargaFragmentoFormato(new POFormatFragment());
            } else if(id == R.id.rbCMR){
                cargaFragmentoFormato(new CMRFormatFragment());
            }else{
                quitaFragmentoFormato();
            }
        });

        binding.rbStandard.setChecked(true);
    }

    private void cargaFragmentoFormato(Fragment fragmento) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_formato_placeholder, fragmento)
                .commit();
    }

    private void quitaFragmentoFormato() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_formato_placeholder);
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
