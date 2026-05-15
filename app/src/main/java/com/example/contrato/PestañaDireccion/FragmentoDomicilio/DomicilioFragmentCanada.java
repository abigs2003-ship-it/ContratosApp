package com.example.contrato.PestañaDireccion.FragmentoDomicilio;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.contrato.ContratoModelo;
import com.example.contrato.PestañaDireccion.PestañaDireccionFragment;
import com.example.contrato.SharedContratoViewModel;
import com.example.contrato.databinding.FragmentDomiciliocanadaBinding;

public class DomicilioFragmentCanada extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {
    private FragmentDomiciliocanadaBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliocanadaBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        cargaDatosExistentes();
        setupAutoGuardado();

        return binding.getRoot();
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editStreetCanada.setText(Contrato.getCanCalle());
            binding.editCityCanada.setText(Contrato.getCanCity());
            binding.editProvinceCanada.setText(Contrato.getCanProvince());
            binding.editCPCanada.setText(Contrato.getCanPostalCode());
            binding.editPaisCanada.setText(Contrato.getPais() != null ? Contrato.getPais() : "Canadá");

        }
    }

    private void setupAutoGuardado() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editStreetCanada.addTextChangedListener(watcher);
        binding.editCityCanada.addTextChangedListener(watcher);
        binding.editProvinceCanada.addTextChangedListener(watcher);
        binding.editCPCanada.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setCanCalle(binding.editStreetCanada.getText().toString());
        Contrato.setCanCity(binding.editCityCanada.getText().toString());
        Contrato.setCanProvince(binding.editProvinceCanada.getText().toString());
        Contrato.setCanPostalCode(binding.editCPCanada.getText().toString());
        

        Contrato.setPais("Canadá");
        Contrato.setCalle(Contrato.getCanCalle());
        Contrato.setCiudad(Contrato.getCanCity());
        Contrato.setEstado(Contrato.getCanProvince());
        Contrato.setCp(Contrato.getCanPostalCode());
        
        viewModel.setContrato(Contrato);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return !binding.editStreetCanada.getText().toString().trim().isEmpty() &&
               !binding.editCityCanada.getText().toString().trim().isEmpty() &&
               !binding.editProvinceCanada.getText().toString().trim().isEmpty() &&
               !binding.editCPCanada.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editStreetCanada.setText("");
            binding.editCityCanada.setText("");
            binding.editProvinceCanada.setText("");
            binding.editCPCanada.setText("");
            saveData();
        }
    }

    @Override
    public void onDestroyView() {
        saveData();
        super.onDestroyView();
        binding = null;
    }
}
