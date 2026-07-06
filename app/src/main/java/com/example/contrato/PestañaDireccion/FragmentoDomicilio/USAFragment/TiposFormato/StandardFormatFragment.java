package com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato;

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
import com.example.contrato.databinding.FragmentStandardFormatBinding;

public class StandardFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {

    private FragmentStandardFormatBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStandardFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        setupEstadosUSASpinner();
        cargaDatosExistentes();
        setupAutoGuardado();

        return binding.getRoot();
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editCalle.setText(Contrato.getUsaCalle());
            binding.editCity.setText(Contrato.getUsaCity());
            binding.editZipCode.setText(Contrato.getUsaZip());
            if (Contrato.getUsaState() != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerEstadoUSA.getAdapter();
                if (adapter != null) {
                    int pos = adapter.getPosition(Contrato.getUsaState());
                    if (pos >= 0) binding.spinnerEstadoUSA.setSelection(pos);
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
        binding.editCalle.addTextChangedListener(watcher);
        binding.editCity.addTextChangedListener(watcher);
        setupZipCodeFormatter(watcher);
        binding.editCountryUSA.addTextChangedListener(watcher);
        binding.spinnerEstadoUSA.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                guardaDatosViewModel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void setupZipCodeFormatter(TextWatcher autoSaveWatcher) {

        binding.editZipCode.addTextChangedListener(new TextWatcher() {

            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {

                if (isFormatting) return;

                isFormatting = true;

                // Solo números
                String digits = editable.toString().replaceAll("[^\\d]", "");

                // Máximo 9 dígitos (ZIP+4)
                if (digits.length() > 9) {
                    digits = digits.substring(0, 9);
                }

                String formatted;

                if (digits.length() > 5) {
                    formatted = digits.substring(0, 5) + "-" + digits.substring(5);
                } else {
                    formatted = digits;
                }

                if (!formatted.equals(editable.toString())) {
                    binding.editZipCode.setText(formatted);
                    binding.editZipCode.setSelection(formatted.length());
                }

                isFormatting = false;

                // Guarda en el ViewModel
                autoSaveWatcher.onTextChanged(formatted, 0, 0, formatted.length());
            }
        });
    }

    private void guardaDatosViewModel() {
        if (binding == null) return;
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setUsaCalle(binding.editCalle.getText().toString());
        Contrato.setUsaCity(binding.editCity.getText().toString());
        Contrato.setUsaState(binding.spinnerEstadoUSA.getSelectedItem().toString());
        Contrato.setUsaZip(binding.editZipCode.getText().toString());

        Contrato.setTipoDir("US1");
        Contrato.setPais(binding.editCountryUSA.getText().toString());


        viewModel.setContrato(Contrato);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        if (binding.spinnerEstadoUSA.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select a State", Toast.LENGTH_SHORT).show();
            return false;
        }
        return !binding.editCalle.getText().toString().trim().isEmpty() &&
               !binding.editCity.getText().toString().trim().isEmpty() &&
               !binding.editZipCode.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editCalle.setText("");
            binding.editCity.setText("");
            binding.spinnerEstadoUSA.setSelection(0);
            binding.editZipCode.setText("");
            guardaDatosViewModel();
        }
    }

    @Override
    public void onDestroyView() {
        guardaDatosViewModel();
        super.onDestroyView();
        binding = null;
    }

    private void setupEstadosUSASpinner() {
        String[] estados = getResources().getStringArray(R.array.EstadosUSA);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, estados);
        binding.spinnerEstadoUSA.setAdapter(adapter);
    }
}
