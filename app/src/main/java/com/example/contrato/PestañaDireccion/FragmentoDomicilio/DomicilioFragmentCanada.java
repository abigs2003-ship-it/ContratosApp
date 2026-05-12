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
import com.example.contrato.SharedContractViewModel;
import com.example.contrato.databinding.FragmentDomiciliocanadaBinding;

public class DomicilioFragmentCanada extends Fragment implements PestañaDireccionFragment.ValidatableFragment {
    private FragmentDomiciliocanadaBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliocanadaBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editStreetCanada.setText(contract.getCanCalle());
            binding.editCityCanada.setText(contract.getCanCity());
            binding.editProvinceCanada.setText(contract.getCanProvince());
            binding.editCPCanada.setText(contract.getCanPostalCode());
            binding.editPaisCanada.setText(contract.getPais() != null ? contract.getPais() : "Canadá");
        }
    }

    private void setupAutoSave() {
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
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setCanCalle(binding.editStreetCanada.getText().toString());
        contract.setCanCity(binding.editCityCanada.getText().toString());
        contract.setCanProvince(binding.editProvinceCanada.getText().toString());
        contract.setCanPostalCode(binding.editCPCanada.getText().toString());
        

        contract.setPais("Canadá");
        contract.setCalle(contract.getCanCalle());
        contract.setCiudad(contract.getCanCity());
        contract.setEstado(contract.getCanProvince());
        contract.setCp(contract.getCanPostalCode());
        
        viewModel.setContract(contract);
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
    public void onDestroyView() {
        saveData();
        super.onDestroyView();
        binding = null;
    }
}
