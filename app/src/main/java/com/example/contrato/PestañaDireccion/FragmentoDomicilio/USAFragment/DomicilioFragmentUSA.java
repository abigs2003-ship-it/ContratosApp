package com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.material.button.MaterialButton;

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

        setupButtons();

        // Configuración inicial: Standard por defecto
        selectButton(binding.btnStandard);
        cargaFragmentoFormato(new StandardFormatFragment());
    }

    private void setupButtons() {
        binding.btnStandard.setOnClickListener(v -> {
            selectButton(binding.btnStandard);
            cargaFragmentoFormato(new StandardFormatFragment());
        });

        binding.btnPObox.setOnClickListener(v -> {
            selectButton(binding.btnPObox);
            cargaFragmentoFormato(new POFormatFragment());
        });

        binding.btnCMR.setOnClickListener(v -> {
            selectButton(binding.btnCMR);
            cargaFragmentoFormato(new CMRFormatFragment());
        });
    }

    private void selectButton(MaterialButton selected) {
        // Resetear estilos de todos los botones
        resetButtonStyle(binding.btnStandard);
        resetButtonStyle(binding.btnPObox);
        resetButtonStyle(binding.btnCMR);

        // Aplicar estilo de seleccionado (Sólido oscuro)
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0A0E21")));
        selected.setTextColor(Color.WHITE);
        selected.setStrokeWidth(0);
    }

    private void resetButtonStyle(MaterialButton button) {
        // Estilo deseleccionado (Outlined)
        button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        button.setTextColor(Color.parseColor("#1E293B"));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        button.setStrokeWidth(1);
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
