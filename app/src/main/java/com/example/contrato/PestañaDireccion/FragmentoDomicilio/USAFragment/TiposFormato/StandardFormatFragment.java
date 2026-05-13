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
import com.example.contrato.databinding.FragmentStandardFormatBinding;

public class StandardFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {

    private FragmentStandardFormatBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStandardFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editCalle.setText(contract.getUsaCalle());
            binding.editCity.setText(contract.getUsaCity());
            binding.editState.setText(contract.getUsaState());
            binding.editZipCode.setText(contract.getUsaZip());
            binding.editNeighborhood.setText(contract.getUsaNeighborhood());
            binding.editCountryUSA.setText(contract.getOtroPais());
        }
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCalle.addTextChangedListener(watcher);
        binding.editCity.addTextChangedListener(watcher);
        binding.editState.addTextChangedListener(watcher);
        binding.editZipCode.addTextChangedListener(watcher);
        binding.editNeighborhood.addTextChangedListener(watcher);
        binding.editCountryUSA.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setUsaCalle(binding.editCalle.getText().toString());
        contract.setUsaCity(binding.editCity.getText().toString());
        contract.setUsaState(binding.editState.getText().toString());
        contract.setUsaZip(binding.editZipCode.getText().toString());
        contract.setUsaNeighborhood(binding.editNeighborhood.getText().toString());
        
        contract.setPais("EEUU");
        contract.setCalle(contract.getUsaCalle());
        contract.setCiudad(contract.getUsaCity());
        contract.setEstado(contract.getUsaState());
        contract.setCp(contract.getUsaZip());
        contract.setColonia(contract.getUsaNeighborhood());

        viewModel.setContract(contract);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return !binding.editCalle.getText().toString().trim().isEmpty() &&
               !binding.editCity.getText().toString().trim().isEmpty() &&
               !binding.editState.getText().toString().trim().isEmpty() &&
               !binding.editZipCode.getText().toString().trim().isEmpty() &&
               !binding.editNeighborhood.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editCalle.setText("");
            binding.editCity.setText("");
            binding.editState.setText("");
            binding.editZipCode.setText("");
            binding.editNeighborhood.setText("");
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
