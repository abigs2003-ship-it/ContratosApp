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
import com.example.contrato.databinding.FragmentDomiciliootroBinding;

public class DomicilioFragmentOtro extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {
    private FragmentDomiciliootroBinding binding;
    private SharedContractViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliootroBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            binding.editLinea1.setText(contract.getOtroLinea1());
            binding.editLinea2.setText(contract.getOtroLinea2());
            binding.editLinea3.setText(contract.getOtroLinea3());
            binding.editLinea4.setText(contract.getOtroLinea4());
            binding.edit5.setText(contract.getOtroLinea5());
            binding.editPaisOtro.setText(contract.getOtroPais());
        }
    }

    private void setupAutoSave() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveData(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editLinea1.addTextChangedListener(watcher);
        binding.editLinea2.addTextChangedListener(watcher);
        binding.editLinea3.addTextChangedListener(watcher);
        binding.editLinea4.addTextChangedListener(watcher);
        binding.edit5.addTextChangedListener(watcher);
        binding.editPaisOtro.addTextChangedListener(watcher);
    }

    private void saveData() {
        if (binding == null) return;
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setOtroLinea1(binding.editLinea1.getText().toString());
        contract.setOtroLinea2(binding.editLinea2.getText().toString());
        contract.setOtroLinea3(binding.editLinea3.getText().toString());
        contract.setOtroLinea4(binding.editLinea4.getText().toString());
        contract.setOtroLinea5(binding.edit5.getText().toString());
        contract.setOtroPais(binding.editPaisOtro.getText().toString());
        
        // Also keep general 'pais' as "Otro" for navigation logic if needed
        contract.setPais("Otro");
        
        viewModel.setContract(contract);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        return !binding.editLinea1.getText().toString().trim().isEmpty() &&
               !binding.editPaisOtro.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editLinea1.setText("");
            binding.editLinea2.setText("");
            binding.editLinea3.setText("");
            binding.editLinea4.setText("");
            binding.edit5.setText("");
            binding.editPaisOtro.setText("");
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
