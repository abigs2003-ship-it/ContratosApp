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
import com.example.contrato.databinding.FragmentCmrFormatBinding;

public class CMRFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment {

    private FragmentCmrFormatBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCmrFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editCMR1.setText(contract.getCalle());
            binding.editBox.setText(contract.getPoBox());
            binding.editAPO.setText(contract.getColonia());
            binding.editCity.setText(contract.getCiudad());
            binding.editState.setText(contract.getEstado());
            binding.zipcode.setText(contract.getCp());
        }
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCMR1.addTextChangedListener(watcher);
        binding.editBox.addTextChangedListener(watcher);
        binding.editAPO.addTextChangedListener(watcher);
        binding.editCity.addTextChangedListener(watcher);
        binding.editState.addTextChangedListener(watcher);
        binding.zipcode.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        contract.setCalle(binding.editCMR1.getText().toString());
        contract.setPoBox(binding.editBox.getText().toString());
        contract.setColonia(binding.editAPO.getText().toString());
        contract.setCiudad(binding.editCity.getText().toString());
        contract.setEstado(binding.editState.getText().toString());
        contract.setCp(binding.zipcode.getText().toString());
        viewModel.setContract(contract);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return (!binding.editCMR1.getText().toString().trim().isEmpty() || 
                !binding.editBox.getText().toString().trim().isEmpty() || 
                !binding.editAPO.getText().toString().trim().isEmpty()) &&
               !binding.editCity.getText().toString().trim().isEmpty() &&
               !binding.editState.getText().toString().trim().isEmpty() &&
               !binding.zipcode.getText().toString().trim().isEmpty();
    }

    @Override
    public void onDestroyView() {
        saveData();
        super.onDestroyView();
        binding = null;
    }
}
