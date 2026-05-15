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
import com.example.contrato.SharedContratoViewModel;
import com.example.contrato.databinding.FragmentStandardFormatBinding;

public class StandardFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {

    private FragmentStandardFormatBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStandardFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editCalle.setText(Contrato.getUsaCalle());
            binding.editCity.setText(Contrato.getUsaCity());
            binding.editState.setText(Contrato.getUsaState());
            binding.editZipCode.setText(Contrato.getUsaZip());
            binding.editNeighborhood.setText(Contrato.getUsaNeighborhood());
            binding.editCountryUSA.setText(Contrato.getOtroPais());
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
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setUsaCalle(binding.editCalle.getText().toString());
        Contrato.setUsaCity(binding.editCity.getText().toString());
        Contrato.setUsaState(binding.editState.getText().toString());
        Contrato.setUsaZip(binding.editZipCode.getText().toString());
        Contrato.setUsaNeighborhood(binding.editNeighborhood.getText().toString());
        
        Contrato.setPais("EEUU");
        Contrato.setCalle(Contrato.getUsaCalle());
        Contrato.setCiudad(Contrato.getUsaCity());
        Contrato.setEstado(Contrato.getUsaState());
        Contrato.setCp(Contrato.getUsaZip());
        Contrato.setColonia(Contrato.getUsaNeighborhood());

        viewModel.setContrato(Contrato);
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
