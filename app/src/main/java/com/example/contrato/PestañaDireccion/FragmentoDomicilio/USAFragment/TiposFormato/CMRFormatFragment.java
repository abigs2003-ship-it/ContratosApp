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
import com.example.contrato.databinding.FragmentCmrFormatBinding;

public class CMRFormatFragment extends Fragment implements PestañaDireccionFragment.ValidatableFragment, PestañaDireccionFragment.ClearableFragment {

    private FragmentCmrFormatBinding binding;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCmrFormatBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        setupEstadosUSASpinner();
        cargaDatosExistentes();
        setupAutoGuardado();

        return binding.getRoot();
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            binding.editCMR1.setText(Contrato.getCalle());
            binding.editBox.setText(Contrato.getPoBox());
            binding.editAPO.setText(Contrato.getColonia());
            binding.editCity.setText(Contrato.getCiudad());
            if (Contrato.getUsaState() != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerEstadoUSA.getAdapter();
                if (adapter != null) {
                    int pos = adapter.getPosition(Contrato.getUsaState());
                    if (pos >= 0) binding.spinnerEstadoUSA.setSelection(pos);
                }
            }
            binding.zipcode.setText(Contrato.getCp());
        }
    }

    private void setupAutoGuardado() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { guardaDatosViewModel(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCMR1.addTextChangedListener(watcher);
        binding.editBox.addTextChangedListener(watcher);
        binding.editAPO.addTextChangedListener(watcher);
        binding.editCity.addTextChangedListener(watcher);
        binding.zipcode.addTextChangedListener(watcher);
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

    private void guardaDatosViewModel() {
        if (binding == null) return;
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        Contrato.setCalle(binding.editCMR1.getText().toString());
        Contrato.setPoBox(binding.editBox.getText().toString());
        Contrato.setColonia(binding.editAPO.getText().toString());
        Contrato.setCiudad(binding.editCity.getText().toString());
        Contrato.setUsaState(binding.spinnerEstadoUSA.getSelectedItem().toString());
        Contrato.setCp(binding.zipcode.getText().toString());
        viewModel.setContrato(Contrato);
    }

    @Override
    public boolean isValid() {
        if (binding == null) return false;
        if (binding.spinnerEstadoUSA.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select a State", Toast.LENGTH_SHORT).show();
            return false;
        }
        return (!binding.editCMR1.getText().toString().trim().isEmpty() || 
                !binding.editBox.getText().toString().trim().isEmpty() || 
                !binding.editAPO.getText().toString().trim().isEmpty()) &&
               !binding.editCity.getText().toString().trim().isEmpty() &&
               !binding.zipcode.getText().toString().trim().isEmpty();
    }

    @Override
    public void clearFields() {
        if (binding != null) {
            binding.editCMR1.setText("");
            binding.editBox.setText("");
            binding.editAPO.setText("");
            binding.editCity.setText("");
            binding.spinnerEstadoUSA.setSelection(0);
            binding.zipcode.setText("");
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
