package com.example.contrato;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.databinding.DialogDescuentosBinding;
import com.example.contrato.databinding.FragmentDatosVentaBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatosVentaFragment extends Fragment {
    private FragmentDatosVentaBinding binding;
    private List<DescuentoItem> listaDescuentos = new ArrayList<>();
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDatosVentaBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);
        seleccionaBoton(binding.btnNueva);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUnidadSpinner();
        setupTemporadaSpinner();
        setupAnioUsoSpinner();
        setupMonedaSpinner();
        setupCalculosPrecios();
        setupDynamicContratos();
        setupEngancheCalculos();
        setupEngancheSalaCalculos();
        setupDynamicDescuentos();
        setupDynamicPagosDiferidos();
        setupTotalesCalculos();
        LogicaTipodePago();
        setupBotones();
        setupCurrencyPrefixes();

        loadExistingData();

        binding.btnLimpiarInventario.setOnClickListener(v -> limpiarInventario());
        binding.btnLimpiarDatosdeVenta.setOnClickListener(v -> limpiarDatosVenta());

        binding.AceptarTarea.setOnClickListener(v -> {
            if (validarCampos()) {
                guardaDatosViewModel();
                Navigation.findNavController(v).navigate(R.id.nav_regalos);
            }
        });
    }

    private void setupUnidadSpinner() {
        viewModel.getUnidades().observe(getViewLifecycleOwner(), unidades -> {
            if (unidades != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, unidades);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spUnidad.setAdapter(adapter);
                
                ContratoModelo Contrato = viewModel.getContratoValue();
                if (Contrato != null && Contrato.getUnidad() != null) {
                    setSpinnerValue(binding.spUnidad, Contrato.getUnidad());
                }
            }
        });
        viewModel.fetchUnidades();
    }

    private void setupTemporadaSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.temporadas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTemporada.setAdapter(adapter);
    }

    private void loadExistingData() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) return;

        if ("Upgrade".equals(Contrato.getTipoVenta())) {
            seleccionaBoton(binding.btnUpgrade);
        } else {
            seleccionaBoton(binding.btnNueva);
        }
        actualizarVisibilidadMontoCuenta();

        setSpinnerValue(binding.spUnidad, Contrato.getUnidad());
        setSpinnerValue(binding.spTemporada, Contrato.getTemporada());
        setSpinnerValue(binding.spAnioUso, Contrato.getAnioUso());
        binding.editNoAnios.setText(Contrato.getNoAnios());

        setSpinnerValue(binding.spMoneda, Contrato.getMoneda());
        binding.editTipoCambio.setText(Contrato.getTipoCambio());
        binding.editPrecioBruto.setText(Contrato.getPrecioBruto());
        binding.editMontoCuenta.setText(Contrato.getMontoCuenta());
        binding.editPrecioNeto.setText(Contrato.getPrecioNeto());

        if ("Financiado".equals(Contrato.getTipoPago())) {
            binding.rbFinanciado.setChecked(true);
        } else if ("Contado".equals(Contrato.getTipoPago())) {
            binding.rbContado.setChecked(true);
        }

        binding.editEnganchePorcentaje.setText(Contrato.getEnganchePorcentaje());
        binding.editEngancheMonto.setText(Contrato.getEngancheMonto());
        binding.editEngancheSalaMonto.setText(Contrato.getEngancheSalaMonto());
        binding.editEngancheSalaPorcentaje.setText(Contrato.getEngancheSalaPorcentaje());
        binding.editVarios.setText(Contrato.getVariosMonto());
        binding.editNoDesc.setText(Contrato.getNoDesc());
        binding.editEngDiferido.setText(Contrato.getEngDiferidoMonto());
        binding.editNoPagosEng.setText(Contrato.getNoPagosEng());
        binding.editSaldoEng.setText(Contrato.getSaldoEnganche());
        binding.editMontoFinanciar.setText(Contrato.getMontoFinanciar());
        binding.editCostoContrato.setText(Contrato.getCostoContrato());
        binding.editpagosala.setText(Contrato.getPagoSala());
        binding.editcostomembresia.setText(Contrato.getCostoMembresia());

        // Restore dynamic Contratos
        if (Contrato.getContratosMontoCuenta() != null) {
            binding.editNoContratosVenta.setText(String.valueOf(Contrato.getContratosMontoCuenta().size()));
            binding.containerContratosDinamicos.removeAllViews();
            for (String xref : Contrato.getContratosMontoCuenta()) {
                EditText et = createContratoEditText();
                et.setText(xref);
                binding.containerContratosDinamicos.addView(et);
            }
        }

        // Restore Discounts
        if (Contrato.getDescuentosDetalle() != null) {
            listaDescuentos.clear();
            for (int i = 0; i < Contrato.getDescuentosDetalle().size(); i++) {
                ContratoModelo.DescuentoDetalle dd = Contrato.getDescuentosDetalle().get(i);
                listaDescuentos.add(new DescuentoItem(i + 1, dd.descripcion, "REM-" + (i + 1), parseDouble(dd.monto), parseDouble(dd.monto), "REF-" + (i + 1)));
            }
        }

        // Restore Deferred Payments
        if (Contrato.getPagosDiferidos() != null && !Contrato.getPagosDiferidos().isEmpty()) {
            binding.containerPagosDinamicos.removeAllViews();
            for (ContratoModelo.PagoDiferido pd : Contrato.getPagosDiferidos()) {
                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                EditText etMonto = createSmallValueEditText("Monto");
                etMonto.setText(pd.monto);
                agregaPrefijoMoneda(etMonto);

                EditText etFecha = createSmallValueEditText("Fecha");
                etFecha.setFocusable(false);
                etFecha.setClickable(true);
                etFecha.setText(pd.fecha);
                etFecha.setOnClickListener(v -> showDatePicker(etFecha));

                row.addView(etMonto);
                row.addView(etFecha);
                binding.containerPagosDinamicos.addView(row);
            }
        }
    }

    private void setSpinnerValue(android.widget.Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int pos = adapter.getPosition(value);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        guardaDatosViewModel();
    }

    private void setupCurrencyPrefixes() {
        agregaPrefijoMoneda(binding.editPrecioBruto);
        agregaPrefijoMoneda(binding.editMontoCuenta);
        agregaPrefijoMoneda(binding.editEngancheMonto);
        agregaPrefijoMoneda(binding.editEngancheSalaMonto);
        agregaPrefijoMoneda(binding.editVarios);
        agregaPrefijoMoneda(binding.editEngDiferido);
        agregaPrefijoMoneda(binding.editCostoContrato);
        agregaPrefijoMoneda(binding.editcostomembresia);
        
        if (binding.editPrecioNeto.getText().toString().isEmpty()) binding.editPrecioNeto.setText("$0.00");
        if (binding.editSaldoEng.getText().toString().isEmpty()) binding.editSaldoEng.setText("$0.00");
        if (binding.editMontoFinanciar.getText().toString().isEmpty()) binding.editMontoFinanciar.setText("$0.00");
        if (binding.editpagosala.getText().toString().isEmpty()) binding.editpagosala.setText("$0.00");
    }

    private void agregaPrefijoMoneda(EditText et) {
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setKeyListener(DigitsKeyListener.getInstance("0123456789.$"));
        et.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    et.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[^\\d.]", "");
                    String formatted = "";
                    if (!cleanString.isEmpty()) {
                        formatted = "$" + cleanString;
                    }
                    current = formatted;
                    et.setText(formatted);
                    et.setSelection(formatted.length());
                    et.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validarCampos() {
        if (binding.editNoAnios.getText().toString().isEmpty() ||
            binding.editTipoCambio.getText().toString().isEmpty() ||
            binding.editPrecioBruto.getText().toString().isEmpty() ||
            binding.editEnganchePorcentaje.getText().toString().isEmpty() ||
            binding.editEngancheSalaPorcentaje.getText().toString().isEmpty() ||
            binding.editCostoContrato.getText().toString().isEmpty() ||
            binding.editcostomembresia.getText().toString().isEmpty()) {
            
            Toast.makeText(requireContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.btnUpgrade.isChecked() && binding.editMontoCuenta.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese el monto de la cuenta para Upgrade", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i++) {
            View row = binding.containerPagosDinamicos.getChildAt(i);
            if (row instanceof LinearLayout) {
                View dateField = ((LinearLayout) row).getChildAt(1);
                if (dateField instanceof EditText && ((EditText) dateField).getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor, seleccione las fechas para todos los pagos diferidos", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }


    private void setupBotones() {
        binding.btnNueva.setOnClickListener(v -> {
            seleccionaBoton(binding.btnNueva);
            actualizarVisibilidadMontoCuenta();
        });

        binding.btnUpgrade.setOnClickListener(v -> {
            seleccionaBoton(binding.btnUpgrade);
            actualizarVisibilidadMontoCuenta();
        });
        
    }

    private void seleccionaBoton(MaterialButton seleccionado) {
        resetEstiloBoton(binding.btnNueva);
        resetEstiloBoton(binding.btnUpgrade);

        seleccionado.setChecked(true);
        seleccionado.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0A0E21")));
        seleccionado.setTextColor(Color.WHITE);
        seleccionado.setStrokeWidth(0);
    }

    private void resetEstiloBoton(MaterialButton boton) {
        boton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        boton.setTextColor(Color.parseColor("#1E293B"));
        boton.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        boton.setStrokeWidth(1);
        boton.setChecked(false);
    }

    private void actualizarVisibilidadMontoCuenta() {
        if (binding.btnUpgrade.isChecked()) {
            binding.layoutMontoCuenta.setVisibility(View.VISIBLE);
        } else {
            binding.layoutMontoCuenta.setVisibility(View.GONE);
        }
    }



    private void showDescuentosDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogDescuentosBinding dialogBinding = DialogDescuentosBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        DescuentoAdapter adapter = new DescuentoAdapter(new ArrayList<>(listaDescuentos));
        dialogBinding.rvDescuentos.setLayoutManager(new LinearLayoutManager(getContext()));
        dialogBinding.rvDescuentos.setAdapter(adapter);

        updateDialogTotal(dialogBinding, adapter.getItems());

        dialogBinding.btnAgregar.setOnClickListener(v -> {
            String desc = dialogBinding.editDescModal.getText().toString();
            String montoStr = dialogBinding.editMontoModal.getText().toString();
            double monto = parseDouble(montoStr);
            if (!desc.isEmpty() && monto > 0) {
                int nextNo = adapter.getItemCount() + 1;
                adapter.addItem(new DescuentoItem(nextNo, desc, "REM-" + nextNo, monto, monto, "REF-" + nextNo));
                updateDialogTotal(dialogBinding, adapter.getItems());
                dialogBinding.editDescModal.setText("");
                dialogBinding.editMontoModal.setText("");
            }
        });

        dialogBinding.btnQuitar.setOnClickListener(v -> {
            adapter.removeLast();
            updateDialogTotal(dialogBinding, adapter.getItems());
        });

        dialogBinding.btnQuitarTodos.setOnClickListener(v -> {
            adapter.clear();
            updateDialogTotal(dialogBinding, adapter.getItems());
        });

        dialogBinding.btnLimpiar.setOnClickListener(v -> {
            dialogBinding.editDescModal.setText("");
            dialogBinding.editMontoModal.setText("");
        });

        dialogBinding.btnRegresar.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnAceptar.setOnClickListener(v -> {
            listaDescuentos = adapter.getItems();
            double total = 0;
            for (DescuentoItem item : listaDescuentos) total += item.montoDesc;
            binding.editVarios.setText(String.format(Locale.US, "$%.2f", total));
            binding.editNoDesc.setText(String.valueOf(listaDescuentos.size()));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateDialogTotal(DialogDescuentosBinding db, List<DescuentoItem> items) {
        double total = 0;
        for (DescuentoItem item : items) total += item.montoDesc;
        db.tvTotalDescuentos.setText(String.format(Locale.US, "Total: $%.2f", total));
    }

    private void setupAnioUsoSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, years);
        binding.spAnioUso.setAdapter(adapter);
    }

    private void setupMonedaSpinner() {
        String[] tipoMonedas = {"MXN", "USD", "CAD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipoMonedas);
        binding.spMoneda.setAdapter(adapter);

        binding.spMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizaMoneda(tipoMonedas[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizaMoneda(String currency) {
        String label = currency.equals("MXN") ? "MN" : currency;
        binding.tvMonedaBruto.setText(label);
        binding.tvMonedaCuenta.setText(label);
        binding.tvMonedaNeto.setText(label);
        binding.tvMonedaEnganche.setText(label);
        binding.tvMonedaEngancheSala.setText(label);
        binding.tvMonedaVarios.setText(label);
        binding.tvMonedaEngDiferido.setText(label);
        binding.tvMonedaSaldoEng.setText(label);
        binding.tvMonedaMontoFinanciar.setText(label);
        binding.tvMonedaCostoContrato.setText(label);
        binding.tvMonedaPagoSala.setText(label);
        binding.tvMonedaCostoMembresia.setText(label);
    }

    private void setupCalculosPrecios() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculaPrecioNeto();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editPrecioBruto.addTextChangedListener(watcher);
        binding.editMontoCuenta.addTextChangedListener(watcher);
    }

    private void calculaPrecioNeto() {
        try {
            double bruto = parseDouble(binding.editPrecioBruto.getText().toString());
            double cuenta = parseDouble(binding.editMontoCuenta.getText().toString());
            double neto = bruto - cuenta;
            binding.editPrecioNeto.setText(String.format(Locale.US, "$%.2f", neto));
            binding.editcostomembresia.setText(String.format(Locale.US, "$%.2f", neto));

            if (binding.rbContado.isChecked()) {
                binding.editEnganchePorcentaje.setText("100");
                updateEngancheMN();
            } else {
                updateEngancheMN();
                updateEngancheSalaMN();
            }
            calculateMontoFinanciar();
            calculateEngancheDiferido();
            calculateTotalPagoSala();
        } catch (Exception e) {
            binding.editPrecioNeto.setText("$0.00");
        }
    }

    private void setupDynamicContratos() {
        binding.editNoContratosVenta.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                String val = s.toString().trim();
                if (val.length() > 0) {
                    try {
                        int num = Integer.parseInt(val);
                        if (num > 7) {
                            isUpdating = true;
                            num = 7;
                            binding.editNoContratosVenta.setText("7");
                            binding.editNoContratosVenta.setSelection(1);
                            isUpdating = false;
                        }
                        if (num >= 0) {
                            binding.containerContratosDinamicos.removeAllViews();
                            for (int i = 0; i < num; i++) {
                                binding.containerContratosDinamicos.addView(createContratoEditText());
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                } else {
                    binding.containerContratosDinamicos.removeAllViews();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private EditText createContratoEditText() {
        EditText et = new EditText(requireContext());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.setMargins(0, 8, 0, 0);
        et.setLayoutParams(lp);
        et.setBackgroundResource(R.drawable.bg_input);
        et.setPadding(16, 0, 16, 0);
        et.setHint("No. Contrato");
        et.setTextSize(14);
        et.setSingleLine(true);
        et.setImeOptions(1);
        et.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(15)});
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        return et;
    }

    private void setupEngancheCalculos() {
        binding.editEnganchePorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEnganchePorcentaje.hasFocus()) {
                    updateEngancheMN();
                    calculateEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculateMontoFinanciar();
            }
        });

        binding.editEngancheMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheMonto.hasFocus()) {
                    updateEnganchePercent();
                    calculateEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculateMontoFinanciar();
            }
        });
    }

    private void setupEngancheSalaCalculos() {
        binding.editEngancheSalaPorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaPorcentaje.hasFocus()) {
                    updateEngancheSalaMN();
                    calculateEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculateTotalPagoSala();
            }
        });

        binding.editEngancheSalaMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaMonto.hasFocus()) {
                    updateEngancheSalaPercent();
                    calculateEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculateTotalPagoSala();
            }
        });
    }

    private void updateEngancheMN() {
        double neto = parseDouble(binding.editPrecioNeto.getText().toString());
        double porc = parseDouble(binding.editEnganchePorcentaje.getText().toString());
        double montoEnganche = neto * (porc / 100.0);
        binding.editEngancheMonto.setText(String.format(Locale.US, "$%.2f", montoEnganche));
    }

    private void calculateTotalPagoSala() {
        double engancheSala = parseDouble(binding.editEngancheSalaMonto.getText().toString());
        double varios = parseDouble(binding.editVarios.getText().toString());
        double costoContrato = parseDouble(binding.editCostoContrato.getText().toString());
        double totalSala = engancheSala + varios + costoContrato;
        binding.editpagosala.setText(String.format(Locale.US, "$%.2f", totalSala));
    }

    private void updateEnganchePercent() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double amount = parseDouble(binding.editEngancheMonto.getText().toString());
            if (neto > 0) binding.editEnganchePorcentaje.setText(String.format(Locale.US, "%.2f", (amount / neto) * 100));
        } catch (Exception ignored) {}
    }

    private void updateEngancheSalaMN() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double percent = parseDouble(binding.editEngancheSalaPorcentaje.getText().toString());
            binding.editEngancheSalaMonto.setText(String.format(Locale.US, "$%.2f", neto * (percent / 100)));
        } catch (Exception ignored) {}
    }

    private void updateEngancheSalaPercent() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double amount = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            if (neto > 0) binding.editEngancheSalaPorcentaje.setText(String.format(Locale.US, "%.2f", (amount / neto) * 100));
        } catch (Exception ignored) {}
    }

    private void setupDynamicDescuentos() {
        binding.editNoDesc.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                binding.containerDescuentosDinamicos.removeAllViews();
                try {
                    String val = s.toString().trim();
                    if (val.isEmpty()) {
                        updateTotalDiscounts();
                        return;
                    }
                    int num = Integer.parseInt(val);
                    if (num > 7) {
                        isUpdating = true;
                        num = 7;
                        binding.editNoDesc.setText("7");
                        binding.editNoDesc.setSelection(1);
                        isUpdating = false;
                    }
                    for (int i = 0; i < num; i++) {
                        binding.containerDescuentosDinamicos.addView(createDiscountRow(i));
                    }
                    updateTotalDiscounts();
                } catch (Exception ignored) {}
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private View createDiscountRow(int i) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(lp);

        EditText etMonto = new EditText(requireContext());
        LinearLayout.LayoutParams lpMonto = new LinearLayout.LayoutParams(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()), 1f);
        lpMonto.setMargins(0, 0, 4, 0);
        etMonto.setLayoutParams(lpMonto);
        etMonto.setBackgroundResource(R.drawable.bg_input);
        etMonto.setHint("Monto " + (i + 1));
        etMonto.setTextSize(12);
        etMonto.setPadding(8, 0, 8, 0);
        etMonto.setSingleLine(true);
        agregaPrefijoMoneda(etMonto);
        etMonto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateTotalDiscounts(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        EditText etDesc = new EditText(requireContext());
        LinearLayout.LayoutParams lpDesc = new LinearLayout.LayoutParams(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()), 1.3f);
        etDesc.setLayoutParams(lpDesc);
        etDesc.setBackgroundResource(R.drawable.bg_input);
        etDesc.setHint("Descripción " + (i + 1));
        etDesc.setTextSize(12);
        etDesc.setPadding(8, 0, 8, 0);
        etDesc.setSingleLine(true);

        row.addView(etMonto);
        row.addView(etDesc);
        return row;
    }

    private void updateTotalDiscounts() {
        double total = 0;
        for (int i = 0; i < binding.containerDescuentosDinamicos.getChildCount(); i++) {
            View row = binding.containerDescuentosDinamicos.getChildAt(i);
            if (row instanceof LinearLayout) {
                View firstChild = ((LinearLayout) row).getChildAt(0);
                if (firstChild instanceof EditText) {
                    total += parseDouble(((EditText) firstChild).getText().toString());
                }
            }
        }
        binding.editVarios.setText(String.format(Locale.US, "$%.2f", total));
        calculateEngancheDiferido();
    }

    private void setupDynamicPagosDiferidos() {
        TextWatcher watcher = new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                
                if (binding.editNoPagosEng.hasFocus()) {
                    String val = binding.editNoPagosEng.getText().toString().trim();
                    if (!val.isEmpty()) {
                        try {
                            int num = Integer.parseInt(val);
                            if (num > 7) {
                                isUpdating = true;
                                binding.editNoPagosEng.setText("7");
                                binding.editNoPagosEng.setSelection(1);
                                isUpdating = false;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
                updatePagosDiferidos();
                calculateSaldoEnganche();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editEngDiferido.addTextChangedListener(watcher);
        binding.editNoPagosEng.addTextChangedListener(watcher);
    }

    private void updatePagosDiferidos() {
        binding.containerPagosDinamicos.removeAllViews();
        try {
            double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
            String numPagosStr = binding.editNoPagosEng.getText().toString().trim();
            if (numPagosStr.isEmpty()) return;
            int numPagos = Integer.parseInt(numPagosStr);
            if (numPagos <= 0) return;
            double split = totalDiferido / numPagos;
            for (int i = 0; i < numPagos; i++) {
                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                EditText etMonto = createSmallValueEditText("Monto");
                etMonto.setText(String.format(Locale.US, "$%.2f", split));
                agregaPrefijoMoneda(etMonto);

                EditText etFecha = createSmallValueEditText("Fecha");
                etFecha.setFocusable(false);
                etFecha.setClickable(true);
                etFecha.setOnClickListener(v -> showDatePicker(etFecha));

                row.addView(etMonto);
                row.addView(etFecha);
                binding.containerPagosDinamicos.addView(row);
            }
        } catch (Exception ignored) {}
    }

    private void showDatePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(requireContext(), (view, y, m, d) -> {
            et.setText(String.format(Locale.US, "%02d/%02d/%d", d, m + 1, y));
        }, year, month, day);

        dpd.getDatePicker().setMinDate(c.getTimeInMillis());
        dpd.show();
    }

    private EditText createSmallValueEditText(String hint) {
        EditText et = new EditText(requireContext());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.setMargins(0, 4, 8, 4);
        et.setLayoutParams(lp);
        et.setBackgroundResource(R.drawable.bg_input);
        et.setTextSize(14);
        et.setPadding(12, 0, 12, 0);
        if (hint != null) et.setHint(hint);
        et.setSingleLine(true);
        return et;
    }

    private void calculateEngancheDiferido() {
        if (binding == null) return;
        try {
            double totalEnganche = parseDouble(binding.editEngancheMonto.getText().toString());
            double salaEnganche = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            double discounts = parseDouble(binding.editVarios.getText().toString());
            double diferido = totalEnganche - salaEnganche - discounts;
            
            String formatted = String.format(Locale.US, "$%.2f", Math.max(0, diferido));
            if (!binding.editEngDiferido.getText().toString().equals(formatted)) {
                binding.editEngDiferido.setText(formatted);
            }
            calculateSaldoEnganche();
        } catch (Exception ignored) {}
    }

    private void calculateSaldoEnganche() {
        try {
            double engancheMonto = parseDouble(binding.editEngancheMonto.getText().toString());
            double salaMonto = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            double diferidoMonto = parseDouble(binding.editEngDiferido.getText().toString());
            double discounts = parseDouble(binding.editVarios.getText().toString());
            double saldo = engancheMonto - salaMonto - diferidoMonto - discounts;
            binding.editSaldoEng.setText(String.format(Locale.US, "$%.2f", saldo));
        } catch (Exception ignored) {}
    }

    private void calculateMontoFinanciar() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double engancheTotal = parseDouble(binding.editEngancheMonto.getText().toString());
            double financia = neto - engancheTotal;
            binding.editMontoFinanciar.setText(String.format(Locale.US, "$%.2f", Math.max(0, financia)));
        } catch (Exception ignored) {}
    }

    private void setupTotalesCalculos() {
        TextWatcher totalsWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalPagoSala();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCostoContrato.addTextChangedListener(totalsWatcher);
        binding.editcostomembresia.addTextChangedListener(totalsWatcher);
    }

    private void LogicaTipodePago() {
        binding.rgTipoPago.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isContado = (checkedId == R.id.rbContado);
            if (isContado) {
                binding.editEnganchePorcentaje.setText("100");
                updateEngancheMN();
                binding.EngacheColapsable.setVisibility(View.GONE);
            } else {
                binding.EngacheColapsable.setVisibility(View.VISIBLE);
            }
            calculateMontoFinanciar();
            calculateEngancheDiferido();
            calculateTotalPagoSala();
        });
    }

    private void guardaDatosViewModel() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();

        Contrato.setTipoVenta(binding.btnNueva.isChecked() ? "Nueva" : "Upgrade");
        if (binding.spUnidad.getSelectedItem() != null) Contrato.setUnidad(binding.spUnidad.getSelectedItem().toString());
        if (binding.spTemporada.getSelectedItem() != null) Contrato.setTemporada(binding.spTemporada.getSelectedItem().toString());
        if (binding.spAnioUso.getSelectedItem() != null) Contrato.setAnioUso(binding.spAnioUso.getSelectedItem().toString());
        Contrato.setNoAnios(binding.editNoAnios.getText().toString());

        if (binding.spMoneda.getSelectedItem() != null) Contrato.setMoneda(binding.spMoneda.getSelectedItem().toString());
        Contrato.setTipoCambio(binding.editTipoCambio.getText().toString());
        Contrato.setPrecioBruto(binding.editPrecioBruto.getText().toString());
        Contrato.setMontoCuenta(binding.editMontoCuenta.getText().toString());
        Contrato.setPrecioNeto(binding.editPrecioNeto.getText().toString());
        Contrato.setTipoPago(binding.rbFinanciado.isChecked() ? "Financiado" : "Contado");

        Contrato.setEnganchePorcentaje(binding.editEnganchePorcentaje.getText().toString());
        Contrato.setEngancheMonto(binding.editEngancheMonto.getText().toString());
        Contrato.setEngancheSalaMonto(binding.editEngancheSalaMonto.getText().toString());
        Contrato.setEngancheSalaPorcentaje(binding.editEngancheSalaPorcentaje.getText().toString());
        Contrato.setVariosMonto(binding.editVarios.getText().toString());
        Contrato.setNoDesc(binding.editNoDesc.getText().toString());
        Contrato.setEngDiferidoMonto(binding.editEngDiferido.getText().toString());
        Contrato.setNoPagosEng(binding.editNoPagosEng.getText().toString());
        Contrato.setSaldoEnganche(binding.editSaldoEng.getText().toString());
        Contrato.setMontoFinanciar(binding.editMontoFinanciar.getText().toString());
        Contrato.setCostoContrato(binding.editCostoContrato.getText().toString());
        Contrato.setPagoSala(binding.editpagosala.getText().toString());
        Contrato.setCostoMembresia(binding.editcostomembresia.getText().toString());

        // Save dynamic Contratos (Xrefs)
        List<String> Contratos = new ArrayList<>();
        for (int i = 0; i < binding.containerContratosDinamicos.getChildCount(); i++) {
            View v = binding.containerContratosDinamicos.getChildAt(i);
            if (v instanceof EditText) Contratos.add(((EditText) v).getText().toString());
        }
        Contrato.setContratosMontoCuenta(Contratos);

        // Save dynamic discounts
        List<ContratoModelo.DescuentoDetalle> discounts = new ArrayList<>();
        if (!listaDescuentos.isEmpty()) {
            for (DescuentoItem item : listaDescuentos) {
                discounts.add(new ContratoModelo.DescuentoDetalle(String.valueOf(item.montoDesc), item.descripcion));
            }
        } else {
            for (int i = 0; i < binding.containerDescuentosDinamicos.getChildCount(); i++) {
                View row = binding.containerDescuentosDinamicos.getChildAt(i);
                if (row instanceof LinearLayout) {
                    String m = ((EditText) ((LinearLayout) row).getChildAt(0)).getText().toString();
                    String d = ((EditText) ((LinearLayout) row).getChildAt(1)).getText().toString();
                    if (!m.isEmpty() || !d.isEmpty()) discounts.add(new ContratoModelo.DescuentoDetalle(m, d));
                }
            }
        }
        Contrato.setDescuentosDetalle(discounts);

        // Save Deferred Payments
        List<ContratoModelo.PagoDiferido> deferredPayments = new ArrayList<>();
        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i++) {
            View row = binding.containerPagosDinamicos.getChildAt(i);
            if (row instanceof LinearLayout) {
                String m = ((EditText) ((LinearLayout) row).getChildAt(0)).getText().toString();
                String f = ((EditText) ((LinearLayout) row).getChildAt(1)).getText().toString();
                deferredPayments.add(new ContratoModelo.PagoDiferido(m, f));
            }
        }
        Contrato.setPagosDiferidos(deferredPayments);

        viewModel.setContrato(Contrato);
    }

    private void limpiarInventario() {
        binding.editNoAnios.setText("");
        binding.spUnidad.setSelection(0);
        binding.spTemporada.setSelection(0);
        binding.spAnioUso.setSelection(0);
    }

    private void limpiarDatosVenta() {
        binding.editPrecioBruto.setText("");
        binding.editMontoCuenta.setText("");
        binding.editNoContratosVenta.setText("");
        binding.containerContratosDinamicos.removeAllViews();
        binding.editEnganchePorcentaje.setText("");
        binding.editEngancheMonto.setText("");
        binding.editEngancheSalaMonto.setText("");
        binding.editEngancheSalaPorcentaje.setText("");
        binding.editVarios.setText("");
        binding.editNoDesc.setText("");
        binding.containerDescuentosDinamicos.removeAllViews();
        binding.editEngDiferido.setText("");
        binding.editNoPagosEng.setText("");
        binding.containerPagosDinamicos.removeAllViews();
        binding.editCostoContrato.setText("");
        binding.editcostomembresia.setText("");
        binding.editMontoFinanciar.setText("");
        binding.editpagosala.setText("");
        listaDescuentos.clear();
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            String clean = value.replaceAll("[^\\d.]", "");
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class DescuentoItem {
        final int no;
        final String descripcion;
        final String remision;
        final double montoRem;
        final double montoDesc;
        final String referencia;
        DescuentoItem(int no, String descripcion, String remision, double montoRem, double montoDesc, String referencia) {
            this.no = no;
            this.descripcion = descripcion;
            this.remision = remision;
            this.montoRem = montoRem;
            this.montoDesc = montoDesc;
            this.referencia = referencia;
        }
    }

    private static class DescuentoAdapter extends RecyclerView.Adapter<DescuentoAdapter.ViewHolder> {
        private final List<DescuentoItem> items;
        DescuentoAdapter(List<DescuentoItem> items) { this.items = items; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(4, 4, 4, 4);
            for (int i = 0; i < 6; i++) {
                TextView tv = new TextView(parent.getContext());
                int dipWidth = (i == 0) ? 40 : (i == 1) ? 120 : (i == 2) ? 80 : 100;
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipWidth, parent.getResources().getDisplayMetrics());
                layout.addView(tv, new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setTextSize(11);
                tv.setGravity(android.view.Gravity.CENTER);
            }
            return new ViewHolder(layout);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DescuentoItem item = items.get(position);
            ViewGroup layout = (ViewGroup) holder.itemView;
            ((TextView) layout.getChildAt(0)).setText(String.valueOf(item.no));
            ((TextView) layout.getChildAt(1)).setText(item.descripcion);
            ((TextView) layout.getChildAt(2)).setText(item.remision);
            ((TextView) layout.getChildAt(3)).setText(String.format(Locale.US, "$%.2f", item.montoRem));
            ((TextView) layout.getChildAt(4)).setText(String.format(Locale.US, "$%.2f", item.montoDesc));
            ((TextView) layout.getChildAt(5)).setText(item.referencia);
        }
        @Override public int getItemCount() { return items.size(); }
        void addItem(DescuentoItem item) { items.add(item); notifyItemInserted(items.size() - 1); }
        void removeLast() { if (!items.isEmpty()) { items.remove(items.size() - 1); notifyItemRemoved(items.size()); } }
        void clear() { int size = items.size(); items.clear(); notifyItemRangeRemoved(0, size); }
        List<DescuentoItem> getItems() { return items; }
        static class ViewHolder extends RecyclerView.ViewHolder { ViewHolder(@NonNull View itemView) { super(itemView); } }
    }
}
