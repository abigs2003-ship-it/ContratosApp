package com.example.contrato;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
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
    private SharedContractViewModel viewModel;
    private List<ContratoModelo.Person> titularesList = new ArrayList<>();
    private List<ContratoModelo.Person> beneficiariosList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTitularesBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);
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

        binding.editFechaCumpleanos.setOnClickListener(v -> showDatePickerDialog(binding.editFechaCumpleanos));
        binding.editCumpleBene.setOnClickListener(v -> showDatePickerDialog(binding.editCumpleBene));

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
            String parentesco = binding.spinnerParentesco.getSelectedItem().toString();

            if (!nombre.isEmpty()) {
                if (binding.spinnerParentesco.getSelectedItemPosition() == adapter.getCount()) {
                    Toast.makeText(requireContext(), R.string.selecciona, Toast.LENGTH_SHORT).show();
                    return;
                }
                ContratoModelo.Person p = new ContratoModelo.Person(nombre, paterno, materno, ocupacion, parentesco, cumple);
                titularesList.add(p);
                saveDataToViewModel();
                agregarPersonaAContenedor(binding.containerTitulares, p, titularesList);
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
            String cumple = binding.editCumpleBene.getText().toString();
            String ocupacion = binding.editOcupacionBene.getText().toString();
            String parentesco = binding.spinnerParentescoBene.getSelectedItem().toString();

            if (!nombre.isEmpty()) {
                if (binding.spinnerParentescoBene.getSelectedItemPosition() == adapter.getCount()) {
                    Toast.makeText(requireContext(), R.string.selecciona, Toast.LENGTH_SHORT).show();
                    return;
                }
                ContratoModelo.Person p = new ContratoModelo.Person(nombre, paterno, materno, ocupacion, parentesco, cumple);
                beneficiariosList.add(p);
                saveDataToViewModel();
                agregarPersonaAContenedor(binding.containerBeneficiarios, p, beneficiariosList);
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
            saveDataToViewModel();
            Navigation.findNavController(v).navigate(R.id.nav_direcciones);
        });
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            titularesList = new ArrayList<>(contract.getTitulares());
            beneficiariosList = new ArrayList<>(contract.getBeneficiarios());
            
            binding.containerTitulares.removeAllViews();
            for (ContratoModelo.Person p : titularesList) {
                agregarPersonaAContenedor(binding.containerTitulares, p, titularesList);
            }
            binding.containerBeneficiarios.removeAllViews();
            for (ContratoModelo.Person p : beneficiariosList) {
                agregarPersonaAContenedor(binding.containerBeneficiarios, p, beneficiariosList);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDataToViewModel();
    }

    private void saveDataToViewModel() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();
        
        contract.setTitulares(new ArrayList<>(titularesList));
        contract.setBeneficiarios(new ArrayList<>(beneficiariosList));
        
        viewModel.setContract(contract);
    }

    private void showDatePickerDialog(EditText editText) {
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

    private void agregarPersonaAContenedor(LinearLayout contenedor, ContratoModelo.Person p, List<ContratoModelo.Person> list) {
        ListItemPersonBinding bindingItem = ListItemPersonBinding.inflate(getLayoutInflater(), contenedor, false);
        
        String fullName = p.nombre + " " + (p.paterno != null ? p.paterno : "") + " " + (p.materno != null ? p.materno : "");
        bindingItem.textNombre.setText(fullName.trim());
        bindingItem.textCumple.setText(p.cumple);
        bindingItem.textOcupacion.setText(p.ocupacion);
        bindingItem.textParentesco.setText(p.parentesco);
        
        bindingItem.btnEliminar.setOnClickListener(v -> {
            contenedor.removeView(bindingItem.getRoot());
            list.remove(p);
            saveDataToViewModel();
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
        if (binding.editCumpleBene.getText() != null) binding.editCumpleBene.getText().clear();
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
