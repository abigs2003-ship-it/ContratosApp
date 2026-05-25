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
import com.example.contrato.databinding.FragmentDomiciliomexicoBinding;

public class DomicilioFragmentMexico extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {
    private FragmentDomiciliomexicoBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliomexicoBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        setupEstadosSpinner();

        cargaDatosExistentes();
        setupAutoGuardado();

        return binding.getRoot();
    }


    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editCalle.setText(Contrato.getMexCalle());
            binding.editNext.setText(Contrato.getMexNumExt());
            binding.editNint.setText(Contrato.getMexNumInt());
            binding.cp.setText(Contrato.getMexCP());
            binding.editColonia.setText(Contrato.getMexColonia());
            binding.editMunicipio.setText(Contrato.getDelegacion());
            binding.editCiudad.setText(Contrato.getMexCiudad());
            if (Contrato.getMexEstado() != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerEstado.getAdapter();
                if (adapter != null) {
                    int pos = adapter.getPosition(Contrato.getMexEstado());
                    if (pos >= 0) binding.spinnerEstado.setSelection(pos);
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
        binding.editNext.addTextChangedListener(watcher);
        binding.editNint.addTextChangedListener(watcher);
        binding.cp.addTextChangedListener(watcher);
        binding.editColonia.addTextChangedListener(watcher);
        binding.editMunicipio.addTextChangedListener(watcher);
        binding.editCiudad.addTextChangedListener(watcher);
        binding.spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        
        Contrato.setMexCalle(binding.editCalle.getText().toString());
        Contrato.setMexNumExt(binding.editNext.getText().toString());
        Contrato.setMexNumInt(binding.editNint.getText().toString());
        Contrato.setMexCP(binding.cp.getText().toString());
        Contrato.setMexColonia(binding.editColonia.getText().toString());
        Contrato.setDelegacion(binding.editMunicipio.getText().toString());
        Contrato.setMexCiudad(binding.editCiudad.getText().toString());
        if (binding.spinnerEstado.getSelectedItem() != null) {
            Contrato.setMexEstado(binding.spinnerEstado.getSelectedItem().toString());
        }
        Contrato.setTipoDir("MEX");

        Contrato.setPais(binding.editPaisMexico.getText().toString());

        
        viewModel.setContrato(Contrato);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        if (binding.editCalle.getText().toString().trim().length() < 3) { //si es menor a 3 caracteres no pasa
            Toast.makeText(requireContext(), "La calle debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.cp.getText().toString().trim().length() < 5) { //si es menor a 3 caracteres no pasa
            Toast.makeText(requireContext(), "El C.P. debe ser de 5 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.spinnerEstado.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Seleccione un Estado", Toast.LENGTH_SHORT).show();
            return false;
        }

        return !binding.editCalle.getText().toString().trim().isEmpty() &&
               !binding.editNext.getText().toString().trim().isEmpty() &&
               !binding.editNext.getText().toString().trim().isEmpty() &&
               !binding.editCiudad.getText().toString().trim().isEmpty() &&
                !binding.editMunicipio.getText().toString().trim().isEmpty() &&
                !binding.editColonia.getText().toString().trim().isEmpty();

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
            binding.spinnerEstado.setSelection(0);
            guardaDatosViewModel();
        }
    }

    @Override
    public void onDestroyView() {
        guardaDatosViewModel();
        super.onDestroyView();
        binding = null;
    }

    private void setupEstadosSpinner() {
        String[] estados = getResources().getStringArray(R.array.Estados);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, estados);
        binding.spinnerEstado.setAdapter(adapter);
    }
}
