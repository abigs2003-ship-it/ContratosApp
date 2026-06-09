package com.example.contrato;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import java.util.Locale;

public class TitularesFragment extends Fragment {

    private FragmentTitularesBinding binding;
    private SharedContratoViewModel viewModel;
    private List<ContratoModelo.Persona> titularesList = new ArrayList<>();
    private List<ContratoModelo.Persona> beneficiariosList = new ArrayList<>();
    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};


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

        viewModel.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            if (contrato == null) return;

            boolean esModoEdicion = Boolean.TRUE.equals(contrato.getModoEdicion());

            boolean titularesLocalVacios     = titularesList.isEmpty();
            boolean beneficiariosLocalVacios = beneficiariosList.isEmpty();
            boolean contratoTieneTitulares     = !contrato.getTitulares().isEmpty();
            boolean contratoTieneBeneficiarios = !contrato.getBeneficiarios().isEmpty();

            // En modo edición mostramos el spinner mientras los datos no hayan llegado todavía
            Log.d("titulares vacios", String.valueOf(titularesLocalVacios));
            Log.d("contrato tiene titulares", String.valueOf(contratoTieneTitulares));
            if (esModoEdicion && titularesLocalVacios && !contratoTieneTitulares) {
                binding.layoutCargandoTitulares.setVisibility(View.VISIBLE);
                return; // esperamos a la siguiente emisión con los datos ya cargados
            }

            // Datos listos — ocultamos el spinner
            binding.layoutCargandoTitulares.setVisibility(View.GONE);

            if (titularesLocalVacios && contratoTieneTitulares) {
                titularesList = new ArrayList<>(contrato.getTitulares());
                binding.containerTitulares.removeAllViews();
                for (ContratoModelo.Persona persona : titularesList) {
                    agregarPersonaaAContenedor(binding.containerTitulares, persona, titularesList);
                }
            }

            if (beneficiariosLocalVacios && contratoTieneBeneficiarios) {
                beneficiariosList = new ArrayList<>(contrato.getBeneficiarios());
                binding.containerBeneficiarios.removeAllViews();
                for (ContratoModelo.Persona persona : beneficiariosList) {
                    agregarPersonaaAContenedor(binding.containerBeneficiarios, persona, beneficiariosList);
                }
            }
        });
        binding.btnAgregar.setOnClickListener(v -> {
            // Esperamos a que el contrato esté cargado antes de permitir agregar
            if (viewModel.getContratoValue() == null) {
                Toast.makeText(requireContext(), "Espere, cargando datos...", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre    = binding.editNombre.getText().toString();
            String paterno   = binding.editPaterno.getText().toString();
            String materno   = (binding.layoutMaterno.getVisibility() == View.VISIBLE)
                    ? binding.editMaterno.getText().toString() : "";
            String cumple    = binding.editFechaCumpleanos.getText().toString();
            String ocupacion = binding.editOcupacion.getText().toString();
            String parentesco = String.valueOf(binding.spinnerParentesco.getSelectedItemPosition());

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

        binding.btnAgregarBene.setOnClickListener(v -> {
            // Igual para beneficiarios
            if (viewModel.getContratoValue() == null) {
                Toast.makeText(requireContext(), "Espere, cargando datos...", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre    = binding.editNombreBene.getText().toString();
            String paterno   = binding.editPaternoBene.getText().toString();
            String materno   = (binding.layoutMaternoBene.getVisibility() == View.VISIBLE)
                    ? binding.editMaternoBene.getText().toString() : "";
            String cumple    = binding.editFechaCumpleanosBene.getText().toString();
            String ocupacion = binding.editOcupacionBene.getText().toString();
            String parentesco = String.valueOf(binding.spinnerParentescoBene.getSelectedItemPosition());

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

    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }

    //si español DD/MM/AAAA, si ingles MM/DD/YYYY
    private void setupFormatoFecha(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            private boolean actualizandose = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (actualizandose) return;
                if (s.toString().matches(".*[a-zA-ZáéíóúÁÉÍÓÚ]+.*")) return;

                actualizandose = true;

                String digitos = s.toString().replaceAll("[^\\d]", "");
                if (digitos.length() > 8) digitos = digitos.substring(0, 8);

                if (digitos.length() >= 4) {
                    if (esIngles()) {
                        // valida me
                        String mesStr = digitos.substring(0, 2);
                        int mes = Integer.parseInt(mesStr);
                        if (mes > 12) {
                            digitos = "12" + digitos.substring(2);
                            Toast.makeText(requireContext(), "Month must be 12 or less", Toast.LENGTH_SHORT).show();
                        }
                        if (mes == 0) {
                            digitos = "01" + digitos.substring(2);
                        }
                    } else {
                        // valida mes
                        String mesStr = digitos.substring(2, 4);
                        int mes = Integer.parseInt(mesStr);
                        if (mes > 12) {
                            digitos = digitos.substring(0, 2) + "12" + digitos.substring(4);
                            Toast.makeText(requireContext(), "El mes debe ser menor a 12", Toast.LENGTH_SHORT).show();
                        }
                        if (mes == 0) {
                            digitos = digitos.substring(0, 2) + "01" + digitos.substring(4);
                        }
                    }
                }

                // se agregan los /
                StringBuilder formateado = new StringBuilder();
                for (int i = 0; i < digitos.length(); i++) {
                    formateado.append(digitos.charAt(i));
                    if ((i == 1 || i == 3) && i != digitos.length() - 1) {
                        formateado.append("/");
                    }
                }

                s.replace(0, s.length(), formateado.toString());
                actualizandose = false;
            }
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) convertirMesANombre(editText);
        });

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) convertirMesANombre(editText);
            return false;
        });
    }

    //cambia de 01/02/2000 a 01/feb/2000
    private void convertirMesANombre(EditText editText) {
        String texto = editText.getText().toString();

        if (texto.length() == 10) {
            try {

                if (esIngles()) {
                    String mesStr = texto.substring(0, 2);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_EN[mes - 1];
                        String fechaFinal = mesPalabra + texto.substring(2); // "Mar/15/2025"
                        editText.setText(fechaFinal);
                    }
                } else {
                    String mesStr = texto.substring(3, 5);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_ES[mes - 1];
                        String fechaFinal = texto.substring(0, 3) + mesPalabra + texto.substring(5);
                        editText.setText(fechaFinal); // "15/mar/2025"
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
    }
    private String getIdiomaActual() {
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentLocales.isEmpty()) {
            return currentLocales.get(0).getLanguage();
        }
        return Locale.getDefault().getLanguage();
    }



    private void cargaDatosExistentes() {
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
        ContratoModelo contrato = viewModel.getContratoValue();

        if (contrato == null) {
            return;
        }

        contrato.setTitulares(new ArrayList<>(titularesList));
        contrato.setBeneficiarios(new ArrayList<>(beneficiariosList));

        viewModel.setContrato(contrato);
    }

    private void muestraDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int anio  = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia   = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, anio1, mesAnio, diaMes) -> {
                    String lang = getIdiomaActual();
                    String d = String.format(Locale.US, "%02d", diaMes);
                    String m = String.format(Locale.US, "%02d", mesAnio + 1);
                    String y = String.valueOf(anio1);

                    String fechaSeleccionada = lang.equals("en")
                            ? m + "/" + d + "/" + y   // MM/DD/YYYY
                            : d + "/" + m + "/" + y;  // DD/MM/YYYY

                    if (fechaSeleccionada.length() == 10) {
                        try {

                            if (esIngles()) {
                                String mesStr = fechaSeleccionada.substring(0, 2);
                                int me = Integer.parseInt(mesStr);
                                if (me >= 1 && me <= 12) {
                                    String mesPalabra = MESES_EN[me - 1];
                                    String fechaFinal = mesPalabra + fechaSeleccionada.substring(2); // "Mar/15/2025"
                                    editText.setText(fechaFinal);
                                }
                            } else {
                                String mesStr = fechaSeleccionada.substring(3, 5);
                                int me = Integer.parseInt(mesStr);
                                if (me >= 1 && me <= 12) {
                                    String mesPalabra = MESES_ES[me - 1];
                                    String fechaFinal = fechaSeleccionada.substring(0, 3) + mesPalabra + fechaSeleccionada.substring(5);
                                    editText.setText(fechaFinal); // "15/mar/2025"
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }, anio, mes, dia);

        dialog.show();
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
