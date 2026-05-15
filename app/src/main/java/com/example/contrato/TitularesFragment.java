package com.example.contrato;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contrato.databinding.FragmentTitularesBinding;
import com.example.contrato.databinding.ListItemPersonBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TitularesFragment extends Fragment {

    private FragmentTitularesBinding binding;
    private SharedContratoViewModel viewModel;
    private List<ContratoModelo.Persona> titularesList = new ArrayList<>();
    private List<ContratoModelo.Persona> beneficiariosList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTitularesBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        String lang = "es";
        if (!currentLocales.isEmpty()) {
            lang = currentLocales.get(0).getLanguage();
            if (lang.equals("en")) {
                binding.layoutMaterno.setVisibility(View.GONE);
                binding.layoutMaternoBene.setVisibility(View.GONE);
            }
        }

        binding.textInputLayoutFechaCumpleanos.setEndIconOnClickListener(v -> {
            muestraDatePicker(binding.editFechaCumpleanos);
        });
        binding.textInputLayoutFechaCumpleanosBene.setEndIconOnClickListener(v -> {
            muestraDatePicker(binding.editFechaCumpleanosBene);
        });

        setupFormatoFecha(binding.editFechaCumpleanos);
        setupFormatoFecha(binding.editFechaCumpleanosBene);

        List<String> parentescosList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.parentescos)));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.spinner_parentesco, android.R.id.text1, parentescosList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                if (position == getCount()) {
                    v.setTextColor(Color.GRAY);
                } else {
                    v.setTextColor(Color.BLACK);
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
        binding.spinnerParentesco.setAdapter(adapter);
        binding.spinnerParentescoBene.setAdapter(adapter);
        
        binding.spinnerParentesco.setSelection(adapter.getCount());
        binding.spinnerParentescoBene.setSelection(adapter.getCount());

        loadExistingData();

        binding.btnAgregar.setOnClickListener(v -> {
            String nombre = binding.editNombre.getText().toString();
            String paterno = binding.editPaterno.getText().toString();
            String materno = (binding.layoutMaterno.getVisibility() == View.VISIBLE) ? binding.editMaterno.getText().toString() : "";
            String cumple = binding.editFechaCumpleanos.getText().toString();
            String ocupacion = binding.editOcupacion.getText().toString();
            
            int position = binding.spinnerParentesco.getSelectedItemPosition();
            if (position == adapter.getCount()) {
                Toast.makeText(requireContext(), R.string.selecciona, Toast.LENGTH_SHORT).show();
                return;
            }
            String parentesco = String.valueOf(position);

            if (!nombre.isEmpty()) {
                ContratoModelo.Persona p = new ContratoModelo.Persona(nombre, paterno, materno, ocupacion, parentesco, cumple);
                titularesList.add(p);
                guardaDatosViewModel();
                agregarPersonaaAContenedor(binding.containerTitulares, p, titularesList);
                limpiarCamposTitular();
            } else {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLimpiar.setOnClickListener(v -> limpiarCamposTitular());

        binding.btnAgregarBene.setOnClickListener(v -> {
            String nombre = binding.editNombreBene.getText().toString();
            String paterno = binding.editPaternoBene.getText().toString();
            String materno = (binding.layoutMaternoBene.getVisibility() == View.VISIBLE) ? binding.editMaternoBene.getText().toString() : "";
            String cumple = binding.editFechaCumpleanosBene.getText().toString();
            String ocupacion = binding.editOcupacionBene.getText().toString();

            int position = binding.spinnerParentescoBene.getSelectedItemPosition();
            if (position == adapter.getCount()) {
                Toast.makeText(requireContext(), R.string.selecciona, Toast.LENGTH_SHORT).show();
                return;
            }
            String parentesco = String.valueOf(position);

            if (!nombre.isEmpty()) {
                ContratoModelo.Persona p = new ContratoModelo.Persona(nombre, paterno, materno, ocupacion, parentesco, cumple);
                beneficiariosList.add(p);
                guardaDatosViewModel();
                agregarPersonaaAContenedor(binding.containerBeneficiarios, p, beneficiariosList);
                limpiarCamposBene();
            } else {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnLimpiarBene.setOnClickListener(v -> limpiarCamposBene());

        binding.AceptarTarea.setOnClickListener(v -> {
            if (titularesList.isEmpty()) {
                Toast.makeText(requireContext(), "Debe agregar al menos un titular", Toast.LENGTH_SHORT).show();
                return;
            }
            guardaDatosViewModel();
            Navigation.findNavController(v).navigate(R.id.nav_direcciones);
        });
    }

    private void setupFormatoFecha(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String prev = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prev = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.length() < prev.length()) return; 

                if (str.length() == 2 || str.length() == 5) {
                    s.append("/");
                }
            }
        });
    }

    private void loadExistingData() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            titularesList = new ArrayList<>(Contrato.getTitulares());
            beneficiariosList = new ArrayList<>(Contrato.getBeneficiarios());
            
            binding.containerTitulares.removeAllViews();
            for (ContratoModelo.Persona p : titularesList) {
                agregarPersonaaAContenedor(binding.containerTitulares, p, titularesList);
            }
            binding.containerBeneficiarios.removeAllViews();
            for (ContratoModelo.Persona p : beneficiariosList) {
                agregarPersonaaAContenedor(binding.containerBeneficiarios, p, beneficiariosList);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        guardaDatosViewModel();
    }

    private void guardaDatosViewModel() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();
        
        Contrato.setTitulares(new ArrayList<>(titularesList));
        Contrato.setBeneficiarios(new ArrayList<>(beneficiariosList));
        
        viewModel.setContrato(Contrato);
    }

    private void muestraDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate;
                    LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
                    String lang = "es";
                    if (!currentLocales.isEmpty()) {
                        lang = currentLocales.get(0).getLanguage();
                    }

                    if (lang.equals("en")) {
                        selectedDate = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    } else {
                        selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    }
                    editText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void agregarPersonaaAContenedor(LinearLayout contenedor, ContratoModelo.Persona p, List<ContratoModelo.Persona> list) {
        ListItemPersonBinding bindingItem = ListItemPersonBinding.inflate(getLayoutInflater(), contenedor, false);
        
        String fullName = p.nombre + " " + (p.paterno != null ? p.paterno : "") + " " + (p.materno != null ? p.materno : "");
        bindingItem.textNombre.setText(fullName.trim());
        bindingItem.textCumple.setText(p.cumple);
        bindingItem.textOcupacion.setText(p.ocupacion);
        
        
        String parentescoDisplay = p.parentesco;
        try {
            int pos = Integer.parseInt(p.parentesco);
            String[] array = getResources().getStringArray(R.array.parentescos);
            if (pos >= 0 && pos < array.length) {
                parentescoDisplay = array[pos];
            }
        } catch (Exception ignored) {}
        bindingItem.textParentesco.setText(parentescoDisplay);
        
        bindingItem.btnEliminar.setOnClickListener(v -> {
            contenedor.removeView(bindingItem.getRoot());
            list.remove(p);
            guardaDatosViewModel();
        });

        contenedor.addView(bindingItem.getRoot());
    }

    private void limpiarCamposTitular() {
        if (binding.editNombre.getText() != null) binding.editNombre.getText().clear();
        if (binding.editPaterno.getText() != null) binding.editPaterno.getText().clear();
        if (binding.editMaterno.getText() != null) binding.editMaterno.getText().clear();
        if (binding.editOcupacion.getText() != null) binding.editOcupacion.getText().clear();
        if (binding.editFechaCumpleanos.getText() != null) binding.editFechaCumpleanos.getText().clear();
        if (binding.spinnerParentesco.getAdapter() != null) {
            binding.spinnerParentesco.setSelection(binding.spinnerParentesco.getAdapter().getCount());
        }
    }

    private void limpiarCamposBene() {
        if (binding.editNombreBene.getText() != null) binding.editNombreBene.getText().clear();
        if (binding.editPaternoBene.getText() != null) binding.editPaternoBene.getText().clear();
        if (binding.editMaternoBene.getText() != null) binding.editMaternoBene.getText().clear();
        if (binding.editOcupacionBene.getText() != null) binding.editOcupacionBene.getText().clear();
        if (binding.editFechaCumpleanosBene.getText() != null) binding.editFechaCumpleanosBene.getText().clear();
        if (binding.spinnerParentescoBene.getAdapter() != null) {
            binding.spinnerParentescoBene.setSelection(binding.spinnerParentescoBene.getAdapter().getCount());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
