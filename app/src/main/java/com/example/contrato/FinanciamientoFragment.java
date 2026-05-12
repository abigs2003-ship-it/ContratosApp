package com.example.contrato;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private SharedContractViewModel viewModel;
    private final Calendar fechaPrimerPagoSeleccionada = Calendar.getInstance();
    
    // Internal values for logic and persistence (Spanish)
    private static final String[] TIPOS_PERIODO_VALORES = {
            "Mensual", "Bimestral", "Trimestral", "Cuatrimestral", "Semestral", "Anual"
    };

    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    private boolean columnasVisibles = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanciamientoBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configurarSpinnerTipoPeriodo();
        configurarDatePicker();
        loadExistingData();
        setupObservers();

        binding.btnCalcular.setOnClickListener(v -> calcularAmortizacion());
        binding.btnLimpiar.setOnClickListener(v -> limpiarTodo());
        
        binding.mostrar.setOnClickListener(v -> {
            columnasVisibles = !columnasVisibles;
            actualizarVisibilidadColumnas();
        });

        binding.btnEnviar.setOnClickListener(v -> mostrarConfirmacionEnvio());
        
        actualizarVisibilidadColumnas();
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
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            if (contract.getMontoFinanciar() != null) binding.etMontoFinanciar.setText(contract.getMontoFinanciar());
            if (contract.getNumPagos() != null) binding.etNumeroPagos.setText(contract.getNumPagos());
            if (contract.getTasaInteres() != null) binding.etTasaInteres.setText(contract.getTasaInteres());
            if (contract.getFechaPrimerPago() != null) binding.etFechaPrimerPago.setText(contract.getFechaPrimerPago());
            
            if (contract.getTipoPeriodo() != null) {
                for (int i = 0; i < TIPOS_PERIODO_VALORES.length; i++) {
                    if (TIPOS_PERIODO_VALORES[i].equals(contract.getTipoPeriodo())) {
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
        saveDataToViewModel();
    }

    private void saveDataToViewModel() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();

        int pos = binding.spinnerTipoPeriodo.getSelectedItemPosition();
        if (pos >= 0 && pos < TIPOS_PERIODO_VALORES.length) {
            contract.setTipoPeriodo(TIPOS_PERIODO_VALORES[pos]);
        }
        
        contract.setFechaPrimerPago(binding.etFechaPrimerPago.getText().toString());
        contract.setNumPagos(binding.etNumeroPagos.getText().toString());
        contract.setTasaInteres(binding.etTasaInteres.getText().toString());
        contract.setMontoFinanciar(binding.etMontoFinanciar.getText().toString());

        viewModel.setContract(contract);
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
        saveDataToViewModel();
        ContratoModelo contract = viewModel.getContractValue();

        // Prepare metadata for local history as well
        if (contract.getId() == null) {
            contract.setId(String.valueOf(System.currentTimeMillis()));
        }
        
        SimpleDateFormat metaSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String now = metaSdf.format(new Date());
        if (contract.getCreationDate() == null) {
            contract.setCreationDate(now);
        }
        contract.setModifiedDate(now);

        if (!contract.getTitulares().isEmpty()) {
            ContratoModelo.Person p = contract.getTitulares().get(0);
            contract.setClientName(p.nombre + " " + (p.paterno != null ? p.paterno : ""));
        } else {
            contract.setClientName("Sin nombre");
        }

        // Save to local history
        ContratoManager.getInstance().updateContract(contract);
        
        // Save to SQL Database via ViewModel
        viewModel.saveToDatabase();
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
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            fechaPrimerPagoSeleccionada.set(Calendar.YEAR, year);
            fechaPrimerPagoSeleccionada.set(Calendar.MONTH, month);
            fechaPrimerPagoSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etFechaPrimerPago.setText(sdf.format(fechaPrimerPagoSeleccionada.getTime()));
        };

        binding.etFechaPrimerPago.setOnClickListener(v -> new DatePickerDialog(requireContext(), dateSetListener,
                fechaPrimerPagoSeleccionada.get(Calendar.YEAR),
                fechaPrimerPagoSeleccionada.get(Calendar.MONTH),
                fechaPrimerPagoSeleccionada.get(Calendar.DAY_OF_MONTH)).show());
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

            agregarFilaTabla(i, sdf.format(calPago.getTime()), montoFila,
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

    private void agregarFilaTabla(int numero, String fecha, double monto, double capital,
                                  double interes, double capitalAcumulado, double saldo, boolean paridad) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpAPx(36)));
        fila.setBackgroundColor(paridad ? Color.parseColor("#F2F2F2") : Color.WHITE);
        fila.setGravity(Gravity.CENTER_VERTICAL);

        agregarCeldaFila(fila, String.valueOf(numero), 44, 0);
        agregarCeldaFila(fila, fecha, 100, 0);
        agregarCeldaFila(fila, formatoMoneda.format(monto), 100, 0);
        
        agregarCeldaFila(fila, formatoMoneda.format(capital), 100, 0);
        agregarCeldaFila(fila, formatoMoneda.format(interes), 100, 0);
        agregarCeldaFila(fila, formatoMoneda.format(capitalAcumulado), 120, 0);
        
        agregarCeldaFila(fila, formatoMoneda.format(saldo), 100, 0);

        binding.contenedorFilasTabla.addView(fila);
    }

    private void agregarCeldaFila(LinearLayout fila, String texto, int widthDp, float peso) {
        TextView celda = new TextView(requireContext());
        int widthPx = widthDp > 0 ? dpAPx(widthDp) : 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT, peso);
        celda.setLayoutParams(params);
        celda.setText(texto);
        celda.setTextSize(10f);
        celda.setTextColor(Color.parseColor("#333333"));
        celda.setGravity(Gravity.CENTER);
        celda.setPadding(dpAPx(2), dpAPx(8), dpAPx(2), dpAPx(8));
        fila.addView(celda);
    }

    private void actualizarResumen(int numeroPagos, double pagoFijo, double totalPagar, double totalIntereses, double ultimoPago) {
        if (numeroPagos > 1) {
            binding.tvResumenPagos1Cantidad.setText(String.format(Locale.getDefault(), "%d pagos de", numeroPagos - 1));
            binding.tvResumenPagos1Monto.setText(formatoMoneda.format(pagoFijo));
            binding.tvResumenPagos2Cantidad.setText("1 pago de");
            binding.tvResumenPagos2Monto.setText(formatoMoneda.format(ultimoPago));
        } else {
            binding.tvResumenPagos1Cantidad.setText("1 pago de");
            binding.tvResumenPagos1Monto.setText(formatoMoneda.format(ultimoPago));
            binding.tvResumenPagos2Cantidad.setText("");
            binding.tvResumenPagos2Monto.setText("");
        }
    }

    private void limpiarTodo() {
        binding.etMontoFinanciar.setText("");
        binding.etNumeroPagos.setText("");
        binding.etTasaInteres.setText("");
        binding.etFechaPrimerPago.setText("");
        binding.tvResumenPagos1Cantidad.setText("— pagos de");
        binding.tvResumenPagos1Monto.setText("$0.00");
        binding.tvResumenPagos2Cantidad.setText("");
        binding.tvResumenPagos2Monto.setText("");
        binding.tvSaldoInicial.setText("$0.00");
        binding.contenedorFilasTabla.removeAllViews();
        binding.spinnerTipoPeriodo.setSelection(0);
        
        columnasVisibles = false;
        actualizarVisibilidadColumnas();
    }

    private int dpAPx(int dp) {
        float densidad = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * densidad);
    }

    @Override
    public void onDestroyView() {
        saveDataToViewModel();
        super.onDestroyView();
        binding = null;
    }
}
