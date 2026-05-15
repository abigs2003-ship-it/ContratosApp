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
import com.example.contrato.databinding.FragmentDomiciliootroBinding;

public class DomicilioFragmentOtro extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {
    private FragmentDomiciliootroBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliootroBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        loadExistingData();
        setupAutoSave();

        return binding.getRoot();
    }

    private void loadExistingData() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editLinea1.setText(Contrato.getOtroLinea1());
            binding.editLinea2.setText(Contrato.getOtroLinea2());
            binding.editLinea3.setText(Contrato.getOtroLinea3());
            binding.editLinea4.setText(Contrato.getOtroLinea4());
            binding.edit5.setText(Contrato.getOtroLinea5());
            binding.editPaisOtro.setText(Contrato.getOtroPais());
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
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setOtroLinea1(binding.editLinea1.getText().toString());
        Contrato.setOtroLinea2(binding.editLinea2.getText().toString());
        Contrato.setOtroLinea3(binding.editLinea3.getText().toString());
        Contrato.setOtroLinea4(binding.editLinea4.getText().toString());
        Contrato.setOtroLinea5(binding.edit5.getText().toString());
        Contrato.setOtroPais(binding.editPaisOtro.getText().toString());
        
        // Also keep general 'pais' as "Otro" for navigation logic if needed
        Contrato.setPais("Otro");
        
        viewModel.setContrato(Contrato);
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
