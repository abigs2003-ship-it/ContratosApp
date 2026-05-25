package com.example.contrato.PestañaDireccion.FragmentoDomicilio;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.contrato.ContratoModelo;
import com.example.contrato.PestañaDireccion.PestañaDireccionFragment;
import com.example.contrato.R;
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

        setupProvinciasSpinner();
        cargaDatosExistentes();
        setupAutoGuardado();

        return binding.getRoot();
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editStreetCanada.setText(Contrato.getCanCalle());
            binding.editCityCanada.setText(Contrato.getCanCity());
            binding.editCPCanada.setText(Contrato.getCanPostalCode());
            if (Contrato.getCanProvince() != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerProvincias.getAdapter();
                if (adapter != null) {
                    int pos = adapter.getPosition(Contrato.getCanProvince());
                    if (pos >= 0) binding.spinnerProvincias.setSelection(pos);
                }
            }

        }
    }

    private void setupAutoGuardado() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { guardaDatosViewModel(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editStreetCanada.addTextChangedListener(watcher);
        binding.editCityCanada.addTextChangedListener(watcher);
        binding.editCPCanada.addTextChangedListener(watcher);
        binding.spinnerProvincias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                guardaDatosViewModel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void guardaDatosViewModel() {
        if (binding == null) return;
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setCanCalle(binding.editStreetCanada.getText().toString());
        Contrato.setCanCity(binding.editCityCanada.getText().toString());
        Contrato.setCanProvince(binding.spinnerProvincias.getSelectedItem().toString());
        Contrato.setCanPostalCode(binding.editCPCanada.getText().toString());

        Contrato.setTipoDir("CAN");

        Contrato.setPais(binding.editPaisCanada.getText().toString());
        
        viewModel.setContrato(Contrato);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        if (binding.spinnerProvincias.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select a State", Toast.LENGTH_SHORT).show();
            return false;
        }
        return !binding.editStreetCanada.getText().toString().trim().isEmpty() &&
               !binding.editCityCanada.getText().toString().trim().isEmpty() &&
               !binding.editCPCanada.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editStreetCanada.setText("");
            binding.editCityCanada.setText("");
            binding.spinnerProvincias.setSelection(0);
            binding.editCPCanada.setText("");
            guardaDatosViewModel();
        }
    }

    @Override
    public void onDestroyView() {
        guardaDatosViewModel();
        super.onDestroyView();
        binding = null;
    }

    private void setupProvinciasSpinner() {
        String[] estados = getResources().getStringArray(R.array.Provincias);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, estados);
        binding.spinnerProvincias.setAdapter(adapter);
    }
}
