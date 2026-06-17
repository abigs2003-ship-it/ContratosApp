package com.example.contrato;

import static android.view.View.VISIBLE;
import com.google.android.material.button.MaterialButton;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import androidx.navigation.fragment.NavHostFragment;

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

    private boolean titularSeleccionado = false;
    private boolean beneSeleccionado = false;

    private ContratoModelo.Persona titularPersonaSeleccionada;
    private View titularViewSeleccionada;

    private ContratoModelo.Persona benePersonaSeleccionada;
    private View beneViewSeleccionada;


    // Flag que indica si las listas locales ya fueron inicializadas desde el ViewModel.
    // Sin esto, onPause() puede guardar listas vacías y borrar los datos del contrato.
    private boolean datosLocalesInicializados = false;

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
        //si seleccionas titular y das click en Añadir Firma se abre FirmasFragment con ese titular
        binding.btnFirma.setOnClickListener(v -> {

            if (titularPersonaSeleccionada == null) {
                Toast.makeText(
                        requireContext(),
                        "Seleccione un titular",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            viewModel.setPersonaParaFirma(
                    titularPersonaSeleccionada
            );


            Navigation.findNavController(this.requireView())
                    .navigate(R.id.action_titulares_a_firmas);

        });
        //lo mismo para beneficiarios
        binding.btnFirmaBene.setOnClickListener(v -> {


            if (benePersonaSeleccionada == null) {

                Toast.makeText(
                        requireContext(),
                        "Seleccione un beneficiario",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            viewModel.setPersonaParaFirma(
                    benePersonaSeleccionada
            );


            Navigation.findNavController(v)
                    .navigate(R.id.action_titulares_a_firmas);

        });
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

        // FIX: repoblamos los containers de inmediato usando lo que ya tengamos en
        // titularesList / beneficiariosList (estado en memoria del Fragment).
        //
        // Por qué es necesario: cuando navegamos a FirmasFragment y volvemos con
        // popBackStack(), Navigation vuelve a llamar onCreateView/onViewCreated en
        // esta instancia de TitularesFragment (la vista anterior fue destruida en
        // onDestroyView). El objeto Fragment como tal NO se destruyó, así que
        // titularesList/beneficiariosList siguen teniendo los datos de antes de
        // navegar. Pero el container nuevo (binding.containerTitulares) está vacío,
        // porque es una View recién inflada.
        //
        // Antes, el único lugar donde se llamaba a agregarPersonaaAContenedorTitular/
        // Bene era dentro del observer del ViewModel, y SOLO si la lista local estaba
        // vacía. Como titularesList ya no estaba vacía al volver, ese bloque nunca se
        // ejecutaba y el container se quedaba vacío aunque los datos siguieran vivos
        // en memoria: por eso el titular "desaparecía" visualmente tras guardar la firma.
        if (!titularesList.isEmpty()) {
            binding.containerTitulares.removeAllViews();
            for (ContratoModelo.Persona persona : titularesList) {
                agregarPersonaaAContenedorTitular(binding.containerTitulares, persona, titularesList);
            }
        }
        if (!beneficiariosList.isEmpty()) {
            binding.containerBeneficiarios.removeAllViews();
            for (ContratoModelo.Persona persona : beneficiariosList) {
                agregarPersonaaAContenedorBene(binding.containerBeneficiarios, persona, beneficiariosList);
            }
        }

        viewModel.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            if (contrato == null) return;

            boolean esModoEdicion = Boolean.TRUE.equals(contrato.getModoEdicion());

            // En modo edición, esperamos hasta que getContratoCompleto() haya terminado
            // (datosListos = true). En modo nuevo, no hay nada que esperar.
            if (esModoEdicion && !contrato.isDatosListos()) {
                binding.layoutCargandoTitulares.setVisibility(VISIBLE);
                return;
            }

            // Datos listos — ocultamos el spinner y marcamos las listas como inicializadas.
            // A partir de aquí onPause() puede guardar sin riesgo.
            binding.layoutCargandoTitulares.setVisibility(View.GONE);
            datosLocalesInicializados = true;

            boolean titularesLocalVacios     = titularesList.isEmpty();
            boolean beneficiariosLocalVacios = beneficiariosList.isEmpty();

            // Solo copiamos del ViewModel si la lista local está vacía,
            // para no pisar cambios que el usuario ya haya hecho en esta sesión.
            // Este caso cubre la PRIMERA carga real del fragmento (no el regreso
            // desde FirmasFragment, que ya quedó cubierto arriba).
            if (titularesLocalVacios && !contrato.getTitulares().isEmpty()) {
                titularesList = new ArrayList<>(contrato.getTitulares());
                binding.containerTitulares.removeAllViews();
                for (ContratoModelo.Persona persona : titularesList) {
                    agregarPersonaaAContenedorTitular(binding.containerTitulares, persona, titularesList);

                }
            }

            if (beneficiariosLocalVacios && !contrato.getBeneficiarios().isEmpty()) {
                beneficiariosList = new ArrayList<>(contrato.getBeneficiarios());
                binding.containerBeneficiarios.removeAllViews();
                for (ContratoModelo.Persona persona : beneficiariosList) {
                    agregarPersonaaAContenedorBene(binding.containerBeneficiarios, persona, beneficiariosList);

                }
            }
        });

        //boton agregar presionado en modo no seleccionado (titulares)
        binding.btnAgregar.setOnClickListener(v -> {

            if (titularSeleccionado) {

                if (titularViewSeleccionada != null &&
                        titularPersonaSeleccionada != null) {

                    binding.containerTitulares.removeView(titularViewSeleccionada);

                    titularesList.remove(titularPersonaSeleccionada);

                    titularViewSeleccionada = null;
                    titularPersonaSeleccionada = null;
                    titularSeleccionado = false;

                    guardaDatosViewModel();

                    resetBotones(
                            binding.btnLimpiar,
                            binding.btnAgregar,
                            binding.btnFirma
                    );
                }

                return;
            }

            if (viewModel.getContratoValue() == null) {
                Toast.makeText(requireContext(),
                        "Espere, cargando datos...",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre = binding.editNombre.getText().toString();
            String paterno = binding.editPaterno.getText().toString();
            String materno = (binding.layoutMaterno.getVisibility() == VISIBLE)
                    ? binding.editMaterno.getText().toString()
                    : "";

            String cumple = binding.editFechaCumpleanos.getText().toString();
            String ocupacion = binding.editOcupacion.getText().toString();
            String parentesco =
                    String.valueOf(binding.spinnerParentesco.getSelectedItemPosition());

            if (!nombre.isEmpty()) {

                ContratoModelo.Persona p =
                        new ContratoModelo.Persona(
                                nombre,
                                paterno,
                                materno,
                                ocupacion,
                                parentesco,
                                cumple);

                titularesList.add(p);

                guardaDatosViewModel();

                agregarPersonaaAContenedorTitular(
                        binding.containerTitulares,
                        p,
                        titularesList);

                limpiarCamposTitular();

            } else {

                Toast.makeText(requireContext(),
                        "El nombre es obligatorio",
                        Toast.LENGTH_SHORT).show();
            }
        });
        //boton agregar presionado en modo no seleccionado (beneficiarios)

        binding.btnAgregarBene.setOnClickListener(v -> {

            if (beneSeleccionado) {

                if (beneViewSeleccionada != null &&
                        benePersonaSeleccionada != null) {

                    binding.containerBeneficiarios.removeView(beneViewSeleccionada);

                    beneficiariosList.remove(benePersonaSeleccionada);

                    beneViewSeleccionada = null;
                    benePersonaSeleccionada = null;
                    beneSeleccionado = false;

                    guardaDatosViewModel();

                    resetBotones(
                            binding.btnLimpiarBene,
                            binding.btnAgregarBene,
                            binding.btnFirmaBene
                    );
                }

                return;
            }

            if (viewModel.getContratoValue() == null) {
                Toast.makeText(requireContext(), "Espere, cargando datos...", Toast.LENGTH_SHORT).show();
                return;
            }

            String nombre    = binding.editNombreBene.getText().toString();
            String paterno   = binding.editPaternoBene.getText().toString();
            String materno   = (binding.layoutMaternoBene.getVisibility() == VISIBLE)
                    ? binding.editMaternoBene.getText().toString() : "";
            String cumple    = binding.editFechaCumpleanosBene.getText().toString();
            String ocupacion = binding.editOcupacionBene.getText().toString();
            String parentesco = String.valueOf(binding.spinnerParentescoBene.getSelectedItemPosition());

            if (!nombre.isEmpty()) {
                ContratoModelo.Persona p = new ContratoModelo.Persona(nombre, paterno, materno, ocupacion, parentesco, cumple);
                beneficiariosList.add(p);
                guardaDatosViewModel();
                agregarPersonaaAContenedorBene(binding.containerBeneficiarios, p, beneficiariosList);
                limpiarCamposBene();
            } else {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            }        });

        binding.btnLimpiarBene.setOnClickListener(v -> limpiarCamposBene());

        binding.AceptarTarea.setOnClickListener(v -> {
            if (titularesList.isEmpty()) {
                Toast.makeText(requireContext(), "Debe agregar al menos un titular", Toast.LENGTH_SHORT).show();
                return;
            }
            guardaDatosViewModel();
            Navigation.findNavController(v).navigate(R.id.nav_direcciones);
        });

        //boton editar TITULARES
        binding.btnLimpiar.setOnClickListener(v -> {

            if (!titularSeleccionado ||
                    titularPersonaSeleccionada == null ||
                    titularViewSeleccionada == null) {

                limpiarCamposTitular();
                return;
            }

            ContratoModelo.Persona p = titularPersonaSeleccionada;

            // Llena los campos
            binding.editNombre.setText(p.nombre);
            binding.editPaterno.setText(p.paterno);
            binding.editMaterno.setText(p.materno);
            binding.editOcupacion.setText(p.ocupacion);
            binding.editFechaCumpleanos.setText(p.cumple);

            try {
                binding.spinnerParentesco.setSelection(
                        Integer.parseInt(p.parentesco));
            } catch (Exception ignored) {
            }

            binding.containerTitulares.removeView(
                    titularViewSeleccionada);

            titularesList.remove(p);

            guardaDatosViewModel();

            titularPersonaSeleccionada = null;
            titularViewSeleccionada = null;
            titularSeleccionado = false;

            resetBotones(
                    binding.btnLimpiar,
                    binding.btnAgregar,
                    binding.btnFirma
            );
        });

        //boton editar bene
        binding.btnLimpiarBene.setOnClickListener(v -> {

            if (!beneSeleccionado ||
                    benePersonaSeleccionada == null ||
                    beneViewSeleccionada == null) {

                limpiarCamposBene();
                return;
            }

            ContratoModelo.Persona p = benePersonaSeleccionada;

            binding.editNombreBene.setText(p.nombre);
            binding.editPaternoBene.setText(p.paterno);
            binding.editMaternoBene.setText(p.materno);
            binding.editOcupacionBene.setText(p.ocupacion);
            binding.editFechaCumpleanosBene.setText(p.cumple);

            try {
                binding.spinnerParentescoBene.setSelection(
                        Integer.parseInt(p.parentesco));
            } catch (Exception ignored) {
            }

            binding.containerBeneficiarios.removeView(
                    beneViewSeleccionada);

            beneficiariosList.remove(p);

            guardaDatosViewModel();

            benePersonaSeleccionada = null;
            beneViewSeleccionada = null;
            beneSeleccionado = false;

            resetBotones(
                    binding.btnLimpiarBene,
                    binding.btnAgregarBene,
                    binding.btnFirmaBene
            );
        });
    }

    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }

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

    private void convertirMesANombre(EditText editText) {
        String texto = editText.getText().toString();

        if (texto.length() == 10) {
            try {
                if (esIngles()) {
                    String mesStr = texto.substring(0, 2);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_EN[mes - 1];
                        String fechaFinal = mesPalabra + texto.substring(2);
                        editText.setText(fechaFinal);
                    }
                } else {
                    String mesStr = texto.substring(3, 5);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_ES[mes - 1];
                        String fechaFinal = texto.substring(0, 3) + mesPalabra + texto.substring(5);
                        editText.setText(fechaFinal);
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
                agregarPersonaaAContenedorTitular(binding.containerTitulares, p, titularesList);
            }
            binding.containerBeneficiarios.removeAllViews();
            for (ContratoModelo.Persona p : beneficiariosList) {
                agregarPersonaaAContenedorBene(binding.containerBeneficiarios, p, beneficiariosList);
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
        if (contrato == null) return;

        // Si los datos locales aún no se inicializaron (el observer no ha pasado el spinner),
        // no guardamos para no sobreescribir el ViewModel con listas vacías.
        if (!datosLocalesInicializados) return;

        contrato.setTitulares(new ArrayList<>(titularesList));
        contrato.setBeneficiarios(new ArrayList<>(beneficiariosList));
        viewModel.setContrato(contrato);
    }

    private void muestraDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int anio = c.get(Calendar.YEAR);
        int mes  = c.get(Calendar.MONTH);
        int dia  = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, anio1, mesAnio, diaMes) -> {
                    String lang = getIdiomaActual();
                    String d = String.format(Locale.US, "%02d", diaMes);
                    String m = String.format(Locale.US, "%02d", mesAnio + 1);
                    String y = String.valueOf(anio1);

                    String fechaSeleccionada = lang.equals("en")
                            ? m + "/" + d + "/" + y
                            : d + "/" + m + "/" + y;

                    if (fechaSeleccionada.length() == 10) {
                        try {
                            if (esIngles()) {
                                String mesStr = fechaSeleccionada.substring(0, 2);
                                int me = Integer.parseInt(mesStr);
                                if (me >= 1 && me <= 12) {
                                    String mesPalabra = MESES_EN[me - 1];
                                    String fechaFinal = mesPalabra + fechaSeleccionada.substring(2);
                                    editText.setText(fechaFinal);
                                }
                            } else {
                                String mesStr = fechaSeleccionada.substring(3, 5);
                                int me = Integer.parseInt(mesStr);
                                if (me >= 1 && me <= 12) {
                                    String mesPalabra = MESES_ES[me - 1];
                                    String fechaFinal = fechaSeleccionada.substring(0, 3) + mesPalabra + fechaSeleccionada.substring(5);
                                    editText.setText(fechaFinal);
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }, anio, mes, dia);

        dialog.show();
    }

    private void agregarPersonaaAContenedorTitular(LinearLayout contenedor, ContratoModelo.Persona p, List<ContratoModelo.Persona> list) {
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
        bindingItem.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                titularSeleccionado = true;

                titularPersonaSeleccionada = p;
                titularViewSeleccionada = bindingItem.getRoot();

                setupBotonesTitular();

                if(titularPersonaSeleccionada.imagenFirmaBase64 != null &&
                        !titularPersonaSeleccionada.imagenFirmaBase64.isEmpty()){


                    bindingItem.btnFirma.setVisibility(View.VISIBLE);


                    bindingItem.btnFirma.setOnClickListener(v -> {

                        byte[] bytes = Base64.decode(titularPersonaSeleccionada.imagenFirmaBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ImageView imageView = new ImageView(getContext());
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                        imageView.setPadding(20,20,20,20);

                        new AlertDialog.Builder(getContext()).setTitle("Firma").setView(imageView).setNegativeButton("", null)
                                .setNeutralButton("Rehacer", (dialog, which) -> {titularPersonaSeleccionada.imagenFirmaBase64 = "";
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("persona", titularPersonaSeleccionada);

                                    bundle.putSerializable(
                                            "persona",
                                            titularPersonaSeleccionada
                                    );

                                    NavHostFragment
                                            .findNavController(this)
                                            .navigate(
                                                    R.id.action_titulares_a_firmas,
                                                    bundle
                                            );                                        }
                                ).setPositiveButton("Quitar", (dialog, which) -> {
                                    titularPersonaSeleccionada.imagenFirmaBase64 = "";
                                    guardaDatosViewModel();
                                    bindingItem.btnFirma.setVisibility(View.GONE);
                                }).show();
                    });
                }else{
                    bindingItem.btnFirma.setVisibility(View.GONE);
                }

            } else {

                if (titularPersonaSeleccionada == p) {
                    titularPersonaSeleccionada = null;
                    titularViewSeleccionada = null;
                }

                titularSeleccionado = false;
                resetBotones(binding.btnLimpiar,
                        binding.btnAgregar,
                        binding.btnFirma);
            }
        });

        contenedor.addView(bindingItem.getRoot());
    }
    private void agregarPersonaaAContenedorBene(LinearLayout contenedor, ContratoModelo.Persona p, List<ContratoModelo.Persona> list) {
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
        bindingItem.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                beneSeleccionado = true;

                benePersonaSeleccionada = p;
                beneViewSeleccionada = bindingItem.getRoot();

                setupBotonesBene();

            } else {

                if (benePersonaSeleccionada == p) {
                    benePersonaSeleccionada = null;
                    beneViewSeleccionada = null;
                }

                beneSeleccionado = false;

                resetBotones(binding.btnLimpiarBene,
                        binding.btnAgregarBene,
                        binding.btnFirmaBene);
            }
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



    private void setupBotonesTitular() {
        titularSeleccionado = true;
        //Agregar se convierte en eliminar cuando se selecciona titular
        binding.btnAgregar.setBackgroundTintList(getResources().getColorStateList(R.color.botonEliminar));
        binding.btnFirma.setVisibility(VISIBLE);

        if (esIngles()) {
            binding.btnAgregar.setText("Delete");
        } else {
            binding.btnAgregar.setText("Eliminar");
        }
        binding.btnAgregar.setIcon(getResources().getDrawable(R.drawable.ic_cancelar));
        //limpiar se vuelve editar cuando se selecciona titular
        if(esIngles()){
            binding.btnLimpiar.setText("Edit");
        }else{
            binding.btnLimpiar.setText("Editar");
        }
        binding.btnFirma.setVisibility(VISIBLE);

        binding.btnLimpiar.setIcon(getResources().getDrawable(R.drawable.ic_editar));
        binding.btnLimpiar.setIconTint(getResources().getColorStateList(R.color.white));

    }
    private void setupBotonesBene() {
        beneSeleccionado = true;
        binding.btnLimpiarBene.setIcon(getResources().getDrawable(R.drawable.ic_editar));
        binding.btnLimpiarBene.setIconTint(getResources().getColorStateList(R.color.white));
        binding.btnFirmaBene.setVisibility(VISIBLE);

        binding.btnAgregarBene.setBackgroundTintList(getResources().getColorStateList(R.color.botonEliminar));
        if (esIngles()) {
            binding.btnAgregarBene.setText("Delete");
        } else {
            binding.btnAgregarBene.setText("Eliminar");
        }
        binding.btnAgregarBene.setIcon(getResources().getDrawable(R.drawable.ic_cancelar));
        //limpiar se vuelve editar cuando se selecciona titular
        if(esIngles()){
            binding.btnLimpiarBene.setText("Edit");
        }else{
            binding.btnLimpiarBene.setText("Editar");
        }

    }
    private void resetBotones(MaterialButton limpiar, MaterialButton agregar, MaterialButton firma){
        beneSeleccionado = false;
        titularSeleccionado = false;

        //limpiar
        limpiar.setIcon(getResources().getDrawable(R.drawable.ic_broom));
        if (esIngles()) {
            limpiar.setText("Clean");
        } else {
            limpiar.setText("Limpiar");
        }

        //agregar
        agregar.setBackgroundTintList(getResources().getColorStateList(R.color.botonAgregar));
        if (esIngles()) {
            agregar.setText("Add");
        } else {
            agregar.setText("Agregar");
        }
        agregar.setIcon(getResources().getDrawable(R.drawable.ic_agregar));


        firma.setVisibility(View.INVISIBLE);

    }


}