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
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

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
        configurarDatePicker();
        loadExistingData();
        setupObservers();
        
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

        binding.btnEnviar.setOnClickListener(v -> mostrarConfirmacionEnvio());
        
        actualizarVisibilidadColumnas();
    }

    private String getCurrentLanguage() {
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentLocales.isEmpty()) {
            return currentLocales.get(0).getLanguage();
        }
        return Locale.getDefault().getLanguage();
    }

    private String getMonthAbbr(int month) {
        if (month < 1 || month > 12) return "";
        String lang = getCurrentLanguage();
        return lang.equals("en") ? MESES_EN[month - 1] : MESES_ES[month - 1];
    }

    private String formatFecha(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        String lang = getCurrentLanguage();
        
        String dayStr = String.format(Locale.getDefault(), "%02d", day);
        String monthAbbr = getMonthAbbr(month);
        
        if (lang.equals("en")) {
            return monthAbbr + "/" + dayStr + "/" + year;
        } else {
            return dayStr + "/" + monthAbbr + "/" + year;
        }
    }

    private void parseFechaIntoCalendar(String dateStr, Calendar cal) {
        if (dateStr == null || dateStr.isEmpty()) return;
        String lang = getCurrentLanguage();
        String[] parts = dateStr.split("/");
        if (parts.length != 3) return;
        
        try {
            int day, month, year;
            if (lang.equals("en")) {
                month = parseMonthPart(parts[0], lang);
                day = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            } else {
                day = Integer.parseInt(parts[0]);
                month = parseMonthPart(parts[1], lang);
                year = Integer.parseInt(parts[2]);
            }
            if (month != -1) {
                cal.set(year, month - 1, day);
            }
        } catch (Exception ignored) {}
    }

    private int parseMonthPart(String monthPart, String lang) {
        try {
            return Integer.parseInt(monthPart);
        } catch (NumberFormatException e) {
            String[] meses = lang.equals("en") ? MESES_EN : MESES_ES;
            for (int i = 0; i < meses.length; i++) {
                if (meses[i].equalsIgnoreCase(monthPart)) return i + 1;
            }
        }
        return -1;
    }

    private void muestraDatePicker(EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    fechaPrimerPagoSeleccionada.set(year, monthOfYear, dayOfMonth);
                    editText.setText(formatFecha(fechaPrimerPagoSeleccionada));
                }, 
                fechaPrimerPagoSeleccionada.get(Calendar.YEAR),
                fechaPrimerPagoSeleccionada.get(Calendar.MONTH),
                fechaPrimerPagoSeleccionada.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupFormatoFecha(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String prev = "";
            private boolean isInternalUpdate = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isInternalUpdate) return;
                prev = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isInternalUpdate) return;
                String str = s.toString();
                if (str.length() < prev.length()) return;

                isInternalUpdate = true;
                String lang = getCurrentLanguage();
                
                if (lang.equals("en")) {
                    if (str.length() == 2) {
                        try {
                            int m = Integer.parseInt(str);
                            if (m >= 1 && m <= 12) {
                                String abbr = getMonthAbbr(m);
                                s.replace(0, 2, abbr);
                                s.append("/");
                            } else {
                                s.append("/");
                            }
                        } catch (Exception e) {
                            s.append("/");
                        }
                    } else if (str.length() == 6) {
                        s.append("/");
                    }
                } else {
                    if (str.length() == 2) {
                        s.append("/");
                    } else if (str.length() == 5) {
                        try {
                            String monthStr = str.substring(3, 5);
                            int m = Integer.parseInt(monthStr);
                            if (m >= 1 && m <= 12) {
                                String abbr = getMonthAbbr(m);
                                s.replace(3, 5, abbr);
                                s.append("/");
                            } else {
                                s.append("/");
                            }
                        } catch (Exception e) {
                            s.append("/");
                        }
                    } else if (str.length() == 7 && str.substring(3, 6).matches("[a-zA-Z]{3}")) {
                        // Correct format after month abbreviation
                    }
                }
                isInternalUpdate = false;
            }
        });
    }

    private void setupObservers() {
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Contrato guardado exitosamente en la base de datos", Toast.LENGTH_LONG).show();
                irAlMenu();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadExistingData() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato != null) {
            if (Contrato.getMontoFinanciar() != null) binding.etMontoFinanciar.setText(Contrato.getMontoFinanciar());
            if (Contrato.getNumPagos() != null) binding.etNumeroPagos.setText(Contrato.getNumPagos());
            if (Contrato.getTasaInteres() != null) binding.etTasaInteres.setText(Contrato.getTasaInteres());
            
            if (Contrato.getFechaPrimerPago() != null) {
                parseFechaIntoCalendar(Contrato.getFechaPrimerPago(), fechaPrimerPagoSeleccionada);
                binding.etFechaPrimerPago.setText(formatFecha(fechaPrimerPagoSeleccionada));
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
        if (pos >= 0 && pos < TIPOS_PERIODO_VALORES.length) {
            Contrato.setTipoPeriodo(TIPOS_PERIODO_VALORES[pos]);
        }
        
        Contrato.setFechaPrimerPago(binding.etFechaPrimerPago.getText().toString());
        Contrato.setNumPagos(binding.etNumeroPagos.getText().toString());
        Contrato.setTasaInteres(binding.etTasaInteres.getText().toString());
        Contrato.setMontoFinanciar(binding.etMontoFinanciar.getText().toString());

        viewModel.setContrato(Contrato);
    }

    private void mostrarConfirmacionEnvio() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Envío")
                .setMessage("¿Estás seguro de que deseas enviar el contrato?")
                .setPositiveButton("Enviar", (dialog, which) -> finalizarContrato())
                .setNegativeButton("Regresar", null)
                .show();
    }

    private void finalizarContrato() {
        guardaDatosViewModel();
        ContratoModelo Contrato = viewModel.getContratoValue();

        if (Contrato.getId() == null) {
            Contrato.setId(String.valueOf(System.currentTimeMillis()));
        }
        
        SimpleDateFormat metaSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String now = metaSdf.format(new Date());
        if (Contrato.getCreationDate() == null) {
            Contrato.setCreationDate(now);
        }
        Contrato.setModifiedDate(now);

        if (!Contrato.getTitulares().isEmpty()) {
            ContratoModelo.Persona p = Contrato.getTitulares().get(0);
            Contrato.setClientName(p.nombre + " " + (p.paterno != null ? p.paterno : ""));
        } else {
            Contrato.setClientName("Sin nombre");
        }

        ContratoManager.getInstance().actualizaContrato(Contrato);
        
        viewModel.guardaBaseDatos();
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

    private void configurarDatePicker() {
        binding.etFechaPrimerPago.setOnClickListener(v -> muestraDatePicker(binding.etFechaPrimerPago));
    }

    private void calcularAmortizacion() {
        String textoMonto = binding.etMontoFinanciar.getText().toString().trim();
        String textoPagos = binding.etNumeroPagos.getText().toString().trim();
        String textoTasa = binding.etTasaInteres.getText().toString().trim();
        String textoFecha = binding.etFechaPrimerPago.getText().toString().trim();

        if (textoMonto.isEmpty() || textoPagos.isEmpty() || textoTasa.isEmpty() || textoFecha.isEmpty()) {
            Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        parseFechaIntoCalendar(textoFecha, fechaPrimerPagoSeleccionada);

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
        Calendar calPago = (Calendar) fechaPrimerPagoSeleccionada.clone();

        for (int i = 1; i <= numeroPagos; i++) {
            double interes = saldo * tasaPeriodica;
            double capital = pagoFijo - interes;

            if (i == numeroPagos) {
                capital = saldo;
            }

            double montoFila = capital + interes;
            if (i == numeroPagos) {
                ultimoPago = montoFila;
            }

            saldo -= capital;
            if (saldo < 0.001) saldo = 0;
            
            capitalAcumulado += capital;
            totalIntereses += interes;

            agregarFilaTabla(i, formatFecha(calPago), montoFila,
                    capital, interes, capitalAcumulado, saldo, i % 2 == 0);

            avanzarFecha(calPago, periodoSeleccionado);
        }

        actualizarResumen(numeroPagos, pagoFijo, capitalAcumulado + totalIntereses, totalIntereses, ultimoPago);
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
        tv.setTextSize(12);
        tv.setTextColor(Color.BLACK);
        return tv;
    }

    private void actualizarResumen(int pagos, double mensualidad, double total, double intereses, double ultimo) {
        binding.etResumenPagos.setText(String.valueOf(pagos));
        binding.etResumenMensualidad.setText(formatoMoneda.format(mensualidad));
        binding.tvResumenTotal.setText(formatoMoneda.format(total));
        binding.tvResumenIntereses.setText(formatoMoneda.format(intereses));
        binding.tvResumenUltimoPago.setText(formatoMoneda.format(ultimo));
    }

    private void limpiarTodo() {
        binding.etMontoFinanciar.setText("");
        binding.etNumeroPagos.setText("");
        binding.etTasaInteres.setText("");
        binding.etFechaPrimerPago.setText("");
        binding.spinnerTipoPeriodo.setSelection(0);
        binding.contenedorFilasTabla.removeAllViews();
        binding.tvSaldoInicial.setText("$0.00");
        actualizarResumen(0, 0, 0, 0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
