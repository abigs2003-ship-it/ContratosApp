package com.example.contrato;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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

import com.example.contrato.databinding.FragmentFinanciamientoBinding;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FinanciamientoFragment extends Fragment {

    private FragmentFinanciamientoBinding binding;
    private SharedContratoViewModel viewModel;
    private final Calendar fechaPrimerPagoSeleccionada = Calendar.getInstance();

    private static final String[] TIPOS_PERIODO_VALORES = {
            "Mensual", "Bimestral", "Trimestral", "Cuatrimestral", "Semestral", "Anual"
    };
    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};



    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private boolean columnasVisibles = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanciamientoBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarSpinnerTipoPeriodo();
        cargaDatosExistentes();
        setupObservers();
        formateaMontos(binding.etMontoFinanciar);

        binding.textInputLayoutFechaPrimerpago.setEndIconOnClickListener(v -> {
            muestraDatePicker(binding.etFechaPrimerPago);
        });

        setupFormatoFecha(binding.etFechaPrimerPago);

        binding.btnCalcular.setOnClickListener(v -> calcularAmortizacion());
        binding.btnLimpiar.setOnClickListener(v -> limpiarTodo());

        binding.mostrar.setOnClickListener(v -> {
            columnasVisibles = !columnasVisibles;
            actualizarVisibilidadColumnas();
        });


        viewModel.getContrato().observe(getViewLifecycleOwner(), contrato -> {

            if (contrato.getModoEdicion()) {
                binding.btnEnviar.setText("Actualizar contrato");
            } else {
                binding.btnEnviar.setText("Enviar contrato");
            }

        });
        binding.btnEnviar.setOnClickListener(v -> {

            ContratoModelo contrato = viewModel.getContratoValue();

            if (contrato.getModoEdicion()) {
                viewModel.actualizaContratoBaseDatos(contrato);
            } else {
                viewModel.guardaContratoBaseDatos();
            }
        });

        binding.btnEnviar.setOnClickListener(v -> mostrarConfirmacionEnvio());

        actualizarVisibilidadColumnas();
    }

    private String getIdiomaActual() {
        LocaleListCompat localeActual = AppCompatDelegate.getApplicationLocales();
        if (!localeActual.isEmpty()) {
            return localeActual.get(0).getLanguage();
        }
        return Locale.getDefault().getLanguage();
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
                        // valida mes ingles
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
                        // valida mes español
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


    private String convertirMesANombreString(String s) {

        String resultado = "";
        if (s.length() == 10) {
            try {

                if (esIngles()) {
                    String mesStr = s.substring(0, 2);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_EN[mes - 1];
                        String fechaFinal = mesPalabra + s.substring(2); // "Mar/15/2025"
                        resultado = fechaFinal;
                    }
                } else {
                    String mesStr = s.substring(3, 5);
                    int mes = Integer.parseInt(mesStr);
                    if (mes >= 1 && mes <= 12) {
                        String mesPalabra = MESES_ES[mes - 1];
                        String fechaFinal = s.substring(0, 3) + mesPalabra + s.substring(5);
                        resultado = fechaFinal; // "15/mar/2025"
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        return resultado;
    }

    private String convertirMesANumero(String s) {
        if (s == null || s.length() != 11) return "";

        try {
            if (esIngles()) {
                String mesPalabra = s.substring(0, 3);

                for (int i = 0; i < MESES_EN.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_EN[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);

                        // jan/15/2025 → 01/15/2025
                        return mesNumero + s.substring(3);
                    }
                }

            } else {
                String mesPalabra = s.substring(3, 6);

                for (int i = 0; i < MESES_ES.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_ES[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);

                        // 15/may/2025 → 15/05/2025
                        return s.substring(0, 3) + mesNumero + s.substring(6);
                    }
                }
            }
        } catch (Exception ignored) {}

        return "";
    }
    //añade $ y comas a los campos de monto
    private void formateaMontos(EditText et) {
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                et.removeTextChangedListener(this);

                try {
                    String texto = s.toString()
                            .replace("$", "")
                            .replace(",", "")
                            .trim();

                    boolean terminaConPunto = texto.endsWith(".");

                    if (!texto.isEmpty()) {
                        double numero = Double.parseDouble(texto);

                        DecimalFormat formato =
                                (DecimalFormat) NumberFormat.getInstance(Locale.US);

                        if (texto.contains(".")) {
                            formato.applyPattern("$#,##0.##");
                        } else {
                            formato.applyPattern("$#,##0");
                        }

                        String formateado = formato.format(numero);

                        if (terminaConPunto) {
                            formateado += ".";
                        }

                        et.setText(formateado);
                        et.setSelection(et.getText().length()); // fixed
                    }

                } catch (NumberFormatException ignored) {}

                et.addTextChangedListener(this);
            }
        };

        et.addTextChangedListener(watcher);
    }
    private void setupObservers() {
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), exito -> {
            if (exito == null || !exito) return;

            ContratoModelo contrato = viewModel.getContratoValue();

            if (contrato != null && Boolean.TRUE.equals(contrato.getModoEdicion())) {
                Toast.makeText(requireContext(), "Contrato actualizado correctamente", Toast.LENGTH_SHORT).show();
                requireActivity().finish(); // ← finish() en el Activity que contiene el Fragment
                return;
            }

            Toast.makeText(requireContext(), "Contrato guardado correctamente", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            if (Contrato.getMontoFinanciar() != null) binding.etMontoFinanciar.setText(Contrato.getMontoFinanciar());
            if (Contrato.getNumPagos() != null) binding.etNumeroPagos.setText(Contrato.getNumPagos());
            if (Contrato.getTasaInteres() != null) binding.etTasaInteres.setText(Contrato.getTasaInteres());

            if (Contrato.getFechaPrimerPago() != null && !Contrato.getFechaPrimerPago().isEmpty()) {
                parseFechaEnCalendario(Contrato.getFechaPrimerPago(), fechaPrimerPagoSeleccionada);
                binding.etFechaPrimerPago.setText(formatFecha(fechaPrimerPagoSeleccionada));
            } else {
                binding.etFechaPrimerPago.setText(""); // Asegura que esté vacío para que autollenar funcione
            }

            if (Contrato.getTipoPeriodo() != null) {
                for (int i = 0; i < TIPOS_PERIODO_VALORES.length; i++) {
                    if (TIPOS_PERIODO_VALORES[i].equals(Contrato.getTipoPeriodo())) {
                        binding.spinnerTipoPeriodo.setSelection(i);
                        break;
                    }
                }
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

        int pos = binding.spinnerTipoPeriodo.getSelectedItemPosition();
        String tipoPeriodo = "";
        switch (pos) {
            case 0:
                tipoPeriodo = "Mensual";
                break;
            case 1:
                tipoPeriodo = "Bimestral";
                break;
            case 2:
                tipoPeriodo = "Trimestral";
                break;
            case 3:
                tipoPeriodo = "Cuatrimestral";
                break;
            case 4:
                tipoPeriodo = "Semestral";
                break;
            case 5:
                tipoPeriodo = "Anual";
                break;
        }

        Contrato.setTipoPeriodo(tipoPeriodo);

        Contrato.setFechaPrimerPago(binding.etFechaPrimerPago.getText().toString());
        Contrato.setNumPagos(binding.etNumeroPagos.getText().toString());
        Contrato.setTasaInteres(binding.etTasaInteres.getText().toString());
        Contrato.setMontoFinanciar(binding.etMontoFinanciar.getText().toString());

        viewModel.setContrato(Contrato);
    }

    private void mostrarConfirmacionEnvio() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Envío")
                .setMessage("¿Está seguro que desea enviar el contrato?")
                .setPositiveButton("Enviar", (dialog, which) -> finalizarContrato())
                .setNegativeButton("Regresar", null)
                .show();
    }
    private void parseFechaEnCalendario(String fecha, Calendar cal) {
        try {
            String[] partes = fecha.split("/");

            String lang = getIdiomaActual();

            int day;
            int month;
            int year;

            if (lang.equals("en")) {
                // MM/DD/YYYY
                month = Integer.parseInt(partes[0]) - 1;
                day = Integer.parseInt(partes[1]);
                year = Integer.parseInt(partes[2]);
            } else {
                // DD/MM/YYYY
                day = Integer.parseInt(partes[0]);
                month = Integer.parseInt(partes[1]) - 1;
                year = Integer.parseInt(partes[2]);
            }

            cal.set(year, month, day);

        } catch (Exception e) {
            cal.setTime(new Date());
        }
    }
    private String formatFecha(Calendar cal) {
        String lang = getIdiomaActual();

        String d = String.format(Locale.US, "%02d",
                cal.get(Calendar.DAY_OF_MONTH));

        String m = String.format(Locale.US, "%02d",
                cal.get(Calendar.MONTH) + 1);

        String y = String.valueOf(cal.get(Calendar.YEAR));

        return lang.equals("en")
                ? m + "/" + d + "/" + y
                : d + "/" + m + "/" + y;
    }

    private void finalizarContrato() {
        guardaDatosViewModel();
        ContratoModelo Contrato = viewModel.getContratoValue();

        if (Contrato.getId() == null) {
            Contrato.setId(String.valueOf(System.currentTimeMillis()));
        }

        SimpleDateFormat metaSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String now = metaSdf.format(new Date());
        if (Contrato.getFechaCreacion() == null) {
            Contrato.setFechaCreacion(now);
        }
        Contrato.setFechaModificacion(now);

        if (!Contrato.getTitulares().isEmpty()) {
            ContratoModelo.Persona p = Contrato.getTitulares().get(0);
            Contrato.setClientName(p.nombre + " " + (p.paterno != null ? p.paterno : ""));
        } else {
            Contrato.setClientName("Sin nombre");
        }

        ContratoManager.getInstance().actualizaContrato(Contrato);

        viewModel.guardaContratoBaseDatos();
    }

    private void irAlMenu() {
        Intent intent = new Intent(requireActivity(), MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void actualizarVisibilidadColumnas() {
        int visibilidad = columnasVisibles ? View.VISIBLE : View.GONE;
        ViewGroup tableContainer = (ViewGroup) binding.contenedorFilasTabla.getParent();
        if (tableContainer == null) return;

        ViewGroup header = (ViewGroup) tableContainer.getChildAt(0);
        if (header != null && header.getChildCount() > 5) {
            header.getChildAt(3).setVisibility(visibilidad);
            header.getChildAt(4).setVisibility(visibilidad);
            header.getChildAt(5).setVisibility(visibilidad);
        }

        ViewGroup fila0 = (ViewGroup) tableContainer.getChildAt(1);
        if (fila0 != null && fila0.getChildCount() > 5) {
            fila0.getChildAt(3).setVisibility(visibilidad);
            fila0.getChildAt(4).setVisibility(visibilidad);
            fila0.getChildAt(5).setVisibility(visibilidad);
        }

        for (int i = 0; i < binding.contenedorFilasTabla.getChildCount(); i++) {
            ViewGroup fila = (ViewGroup) binding.contenedorFilasTabla.getChildAt(i);
            if (fila != null && fila.getChildCount() > 5) {
                fila.getChildAt(3).setVisibility(visibilidad);
                fila.getChildAt(4).setVisibility(visibilidad);
                fila.getChildAt(5).setVisibility(visibilidad);
            }
        }
    }

    private void configurarSpinnerTipoPeriodo() {
        ArrayAdapter<CharSequence> adapterPeriodo = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tipos_periodo,
                android.R.layout.simple_spinner_item
        );
        adapterPeriodo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTipoPeriodo.setAdapter(adapterPeriodo);
    }



    private void calcularAmortizacion() {
        String textoMonto = binding.etMontoFinanciar.getText().toString().trim();
        String textoPagos = binding.etNumeroPagos.getText().toString().trim();
        String textoTasa = binding.etTasaInteres.getText().toString().trim();
        String fecha = binding.etFechaPrimerPago.getText().toString().trim();
        String textoFecha = convertirMesANumero(fecha);

        if (textoMonto.isEmpty() || textoPagos.isEmpty() || textoTasa.isEmpty() || textoFecha.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        parseFechaEnCalendario(textoFecha, fechaPrimerPagoSeleccionada);

        double montoFinanciar;
        int numeroPagos;
        double tasaInteres;

        try {
            montoFinanciar = Double.parseDouble(textoMonto.replaceAll("[^\\d.]", ""));
            numeroPagos = Integer.parseInt(textoPagos);
            tasaInteres = Double.parseDouble(textoTasa);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Valores numéricos inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (montoFinanciar <= 0 || numeroPagos <= 0) {
            Toast.makeText(requireContext(), "Los valores deben ser mayores a cero", Toast.LENGTH_SHORT).show();
            return;
        }

        int periodoSeleccionado = binding.spinnerTipoPeriodo.getSelectedItemPosition();
        double tasaPeriodica = calcularTasaPeriodica(tasaInteres, periodoSeleccionado);
        double pagoFijo = calcularPagoFijo(montoFinanciar, tasaPeriodica, numeroPagos);

        binding.contenedorFilasTabla.removeAllViews();
        binding.tvSaldoInicial.setText(formatoMoneda.format(montoFinanciar));

        double saldo = montoFinanciar;
        double capitalAcumulado = 0;
        double totalIntereses = 0;
        double ultimoPago = 0;
        System.out.println(ultimoPago);
        Calendar calPago = (Calendar) fechaPrimerPagoSeleccionada.clone();
        double todo=0;
        double aPagar=0;
        for (int i = 1; i <= numeroPagos; i++) {
            double interes = saldo * tasaPeriodica;
            double capital = pagoFijo - interes;
            todo += pagoFijo;

            double cortado = Math.floor(pagoFijo * 100.0) / 100.0;

            aPagar += cortado;
            if (i == numeroPagos) {
                capital = saldo; // último pago liquida exactamente el saldo restante
            }


            saldo -= capital;
            if (saldo < 0.001) saldo = 0;

            double montoFila = capital + interes;

            if (saldo == 0){
                montoFila=montoFila+ (todo-aPagar);
            }

            if (capital < 0.001) capital = 0;

            capitalAcumulado += capital;
            totalIntereses += interes;

            agregarFilaTabla(i, formatFecha(calPago), montoFila,
                    capital, interes, capitalAcumulado, saldo, i % 2 == 0);

            avanzarFecha(calPago, periodoSeleccionado);
        }

        System.out.println(ultimoPago);
        System.out.println(aPagar);
        System.out.println(pagoFijo);
        ultimoPago=pagoFijo+(todo-aPagar);

        actualizarResumen(numeroPagos, pagoFijo, ultimoPago);
        actualizarVisibilidadColumnas();
    }


    private double calcularTasaPeriodica(double tasaAnual, int periodo) {
        if (tasaAnual == 0) return 0;
        switch (periodo) {
            case 0: return (tasaAnual / 100.0) / 12.0; // Mensual
            case 1: return (tasaAnual / 100.0) / 6.0;  // Bimestral
            case 2: return (tasaAnual / 100.0) / 4.0;  // Trimestral
            case 3: return (tasaAnual / 100.0) / 3.0;  // Cuatrimestral
            case 4: return (tasaAnual / 100.0) / 2.0;  // Semestral
            case 5: return (tasaAnual / 100.0);         // Anual
            default: return (tasaAnual / 100.0) / 12.0;
        }
    }


    private double calcularPagoFijo(double monto, double tasaPeriodica, int numPagos) {
        if (tasaPeriodica == 0) return monto / numPagos;
        return (monto * tasaPeriodica * Math.pow(1 + tasaPeriodica, numPagos))
                / (Math.pow(1 + tasaPeriodica, numPagos) - 1);
    }

    private void avanzarFecha(Calendar cal, int periodo) {
        switch (periodo) {
            case 0: cal.add(Calendar.MONTH, 1); break;
            case 1: cal.add(Calendar.MONTH, 2); break;
            case 2: cal.add(Calendar.MONTH, 3); break;
            case 3: cal.add(Calendar.MONTH, 4); break;
            case 4: cal.add(Calendar.MONTH, 6); break;
            case 5: cal.add(Calendar.YEAR, 1); break;
        }
    }

    private void agregarFilaTabla(int num, String fecha, double monto, double capital, double interes, double acumulado, double saldo, boolean esPar) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, 10, 0, 10);
        if (esPar) fila.setBackgroundColor(Color.parseColor("#F5F5F5"));

        fila.addView(crearTextViewFila(String.valueOf(num), 0.5f));
        fila.addView(crearTextViewFila(fecha, 1.2f));
        fila.addView(crearTextViewFila(formatoMoneda.format(monto), 1.2f));

        TextView tvCapital = crearTextViewFila(formatoMoneda.format(capital), 1.2f);
        TextView tvInteres = crearTextViewFila(formatoMoneda.format(interes), 1.2f);
        TextView tvAcumulado = crearTextViewFila(formatoMoneda.format(acumulado), 1.2f);

        tvCapital.setVisibility(columnasVisibles ? View.VISIBLE : View.GONE);
        tvInteres.setVisibility(columnasVisibles ? View.VISIBLE : View.GONE);
        tvAcumulado.setVisibility(columnasVisibles ? View.VISIBLE : View.GONE);

        fila.addView(tvCapital);
        fila.addView(tvInteres);
        fila.addView(tvAcumulado);

        fila.addView(crearTextViewFila(formatoMoneda.format(saldo), 1.2f));

        binding.contenedorFilasTabla.addView(fila);
    }

    private TextView crearTextViewFila(String texto, float weight) {
        TextView tv = new TextView(requireContext());
        tv.setText(texto);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(15);
        tv.setTextColor(Color.BLACK);
        return tv;
    }
//checa si el ultimo pago es diferente a los demas entonces hace otra fila que diga 1 pago de tanto
private void actualizarResumen(int pagos, double pagoFijo, double ultimoPago) {
        System.out.println(ultimoPago);
    if (pagos == 0) {
        binding.etResumenPagos.setText("—");
        binding.etResumenMensualidad.setText("");
        binding.filaUltimoPago.setVisibility(View.GONE);
        return;
    }

    boolean ultimoDiferente = Math.abs(ultimoPago - pagoFijo) > 0.01;

    if (ultimoDiferente && pagos > 1) {
        // Ej: "2 pagos de"  +  "$3,333.33"  (en verde)
        binding.etResumenPagos.setText((pagos - 1) + " pagos de");
        binding.etResumenMensualidad.setText(formatoMoneda.format(pagoFijo));

        // Fila oculta se hace visible: "1 pago de"  +  "$3,333.34" (en verde)
        binding.filaUltimoPago.setVisibility(View.VISIBLE);
        binding.tvResumenUltimoPago.setText(formatoMoneda.format(ultimoPago));
    } else {
        // Todos iguales: "20 pagos de"  +  "$3,000.00" (en verde)
        binding.etResumenPagos.setText(pagos + " pagos de");
        binding.etResumenMensualidad.setText(formatoMoneda.format(pagoFijo));

        // Ocultar la fila del último pago
        binding.filaUltimoPago.setVisibility(View.GONE);
    }
}

    private void limpiarTodo() {
        binding.etMontoFinanciar.setText("");
        binding.etNumeroPagos.setText("");
        binding.etTasaInteres.setText("");
        binding.etFechaPrimerPago.setText("");
        binding.spinnerTipoPeriodo.setSelection(0);
        binding.contenedorFilasTabla.removeAllViews();
        binding.tvSaldoInicial.setText("$0.00");
        actualizarResumen(0, 0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        actualizarPrimerPagoSiCambioEnganche();
    }

    private void actualizarPrimerPagoSiCambioEnganche() {
        ContratoModelo contrato = viewModel.getContratoValue();
        if (contrato == null) return;

        String ultimaFechaEngancheFormateada = contrato.getUltimaFechaEnganche(); //llega la fecha así 02/may/2025
        String ultimaFechaEnganche = convertirMesANumero(ultimaFechaEngancheFormateada); //regresamos la fecha a numero
        if (ultimaFechaEnganche == null || ultimaFechaEnganche.isEmpty()) return;

        Calendar cal = Calendar.getInstance();
        parseFechaEnCalendario(ultimaFechaEnganche, cal);

        // +1 mes
        cal.add(Calendar.MONTH, 1);


        // si pasa de día 20, dejar en 20
        if (cal.get(Calendar.DAY_OF_MONTH) > 20) {
            cal.set(Calendar.DAY_OF_MONTH, 20);
        }

        String nuevaFecha = formatFecha(cal);

        // solo actualizar si cambió
        String fechaActual = binding.etFechaPrimerPago.getText().toString();

        if (!nuevaFecha.equals(fechaActual)) {
            String formateada = convertirMesANombreString(nuevaFecha);
            binding.etFechaPrimerPago.setText(formateada);
        }
    }
    private void setupComasMontos(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            private boolean actualizando;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (actualizando) return;

                actualizando = true;

                String texto = s.toString();

                // quita comas
                String limpio = texto.replace(",", "");

                try {
                    if (!limpio.isEmpty()) {

                        // permite decimales
                        double numero = Double.parseDouble(limpio);

                        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                        formatter.setMaximumFractionDigits(2);
                        formatter.setMinimumFractionDigits(0);

                        String formateado = formatter.format(numero);

                        editText.setText(formateado);
                        editText.setSelection(formateado.length());
                    }
                } catch (NumberFormatException e) {
                    // ignora inputs invalidas
                }

                actualizando = false;
            }
        });
    }
}
