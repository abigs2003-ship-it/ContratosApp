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
import com.example.contrato.databinding.FragmentDomiciliomexicoBinding;

public class DomicilioFragmentMexico extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {
    private FragmentDomiciliomexicoBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliomexicoBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);
        
        loadExistingData();
        setupAutoSave();
        
        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editCalle.setText(contract.getMexCalle());
            binding.editNext.setText(contract.getMexNumExt());
            binding.cp.setText(contract.getMexCP());
            binding.editColonia.setText(contract.getMexColonia());
            binding.editMunicipio.setText(contract.getMexMunicipio());
            binding.editCiudad.setText(contract.getMexCiudad());
            binding.editEstado.setText(contract.getMexEstado());
        }
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCalle.addTextChangedListener(watcher);
        binding.editNext.addTextChangedListener(watcher);
        binding.cp.addTextChangedListener(watcher);
        binding.editColonia.addTextChangedListener(watcher);
        binding.editMunicipio.addTextChangedListener(watcher);
        binding.editCiudad.addTextChangedListener(watcher);
        binding.editEstado.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setMexCalle(binding.editCalle.getText().toString());
        contract.setMexNumExt(binding.editNext.getText().toString());
        contract.setMexCP(binding.cp.getText().toString());
        contract.setMexColonia(binding.editColonia.getText().toString());
        contract.setMexMunicipio(binding.editMunicipio.getText().toString());
        contract.setMexCiudad(binding.editCiudad.getText().toString());
        contract.setMexEstado(binding.editEstado.getText().toString());
        

        contract.setPais("México");
        contract.setCalle(contract.getMexCalle());
        contract.setColonia(contract.getMexColonia());
        contract.setCp(contract.getMexCP());
        contract.setCiudad(contract.getMexCiudad());
        contract.setEstado(contract.getMexEstado());
        
        viewModel.setContract(contract);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return !binding.editCalle.getText().toString().trim().isEmpty() &&
               !binding.editNext.getText().toString().trim().isEmpty() &&
               !binding.cp.getText().toString().trim().isEmpty() &&
               !binding.editColonia.getText().toString().trim().isEmpty() &&
               !binding.editMunicipio.getText().toString().trim().isEmpty() &&
               !binding.editCiudad.getText().toString().trim().isEmpty() &&
               !binding.editEstado.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editCalle.setText("");
            binding.editNext.setText("");
            binding.cp.setText("");
            binding.editColonia.setText("");
            binding.editMunicipio.setText("");
            binding.editCiudad.setText("");
            binding.editEstado.setText("");
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
