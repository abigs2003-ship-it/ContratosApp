package com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato;

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
import com.example.contrato.databinding.FragmentPoboxFormatBinding;

public class POFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {

    private FragmentPoboxFormatBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPoboxFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editStreetPO.setText(contract.getUsaCalle());
            binding.editPO.setText(contract.getPoBox());
            binding.editCity.setText(contract.getUsaCity());
            binding.editState.setText(contract.getUsaState());
            binding.editZipCode2.setText(contract.getUsaZip());
        }
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editStreetPO.addTextChangedListener(watcher);
        binding.editPO.addTextChangedListener(watcher);
        binding.editCity.addTextChangedListener(watcher);
        binding.editState.addTextChangedListener(watcher);
        binding.editZipCode2.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setUsaCalle(binding.editStreetPO.getText().toString());
        contract.setPoBox(binding.editPO.getText().toString());
        contract.setUsaCity(binding.editCity.getText().toString());
        contract.setUsaState(binding.editState.getText().toString());
        contract.setUsaZip(binding.editZipCode2.getText().toString());
        
        // Update general fields for compatibility
        contract.setPais("EEUU");
        contract.setCalle(contract.getUsaCalle());
        contract.setCiudad(contract.getUsaCity());
        contract.setEstado(contract.getUsaState());
        contract.setCp(contract.getUsaZip());
        
        viewModel.setContract(contract);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return  !binding.editStreetPO.getText().toString().trim().isEmpty() &&
                !binding.editPO.getText().toString().trim().isEmpty() &&
               !binding.editCity.getText().toString().trim().isEmpty() &&
               !binding.editState.getText().toString().trim().isEmpty() &&
               !binding.editZipCode2.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editStreetPO.setText("");
            binding.editPO.setText("");
            binding.editCity.setText("");
            binding.editState.setText("");
            binding.editZipCode2.setText("");
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
