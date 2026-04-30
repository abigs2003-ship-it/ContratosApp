package com.example.contrato;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.example.contrato.databinding.FragmentTitularesBinding;
import com.example.contrato.databinding.ListItemPersonBinding;

public class TitularesFragment extends Fragment {

    private FragmentTitularesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTitularesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ocultar Materno si el idioma es inglés
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentLocales.isEmpty()) {
            String lang = currentLocales.get(0).getLanguage();
            if (lang.equals("en")) {
                binding.layoutMaterno.setVisibility(View.GONE);
                binding.layoutMaternoBene.setVisibility(View.GONE);
            }
        }

        //adapter para el spinner de parentesco
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int posicion, View convertView, ViewGroup parent) {
                View v = super.getView(posicion, convertView, parent);
                if (posicion == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount()));
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1;
            }
        };

        String selecciona = getString(R.string.selecciona);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adapter.add(selecciona);
        adapter.add("Padre");
        adapter.add("Madre");
        adapter.add("Hijo");
        adapter.add("Esposo");
        adapter.add("Tío");


        binding.spinnerParentesco.setSelection(adapter.getCount());
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
        binding.spinnerParentesco.setAdapter(adapter);
        binding.spinnerParentescoBene.setAdapter(adapter);

        // datos de prueba
        agregarPersonaAContenedor(binding.containerTitulares, "Juan Pérez García", "15/05/1985", "Ingeniero", "Titular");
        agregarPersonaAContenedor(binding.containerBeneficiarios, "María López", "20/10/2010", "Estudiante", "Hija");

        binding.btnAgregar.setOnClickListener(v -> {
            String nombre = binding.editNombre.getText().toString();
            String paterno = binding.editPaterno.getText().toString();
            String materno = (binding.layoutMaterno.getVisibility() == View.VISIBLE) ? binding.editMaterno.getText().toString() : "";
            String cumple = binding.editCumple.getText().toString();
            String ocupacion = binding.editOcupacion.getText().toString();
            String parentesco = binding.spinnerParentesco.getSelectedItem().toString();

            if (!nombre.isEmpty()) {
                agregarPersonaAContenedor(binding.containerTitulares, nombre + " " + paterno + " " + materno, cumple, ocupacion, parentesco);
                limpiarCamposTitular();
            }
        });

        binding.btnLimpiar.setOnClickListener(v -> limpiarCamposTitular());

        binding.btnBorrar.setOnClickListener(v -> eliminarItemsSeleccionados(binding.containerTitulares));

        binding.btnAgregarBene.setOnClickListener(v -> {
            String nombre = binding.editNombreBene.getText().toString();
            String paterno = binding.editPaternoBene.getText().toString();
            String materno = (binding.layoutMaternoBene.getVisibility() == View.VISIBLE) ? binding.editMaternoBene.getText().toString() : "";
            String cumple = binding.editCumpleBene.getText().toString();
            String ocupacion = binding.editOcupacionBene.getText().toString();
            String parentesco = binding.spinnerParentescoBene.getSelectedItem().toString();

            if (!nombre.isEmpty()) {
                agregarPersonaAContenedor(binding.containerBeneficiarios, nombre + " " + paterno + " " + materno, cumple, ocupacion, parentesco);
                limpiarCamposBene();
            }
        });

        binding.btnLimpiarBene.setOnClickListener(v -> limpiarCamposBene());

        binding.btnBorrarBene.setOnClickListener(v -> eliminarItemsSeleccionados(binding.containerBeneficiarios));
    }

    private void agregarPersonaAContenedor(LinearLayout contenedor, String nombre, String cumple, String ocupacion, String parentesco) {
        ListItemPersonBinding bindingItem = ListItemPersonBinding.inflate(getLayoutInflater(), contenedor, false);
        bindingItem.textNombre.setText(nombre);
        bindingItem.textCumple.setText(cumple);
        bindingItem.textOcupacion.setText(ocupacion);
        bindingItem.textParentesco.setText(parentesco);

        contenedor.addView(bindingItem.getRoot());
    }

    private void eliminarItemsSeleccionados(LinearLayout contenedor) {
        for (int i = contenedor.getChildCount() - 1; i >= 0; i--) {
            View vista = contenedor.getChildAt(i);
            ListItemPersonBinding bindingItem = ListItemPersonBinding.bind(vista);
            if (bindingItem.checkBox.isChecked()) {
                contenedor.removeView(vista);
            }
        }
    }

    private void limpiarCamposTitular() {
        if (binding.editNombre.getText() != null) binding.editNombre.getText().clear();
        if (binding.editPaterno.getText() != null) binding.editPaterno.getText().clear();
        if (binding.editMaterno.getText() != null) binding.editMaterno.getText().clear();
        if (binding.editOcupacion.getText() != null) binding.editOcupacion.getText().clear();
        if (binding.editCumple.getText() != null) binding.editCumple.getText().clear();
        binding.spinnerParentesco.setSelection(binding.spinnerParentesco.getAdapter().getCount());
    }

    private void limpiarCamposBene() {
        if (binding.editNombreBene.getText() != null) binding.editNombreBene.getText().clear();
        if (binding.editPaternoBene.getText() != null) binding.editPaternoBene.getText().clear();
        if (binding.editMaternoBene.getText() != null) binding.editMaternoBene.getText().clear();
        if (binding.editOcupacionBene.getText() != null) binding.editOcupacionBene.getText().clear();
        if (binding.editCumpleBene.getText() != null) binding.editCumpleBene.getText().clear();
        binding.spinnerParentescoBene.setSelection(binding.spinnerParentescoBene.getAdapter().getCount());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
