package com.example.contrato;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.databinding.DialogDescuentosBinding;
import com.example.contrato.databinding.FragmentDatosVentaBinding;
import com.example.contrato.repository.VentasInventarioRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatosVentaFragment extends Fragment {
    private FragmentDatosVentaBinding binding;
    private List<DescuentoItem> listaDescuentos = new ArrayList<>();
    private SharedContratoViewModel viewModel;

    private VentasInventarioRepository repoVentas = new VentasInventarioRepository();

    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};
    private boolean modoMensual = false;
    private boolean construyendoPagos = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDatosVentaBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);
        seleccionaBoton(binding.btnNueva);
        seleccionaBoton(binding.btnFinanciado);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUnidadSpinner();
        setupTemporadaSpinner();
        setupTipoOcupacionSpinner();
        setupMonedaSpinner();
        setupCalculosPrecios();
        setupContratosDinamicos();
        setupEngancheCalculos();
        setupEngancheSalaCalculos();
        setupDescuentosDinamicos();
        setupPagosDiferidos();
        setupTotalesCalculos();
        LogicaTipodePago();
        setupBotones();
        setupPrefijosMoneda();
        setupTipoCambio();


        cargaDatosExistentes();

        binding.btnLimpiarInventario.setOnClickListener(v -> limpiarInventario());
        binding.btnLimpiarDatosdeVenta.setOnClickListener(v -> limpiarDatosVenta());

        binding.AceptarTarea.setOnClickListener(v -> {
            if (validarCampos()) {
                guardaDatosViewModel();
                irAFinanciamiento();
            }
        });

        //si da click a spinner año uso le avisa al usuario que primero hay que seleccionar un tipo de ocupación
        binding.spAnioUso.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (binding.spTipoOcupacion.getSelectedItem().toString().contains("Selec")) {
                    Toast.makeText(
                            requireContext(),
                            "Seleccione un tipo de ocupación primero.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return true;
                }
            }
            return false;
        });
    }
    private void irAFinanciamiento() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // Simulamos la pulsación en el menú inferior para mantener la sincronización y permitir regresar
            activity.binding.bottomNav.setSelectedItemId(R.id.nav_financiamiento);
        }
    }

    private void setupUnidadSpinner() {
        viewModel.getUnidades().observe(getViewLifecycleOwner(), unidades -> {
            if (unidades != null) {

                // para que la primera opción sea "seleccionar"
                List<String> opciones = new ArrayList<>();
                if(getLenguajeActual().equalsIgnoreCase("es")){
                    opciones.add("Seleccionar");
                }else if(getLenguajeActual().contains("en")){
                    opciones.add("Select");
                }

                opciones.addAll(unidades);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        opciones
                );

                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );

                binding.spUnidad.setAdapter(adapter);

                ContratoModelo contrato = viewModel.getContratoValue();

                if (contrato != null && contrato.getUnidad() != null) {
                    setSpinnerValue(binding.spUnidad, contrato.getUnidad());
                } else {
                    // default selected = "Seleccionar"
                    binding.spUnidad.setSelection(0);
                }
            }
        });

        viewModel.fetchUnidades();
    }
    private String getLenguajeActual() {
        LocaleListCompat localeActual = AppCompatDelegate.getApplicationLocales();
        if (!localeActual.isEmpty()) {
            return localeActual.get(0).getLanguage();
        }
        return Locale.getDefault().getLanguage();
    }

    private void setupTemporadaSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.temporadas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTemporada.setAdapter(adapter);
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) return;

        if ("Upgrade".equals(Contrato.getTipoVenta())) {
            seleccionaBoton(binding.btnUpgrade);
        } else {
            seleccionaBoton(binding.btnNueva);
        }

        if ("Contado".equals(Contrato.getTipoPago())) {
            seleccionaBoton(binding.btnContado);
        } else {
            seleccionaBoton(binding.btnFinanciado);
        }
        binding.EngacheColapsable.setVisibility(
                binding.btnContado.isChecked()
                        ? View.GONE
                        : View.VISIBLE
        );
        actualizarVisibilidadMontoCuenta();

        setSpinnerValue(binding.spUnidad, Contrato.getUnidad());
        setSpinnerValue(binding.spTemporada, Contrato.getTemporada());
        setSpinnerValue(binding.spTipoOcupacion, Contrato.getTipoOcupacion());
        setSpinnerValue(binding.spTipoOcupacion, Contrato.getTipoOcupacion());


        binding.spAnioUso.post(() -> {
            setSpinnerValue(binding.spAnioUso, Contrato.getAnioUso());
        });
        binding.editNoAnios.setText(Contrato.getNoAnios());

        setSpinnerValue(binding.spMoneda, Contrato.getMoneda());
        binding.editTipoCambio.setText(Contrato.getTipoCambio());
        binding.editPrecioBruto.setText(Contrato.getPrecioBruto());
        binding.editMontoCuenta.setText(Contrato.getMontoCuenta());
        binding.editNoContratosVenta.setText(Contrato.getNoContratosMC());
        binding.editPrecioNeto.setText(Contrato.getPrecioNeto());



        binding.editEnganchePorcentaje.setText(Contrato.getEnganchePorcentaje());
        binding.editEngancheMonto.setText(Contrato.getEngancheTotal());
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

        // restaura contratos dinamicos
        if (Contrato.getContratosMontoCuenta() != null) {
            binding.editNoContratosVenta.setText(String.valueOf(Contrato.getContratosMontoCuenta().size()));
            binding.containerContratosDinamicos.removeAllViews();
            for (String xref : Contrato.getContratosMontoCuenta()) {
                int NoContratos = Integer.parseInt(Contrato.getNoContratosMC());
                EditText et = CreaEditTextContrato(NoContratos);
                et.setText(xref);
                binding.containerContratosDinamicos.addView(et);
            }
        }

        // restaura descuentos dinamicos
        if (Contrato.getDescuentosDetalle() != null) {

            listaDescuentos.clear();

            binding.editNoDesc.setText(
                    String.valueOf(Contrato.getDescuentosDetalle().size())
            );

            binding.containerDescuentosDinamicos.removeAllViews();

            for (int i = 0; i < Contrato.getDescuentosDetalle().size(); i++) {

                ContratoModelo.DescuentoDetalle dd =
                        Contrato.getDescuentosDetalle().get(i);

                listaDescuentos.add(
                        new DescuentoItem(
                                i + 1,
                                dd.descripcion,
                                "REM-" + (i + 1),
                                parseDouble(dd.monto),
                                parseDouble(dd.monto),
                                "REF-" + (i + 1)
                        )
                );

                View row = creaFilaDescuentos(i);

                LinearLayout layout = (LinearLayout) row;

                EditText etMonto = (EditText) layout.getChildAt(0);
                EditText etDesc = (EditText) layout.getChildAt(1);

                etMonto.setText(dd.monto);
                etDesc.setText(dd.descripcion);

                binding.containerDescuentosDinamicos.addView(row);
            }
        }
        if (Contrato.getPagosDiferidos() != null && !Contrato.getPagosDiferidos().isEmpty()) {
            binding.containerPagosDinamicos.removeAllViews();

            int numPagos = Contrato.getPagosDiferidos().size();
            double totalDiferido = parseDouble(Contrato.getEngDiferidoMonto());
            long totalCentavos = Math.round(totalDiferido * 100);
            long cuotaBase = numPagos > 0 ? totalCentavos / numPagos : 0;
            long residuo = numPagos > 0 ? totalCentavos - (cuotaBase * numPagos) : 0;

            List<String> fechas = new ArrayList<>();
            for (ContratoModelo.PagoDiferido pd : Contrato.getPagosDiferidos()) {
                fechas.add(pd.fecha);
            }

            agregarFilasPagos3Columnas(numPagos, fechas, cuotaBase, residuo);

            // Restaura montos individuales (pueden diferir del promedio si el usuario los editó)
            int pagoIdx = 0;
            for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i += 2) {
                View rowMontos = binding.containerPagosDinamicos.getChildAt(i);
                if (rowMontos instanceof LinearLayout) {
                    LinearLayout lm = (LinearLayout) rowMontos;
                    for (int col = 0; col < lm.getChildCount() && pagoIdx < numPagos; col++) {
                        View child = lm.getChildAt(col);
                        if (child instanceof EditText) {
                            ((EditText) child).setText(
                                    Contrato.getPagosDiferidos().get(pagoIdx).monto
                            );
                            pagoIdx++;
                        }
                    }
                }
            }
        }}

    private void muestraDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int anio  = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia   = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, anio1, mesAnio, diaMes) -> {
                    String lang = getLenguajeActual();
                    String d = String.format(Locale.US, "%02d", diaMes);
                    String m = String.format(Locale.US, "%02d", mesAnio + 1);
                    String y = String.valueOf(anio1);

                    String fechaSeleccionada = lang.equals("en")
                            ? m + "/" + d + "/" + y   // MM/DD/YYYY
                            : d + "/" + m + "/" + y;  // DD/MM/YYYY

                    String fecha = convertirMesANombreString(fechaSeleccionada);
                    editText.setText(fecha);
                }, anio, mes, dia);

        dialog.show();
    }

    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }

    //si español DD/MM/AAAA, si ingles MM/DD/YYYY
    private void setupFormatoFecha(EditText editText) {

        editText.setImeOptions(6);
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
    }private TextInputLayout creaCampoFechaConCalendario() {

        TextInputLayout til = new TextInputLayout(requireContext());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        200,
                        getResources().getDisplayMetrics()
                ),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // CENTRAR TODO EL CAMPO EN EL MODAL
        lp.gravity = Gravity.CENTER_HORIZONTAL;

        // márgenes simétricos
        lp.setMargins(0, 4, 0, 4);

        til.setLayoutParams(lp);

        til.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        til.setEndIconDrawable(R.drawable.ic_calendario);

        TextInputEditText et = new TextInputEditText(requireContext());

        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        46,
                        getResources().getDisplayMetrics()
                )
        );

        et.setLayoutParams(etLp);

        et.setBackgroundResource(R.drawable.bg_input);
        et.setHint(getString(R.string.fechaformato));
        et.setInputType(InputType.TYPE_CLASS_DATETIME);

        // padding balanceado
        et.setPadding(14, 0, 14, 0);
        et.setTextSize(13);
        et.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        et.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        et.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et.setSingleLine(true);

        til.addView(et);

        return til;
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

    private void setupPrefijosMoneda() {
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
                binding.editcostomembresia.getText().toString().isEmpty()) {

            Toast.makeText(requireContext(), "Por favor, complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.btnUpgrade.isChecked() && binding.editMontoCuenta.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese el monto de la cuenta para Upgrade", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(binding.spTemporada.getSelectedItem().toString().contains("Selec") ){
            Toast.makeText(requireContext(), "Seleccione una Temporada", Toast.LENGTH_SHORT).show();
        }
        if(binding.spUnidad.getSelectedItem().toString().contains("Selec")){
            Toast.makeText(requireContext(), "Seleccione una Unidad", Toast.LENGTH_SHORT).show();
        }
        if(binding.spTipoOcupacion.getSelectedItem().toString().contains("Selec")){
            Toast.makeText(requireContext(), "Seleccione un Tipo de Ocupación", Toast.LENGTH_SHORT).show();
        }
        if (!binding.btnContado.isChecked() && !binding.btnFinanciado.isChecked()) {
            Toast.makeText(requireContext(), "Ingrese un Tipo de Pago", Toast.LENGTH_SHORT).show();
            return false;
        }

        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i++) {
            View row = binding.containerPagosDinamicos.getChildAt(i);
            if (row instanceof LinearLayout) {
                View dateField = ((LinearLayout) row).getChildAt(1);
                if (dateField instanceof TextInputLayout) {
                    TextInputEditText etFecha =
                            (TextInputEditText)
                                    ((TextInputLayout) dateField).getEditText();

                    if (etFecha != null &&
                            etFecha.getText().toString().isEmpty()) {
                        Toast.makeText(
                                requireContext(),
                                "Por favor, seleccione las fechas para todos los pagos diferidos",
                                Toast.LENGTH_SHORT
                        ).show();
                        return false;
                    }
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


    public void setupTipoCambio() {
        new Thread(() -> {
            try {
                String tipo = repoVentas.getTipoCambio();
                if (binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.editTipoCambio.setText(tipo);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void seleccionaBoton(MaterialButton seleccionado) {

        // Grupo Tipo Venta
        if (seleccionado == binding.btnNueva || seleccionado == binding.btnUpgrade) {
            resetEstiloBoton(binding.btnNueva);
            resetEstiloBoton(binding.btnUpgrade);
        }

        // Grupo Tipo Pago
        if (seleccionado == binding.btnContado || seleccionado == binding.btnFinanciado) {
            resetEstiloBoton(binding.btnContado);
            resetEstiloBoton(binding.btnFinanciado);
        }

        // Activar solo el seleccionado
        seleccionado.setChecked(true);
        seleccionado.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#0A0E21"))
        );
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
        Spinner TO = binding.spTipoOcupacion;
        int anioactual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> anios = new ArrayList<>();

        if(getLenguajeActual().equalsIgnoreCase("es")){
            anios.add("Seleccionar");
        }else if(getLenguajeActual().contains("en")){
            anios.add("Select");
        }
        if(TO.getSelectedItem().toString().equalsIgnoreCase("Corridos")|| binding.spTipoOcupacion.getSelectedItem().toString().equalsIgnoreCase("Consecutive years")){

            for (int i = 0; i <= 2; i++) {
                anios.add(String.valueOf(anioactual + i));
            }
        }
        if(TO.getSelectedItem().toString().contains("Pares" )|| binding.spTipoOcupacion.getSelectedItem().toString().contains("Even")){
            while(anios.size() < 4){
                if(anioactual % 2 == 0){
                    anios.add(String.valueOf(anioactual));
                    anioactual+=2;
                }else{
                    anioactual++;
                }
            }

        }
        if(binding.spTipoOcupacion.getSelectedItem().toString().contains("Nones" )|| binding.spTipoOcupacion.getSelectedItem().toString().contains("Odd")){
            while(anios.size() < 4){
                if(anioactual % 2 != 0){
                    anios.add(String.valueOf(anioactual));
                    anioactual+=2;
                }else{
                    anioactual++;
                }
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, anios);
        binding.spAnioUso.setAdapter(adapter);


    }

    private void setupTipoOcupacionSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.tiposOcupacion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTipoOcupacion.setAdapter(adapter);
        binding.spTipoOcupacion.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {

                        setupAnioUsoSpinner();

                        ContratoModelo contrato = viewModel.getContratoValue();
                        if (contrato != null && contrato.getAnioUso() != null) {
                            binding.spAnioUso.post(() -> {
                                setSpinnerValue(binding.spAnioUso, contrato.getAnioUso());
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );

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

            if (binding.btnContado.isChecked()) {
                binding.editEnganchePorcentaje.setText("100");
                updateEngancheMN();
            } else {
                updateEngancheMN();
                updateEngancheSalaMN();
            }
            calculaMontoFinanciar();
            calculaEngancheDiferido();
            calculaTotalPagoSala();
        } catch (Exception e) {
            binding.editPrecioNeto.setText("$0.00");
        }
    }

    private void setupContratosDinamicos() {
        binding.editNoContratosVenta.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String val = s.toString().trim();

                if (!val.isEmpty()) {
                    try {
                        int num = Integer.parseInt(val);

                        if (num > 7) {
                            isUpdating = true;
                            num = 7;
                            binding.editNoContratosVenta.setText("7");
                            binding.editNoContratosVenta.setSelection(1);
                            isUpdating = false;
                        }
                        List<String> oldValues = new ArrayList<>();
                        for (int i = 0; i < binding.containerContratosDinamicos.getChildCount(); i++) {
                            View v = binding.containerContratosDinamicos.getChildAt(i);
                            if (v instanceof EditText) {
                                oldValues.add(((EditText) v).getText().toString());
                            }
                        }

                        binding.containerContratosDinamicos.removeAllViews();
                        for (int i = 0; i < num; i++) {
                            EditText et = CreaEditTextContrato(i + 1);

                            if (i < oldValues.size()) {
                                et.setText(oldValues.get(i));
                            }

                            binding.containerContratosDinamicos.addView(et);
                        }

                    } catch (NumberFormatException ignored) {}
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private EditText CreaEditTextContrato(int numero) {
        EditText et = new EditText(requireContext());

        int width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                140,
                getResources().getDisplayMetrics()
        );

        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                40,
                getResources().getDisplayMetrics()
        );

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = width;
        lp.height = height;

        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6,
                getResources().getDisplayMetrics()
        );

        lp.setMargins(margin, margin, margin, margin);

        et.setLayoutParams(lp);

        et.setBackgroundResource(R.drawable.bg_input);
        et.setPadding(16, 0, 16, 0);

        et.setHint("No. Contrato " + numero);

        et.setTextSize(15);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT);

        et.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(15)
        });

        //Aqui vamos a revisar si el contrato ya existe en la BD
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String xref = s.toString(); //obtiene el xref que escribe el usuario

                if (xref.length() >= 1) {
                    new Thread(() -> {
                        try {
                            String estatus = repoVentas.getEstatusContrato(xref);

                            requireActivity().runOnUiThread(() -> {
                                if (estatus == null) { //si el contrato no existe pintamos de amarillo
                                    et.setBackgroundTintList(
                                            ColorStateList.valueOf(Color.parseColor("#ADF6ECAB")));

                                } else if (!estatus.equals("2")) {
                                    et.setBackgroundTintList(
                                            ColorStateList.valueOf(Color.parseColor("#7FF6ADAD")));

                                } else{
                                    et.setBackgroundTintList(
                                            ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }

            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){}
        });

        return et;


    }

    private void setupEngancheCalculos() {
        binding.editEnganchePorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEnganchePorcentaje.hasFocus()) {
                    updateEngancheMN();
                    calculaEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculaMontoFinanciar();
            }
        });

        binding.editEngancheMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheMonto.hasFocus()) {
                    updateEnganchePercent();
                    calculaEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculaMontoFinanciar();
            }
        });
    }

    private void setupEngancheSalaCalculos() {
        binding.editEngancheSalaPorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaPorcentaje.hasFocus()) {
                    updateEngancheSalaMN();
                    calculaEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculaTotalPagoSala();
            }
        });

        binding.editEngancheSalaMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaMonto.hasFocus()) {
                    updateEngancheSalaPercent();
                    calculaEngancheDiferido();
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {
                calculaTotalPagoSala();
            }
        });
    }

    private void updateEngancheMN() {
        double neto = parseDouble(binding.editPrecioNeto.getText().toString());
        double porc = parseDouble(binding.editEnganchePorcentaje.getText().toString());
        double montoEnganche = neto * (porc / 100.0);
        binding.editEngancheMonto.setText(String.format(Locale.US, "$%.2f", montoEnganche));
    }

    private void calculaTotalPagoSala() {
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

    private void setupDescuentosDinamicos() {
        binding.editNoDesc.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                List<String[]> oldDescuentos = new ArrayList<>();

                for (int i = 0; i < binding.containerDescuentosDinamicos.getChildCount(); i++) {
                    View row = binding.containerDescuentosDinamicos.getChildAt(i);

                    if (row instanceof LinearLayout) {
                        EditText monto = (EditText) ((LinearLayout) row).getChildAt(0);
                        EditText desc = (EditText) ((LinearLayout) row).getChildAt(1);

                        oldDescuentos.add(new String[]{
                                monto.getText().toString(),
                                desc.getText().toString()
                        });
                    }
                }

                binding.containerDescuentosDinamicos.removeAllViews();                try {
                    String val = s.toString().trim();
                    if (val.isEmpty()) {
                        updateTotalDescuentos();
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
                        View row = creaFilaDescuentos(i);

                        // restaura valores anteriores
                        if (i < oldDescuentos.size()) {
                            LinearLayout layout = (LinearLayout) row;

                            ((EditText) layout.getChildAt(0))
                                    .setText(oldDescuentos.get(i)[0]);

                            ((EditText) layout.getChildAt(1))
                                    .setText(oldDescuentos.get(i)[1]);
                        }

                        binding.containerDescuentosDinamicos.addView(row);
                    }
                    updateTotalDescuentos();
                } catch (Exception ignored) {}
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private View creaFilaDescuentos(int i) {
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateTotalDescuentos(); }
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

    private void updateTotalDescuentos() {
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
        calculaEngancheDiferido();
    }
    private void setupPagosDiferidos() {

        binding.btnMensual.setOnClickListener(v -> {
            modoMensual = true;
            seleccionaBotonPago(binding.btnMensual, binding.btnAbierto);
            mostrarModalFechaInicial();
        });

        binding.btnAbierto.setOnClickListener(v -> {
            modoMensual = false;
            fechaInicialMensual = null;
            seleccionaBotonPago(binding.btnAbierto, binding.btnMensual);
            updatePagosDiferidos(null);
            calculaSaldoEnganche();
        });

        binding.btnLimpiarMontos.setOnClickListener(v -> limpiarMontosPagos());

        binding.editEngDiferido.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculaSaldoEnganche();
            }
        });

        binding.editNoPagosEng.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        int num = Integer.parseInt(val);
                        if (num > 9) {
                            isUpdating = true;
                            binding.editNoPagosEng.setText("9");
                            binding.editNoPagosEng.setSelection(1);
                            isUpdating = false;
                        }
                    } catch (NumberFormatException ignored) {}
                }

                if (modoMensual) {
                    updatePagosMensual(fechaInicialMensual);
                } else {
                    updatePagosDiferidos(null);
                }
                calculaSaldoEnganche();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Variable para guardar la fecha inicial del modo mensual
    private String fechaInicialMensual = null;
    private void mostrarModalFechaInicial() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundResource(android.R.color.white);

        TextView tvTitulo = new TextView(requireContext());
        tvTitulo.setText("Fecha del primer pago");
        tvTitulo.setTextSize(16);
        tvTitulo.setGravity(Gravity.CENTER);
        tvTitulo.setTextColor(Color.parseColor("#0A0E21"));
        tvTitulo.setPadding(0, 0, 0, 24);
        root.addView(tvTitulo);

        TextInputLayout tilFecha = creaCampoFechaConCalendario();
        TextInputEditText etFecha = (TextInputEditText) tilFecha.getEditText();

        etFecha.setGravity(Gravity.CENTER);
        etFecha.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        ViewGroup.LayoutParams lp = etFecha.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        etFecha.setLayoutParams(lp);
        etFecha.setFocusable(true);
        etFecha.setFocusableInTouchMode(true);
        etFecha.setClickable(true);
        tilFecha.setEndIconOnClickListener(v -> muestraDatePicker(etFecha));
        setupFormatoFecha(etFecha);

        if (fechaInicialMensual != null && !fechaInicialMensual.isEmpty()) {
            etFecha.setText(fechaInicialMensual);
        }
        root.addView(tilFecha);

        LinearLayout btnsRow = new LinearLayout(requireContext());
        btnsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnsLp.topMargin = 32;
        btnsRow.setLayoutParams(btnsLp);

        MaterialButton btnCancelar = new MaterialButton(requireContext());
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelLp.setMargins(0, 0, 8, 0);
        btnCancelar.setLayoutParams(cancelLp);
        btnCancelar.setText("Cancelar");
        btnCancelar.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btnCancelar.setTextColor(Color.parseColor("#1E293B"));
        btnCancelar.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        btnCancelar.setStrokeWidth(1);

        MaterialButton btnAceptar = new MaterialButton(requireContext());
        LinearLayout.LayoutParams aceptarLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        aceptarLp.setMargins(8, 0, 0, 0);
        btnAceptar.setLayoutParams(aceptarLp);
        btnAceptar.setText("Aceptar");
        btnAceptar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0A0E21")));
        btnAceptar.setTextColor(Color.WHITE);

        btnsRow.addView(btnCancelar);
        btnsRow.addView(btnAceptar);
        root.addView(btnsRow);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.show();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnAceptar.setOnClickListener(v -> {
            String fecha = etFecha.getText() != null ? etFecha.getText().toString() : "";
            if (fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona la fecha del primer pago", Toast.LENGTH_SHORT).show();
                return;
            }
            fechaInicialMensual = fecha;
            dialog.dismiss();
            updatePagosMensual(fechaInicialMensual);
            calculaSaldoEnganche();
        });
    }
    private double parseMonto(String texto) {
        if (texto == null) return 0.0;
        texto = texto.replace("$", "")
                .replace(",", "")
                .trim();

        if (texto.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void recalculaEngancheDiferidoDesdeTextBoxes() {
        double total = 0.0;

        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i += 2) {
            View rowMontos = binding.containerPagosDinamicos.getChildAt(i);

            if (rowMontos instanceof LinearLayout) {
                LinearLayout lm = (LinearLayout) rowMontos;

                for (int col = 0; col < lm.getChildCount(); col++) {
                    View child = lm.getChildAt(col);

                    if (child instanceof EditText) {
                        total += parseMonto(((EditText) child).getText().toString());
                    }
                }
            }
        }

        String totalFormateado = String.format(Locale.US, "$%.2f", total);
        binding.editEngDiferido.setText(totalFormateado);
    }

    private void updatePagosMensual(String fechaInicial) {
        if (fechaInicial == null || fechaInicial.isEmpty()) return;

        String numPagosStr = binding.editNoPagosEng.getText().toString().trim();
        if (numPagosStr.isEmpty()) return;

        int numPagos;
        try {
            numPagos = Integer.parseInt(numPagosStr);
        } catch (NumberFormatException e) {
            return;
        }
        if (numPagos <= 0) return;

        // Calcula montos con compensación de centavo en el último pago
        double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
        long totalCentavos = Math.round(totalDiferido * 100); // trabaja en centavos para precisión
        long cuotaBase = totalCentavos / numPagos;
        long residuo = totalCentavos - (cuotaBase * numPagos);

        // Genera las fechas mensuales consecutivas
        List<String> fechas = generarFechasMensuales(fechaInicial, numPagos);

        // Reconstruye el container
        binding.containerPagosDinamicos.removeAllViews();
        agregarFilasPagos3Columnas(numPagos, fechas, cuotaBase, residuo);
    }

    private void updatePagosDiferidos(List<String[]> anterioresPagos) {
        if (anterioresPagos == null) {
            // Captura los valores actuales antes de limpiar
            anterioresPagos = new ArrayList<>();
            for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i++) {
                View fila = binding.containerPagosDinamicos.getChildAt(i);
                anterioresPagos.addAll(extraerDatosDeFila(fila));
            }
        }

        String numPagosStr = binding.editNoPagosEng.getText().toString().trim();
        if (numPagosStr.isEmpty()) {
            binding.containerPagosDinamicos.removeAllViews();
            return;
        }

        int numPagos;
        try {
            numPagos = Integer.parseInt(numPagosStr);
        } catch (NumberFormatException e) {
            return;
        }
        if (numPagos <= 0) {
            binding.containerPagosDinamicos.removeAllViews();
            return;
        }

        double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
        long totalCentavos = Math.round(totalDiferido * 100);
        long cuotaBase = totalCentavos / numPagos;
        long residuo = totalCentavos - (cuotaBase * numPagos);

        // Genera fechas vacías para modo abierto
        List<String> fechas = new ArrayList<>();
        for (int i = 0; i < numPagos; i++) {
            if (i < anterioresPagos.size()) {
                fechas.add(anterioresPagos.get(i)[1]);
            } else {
                fechas.add("");
            }
        }

        // Restaura montos anteriores si existen (modo abierto permite edición libre)
        binding.containerPagosDinamicos.removeAllViews();
        agregarFilasPagos3Columnas(numPagos, fechas, cuotaBase, residuo);

        // Restaura montos que el usuario ya había ingresado (solo modo abierto)
        for (int i = 0; i < Math.min(anterioresPagos.size(), numPagos); i++) {
            String montoAnterior = anterioresPagos.get(i)[0];
            if (!montoAnterior.isEmpty()) {
                View fila = getFilaEnIndice(i);
                if (fila instanceof LinearLayout) {
                    EditText etM = (EditText) ((LinearLayout) fila).getChildAt(0);
                    if (etM != null) etM.setText(montoAnterior);
                }
            }
        }
    }
    private void agregarFilasPagos3Columnas(int numPagos, List<String> fechas,
                                            long cuotaBase, long residuo) {
        construyendoPagos = true; // ← block recalculation while building
        final int COLS = 3;
        int filas = (int) Math.ceil((double) numPagos / COLS);

        for (int fila = 0; fila < filas; fila++) {
            LinearLayout rowMontos = new LinearLayout(requireContext());
            rowMontos.setOrientation(LinearLayout.HORIZONTAL);
            rowMontos.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout rowFechas = new LinearLayout(requireContext());
            rowFechas.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams fechasLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fechasLp.bottomMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            rowFechas.setLayoutParams(fechasLp);

            int pagosEnEstaFila = Math.min(COLS, numPagos - fila * COLS);

            for (int col = 0; col < pagosEnEstaFila; col++) {
                int idx = fila * COLS + col;

                long centavos = cuotaBase + (idx == numPagos - 1 ? residuo : 0);
                double monto = centavos / 100.0;

                EditText etMonto = creaEditTextPequeñoCuadrado("Pago " + (idx + 1));
                etMonto.setText(String.format(Locale.US, "$%.2f", monto));
                agregaPrefijoMoneda(etMonto);

                etMonto.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void afterTextChanged(Editable s) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (construyendoPagos) return; // ← skip during build
                        recalculaEngancheDiferidoDesdeTextBoxes();
                        calculaSaldoEnganche();
                    }
                });

                LinearLayout.LayoutParams lpMonto = new LinearLayout.LayoutParams(
                        0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44,
                        getResources().getDisplayMetrics()), 1f);
                lpMonto.setMargins(
                        col > 0 ? (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                                getResources().getDisplayMetrics()) : 0,
                        0, 0, 4);
                etMonto.setLayoutParams(lpMonto);
                rowMontos.addView(etMonto);

                TextInputLayout tilFecha = creaCampoFechaConCalendario();
                TextInputEditText etFecha = (TextInputEditText) tilFecha.getEditText();
                etFecha.setFocusable(true);
                etFecha.setFocusableInTouchMode(true);
                etFecha.setClickable(true);
                tilFecha.setEndIconOnClickListener(v -> muestraDatePicker(etFecha));
                setupFormatoFecha(etFecha);

                if (idx < fechas.size() && !fechas.get(idx).isEmpty()) {
                    etFecha.setText(fechas.get(idx));
                }

                if (modoMensual) {
                    etFecha.setFocusable(false);
                    etFecha.setFocusableInTouchMode(false);
                    etFecha.setClickable(false);
                    tilFecha.setEndIconOnClickListener(null);
                }

                LinearLayout.LayoutParams lpFecha = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lpFecha.setMargins(
                        col > 0 ? (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                                getResources().getDisplayMetrics()) : 0,
                        0, 0, 0);
                tilFecha.setLayoutParams(lpFecha);
                rowFechas.addView(tilFecha);
            }

            int faltantes = COLS - pagosEnEstaFila;
            for (int f = 0; f < faltantes; f++) {
                View espacio = new View(requireContext());
                espacio.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                rowMontos.addView(espacio);

                View espacioFecha = new View(requireContext());
                espacioFecha.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                rowFechas.addView(espacioFecha);
            }

            binding.containerPagosDinamicos.addView(rowMontos);
            binding.containerPagosDinamicos.addView(rowFechas);
        }

        construyendoPagos = false; // ← re-enable after all rows are built
        calculaSaldoEnganche();    // ← one clean recalc at the end
    }
    private List<String> generarFechasMensuales(String fechaInicial, int numPagos) {
        List<String> fechas = new ArrayList<>();
        try {
            Calendar cal = parsearFecha(fechaInicial);
            if (cal == null) {
                for (int i = 0; i < numPagos; i++) fechas.add("");
                return fechas;
            }

            for (int i = 0; i < numPagos; i++) {
                String f;
                if (esIngles()) {
                    // MM/mes/YYYY
                    String mes = MESES_EN[cal.get(Calendar.MONTH)];
                    String fechaStr = String.format(Locale.US, "%02d/%s/%d",
                            cal.get(Calendar.DAY_OF_MONTH),
                            mes,
                            cal.get(Calendar.YEAR));
                    f = fechaStr;
                } else {
                    // DD/mes/YYYY
                    String mes = MESES_ES[cal.get(Calendar.MONTH)];
                    f = String.format(Locale.US, "%02d/%s/%d",
                            cal.get(Calendar.DAY_OF_MONTH),
                            mes,
                            cal.get(Calendar.YEAR));
                }
                fechas.add(f);
                cal.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            for (int i = 0; i < numPagos; i++) fechas.add("");
        }
        return fechas;
    }private Calendar parsearFecha(String fecha) {
        if (fecha == null || fecha.length() < 10) return null;
        try {
            Calendar cal = Calendar.getInstance();
            String[] partes = fecha.split("/");
            if (partes.length != 3) return null;

            int dia, mes, anio;
            if (esIngles()) {
                // mes(nombre)/dia/anio  o  MM/DD/YYYY
                mes = parsearMes(partes[0]);
                dia = Integer.parseInt(partes[1]);
                anio = Integer.parseInt(partes[2]);
            } else {
                // dia/mes(nombre)/anio  o  DD/MM/YYYY
                dia = Integer.parseInt(partes[0]);
                mes = parsearMes(partes[1]);
                anio = Integer.parseInt(partes[2]);
            }

            if (mes < 0) return null;
            cal.set(anio, mes, dia);
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    // devuelve índice de mes 0-11, acepta nombre o número
    private int parsearMes(String s) {
        try {
            return Integer.parseInt(s) - 1; // numérico: "03" -> 2
        } catch (NumberFormatException ignored) {}

        String lower = s.toLowerCase(Locale.ROOT);
        for (int i = 0; i < MESES_ES.length; i++) {
            if (MESES_ES[i].equals(lower) || MESES_EN[i].equals(lower)) return i;
        }
        return -1;
    }
    private void limpiarMontosPagos() {
        // Recalcula montos divididos igualmente y los resetea
        String numPagosStr = binding.editNoPagosEng.getText().toString().trim();
        if (numPagosStr.isEmpty()) return;

        int numPagos;
        try {
            numPagos = Integer.parseInt(numPagosStr);
        } catch (NumberFormatException e) {
            return;
        }

        double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
        long totalCentavos = Math.round(totalDiferido * 100);
        long cuotaBase = numPagos > 0 ? totalCentavos / numPagos : 0;
        long residuo = numPagos > 0 ? totalCentavos - (cuotaBase * numPagos) : 0;

        int pagoIdx = 0;
        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i += 2) {
            View rowMontos = binding.containerPagosDinamicos.getChildAt(i);
            if (rowMontos instanceof LinearLayout) {
                LinearLayout lm = (LinearLayout) rowMontos;
                for (int col = 0; col < lm.getChildCount(); col++) {
                    View child = lm.getChildAt(col);
                    if (child instanceof EditText && pagoIdx < numPagos) {
                        long centavos = cuotaBase + (pagoIdx == numPagos - 1 ? residuo : 0);
                        ((EditText) child).setText(String.format(Locale.US, "$%.2f", centavos / 100.0));
                        pagoIdx++;
                    }
                }
            }
        }
    }


    private void seleccionaBotonPago(MaterialButton activo, MaterialButton inactivo) {
        activo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0A0E21")));
        activo.setTextColor(Color.WHITE);
        activo.setStrokeWidth(0);

        inactivo.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        inactivo.setTextColor(Color.parseColor("#1E293B"));
        inactivo.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        inactivo.setStrokeWidth(1);
    }

    private void updateMontoPagosDiferidos() {
        int numPagos = binding.containerPagosDinamicos.getChildCount();
        if (numPagos == 0) return;
        try {
            double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
            double split = totalDiferido / numPagos;
            for (int i = 0; i < numPagos; i++) {
                View row = binding.containerPagosDinamicos.getChildAt(i);
                if (row instanceof LinearLayout) {
                    EditText etMonto = (EditText) ((LinearLayout) row).getChildAt(0);
                    // solo actualiza si el usuario no lo ha editado manualmente
                    // (puedes quitar esta condición si prefieres siempre recalcular)
                    etMonto.setText(String.format(Locale.US, "$%.2f", split));
                }
            }
        } catch (Exception ignored) {}
    }
    private void updatePagosDiferidos() {
        List<String[]> anterioresPagos = new ArrayList<>();
        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i++) {
            View row = binding.containerPagosDinamicos.getChildAt(i);
            if (row instanceof LinearLayout) {
                EditText monto = (EditText) ((LinearLayout) row).getChildAt(0);
                View child1 = ((LinearLayout) row).getChildAt(1);
                String fechaStr = "";
                if (child1 instanceof TextInputLayout) {
                    EditText etFecha = (EditText) ((TextInputLayout) child1).getEditText();
                    if (etFecha != null) fechaStr = etFecha.getText().toString();
                } else if (child1 instanceof EditText) {
                    fechaStr = ((EditText) child1).getText().toString();
                }
                anterioresPagos.add(new String[]{monto.getText().toString(), fechaStr});
            }
        }

        binding.containerPagosDinamicos.removeAllViews();

        String numPagosStr = binding.editNoPagosEng.getText().toString().trim();
        if (numPagosStr.isEmpty()) return;

        int numPagos;
        try {
            numPagos = Integer.parseInt(numPagosStr);
        } catch (NumberFormatException e) {
            return;
        }
        if (numPagos <= 0) return;


        double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
        double split = numPagos > 0 ? totalDiferido / numPagos : 0;

        for (int i = 0; i < numPagos; i++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            EditText etMonto = creaEditTextPequeñoCuadrado("Monto");
            agregaPrefijoMoneda(etMonto);

            TextInputLayout tilFecha = creaCampoFechaConCalendario();
            TextInputEditText etFecha = (TextInputEditText) tilFecha.getEditText();

            etFecha.setFocusable(true);
            etFecha.setFocusableInTouchMode(true);
            etFecha.setClickable(true);
            setupFormatoFecha(etFecha);
            etFecha.setOnClickListener(v -> muestraDatePicker(etFecha));
            tilFecha.setEndIconOnClickListener(v -> muestraDatePicker(etFecha));

            if (i < anterioresPagos.size()) {
                etMonto.setText(anterioresPagos.get(i)[0]);
                etFecha.setText(anterioresPagos.get(i)[1]);
            } else {
                etMonto.setText(String.format(Locale.US, "$%.2f", split));
            }

            row.addView(etMonto);
            row.addView(tilFecha);
            binding.containerPagosDinamicos.addView(row);
        }
    }


    private EditText creaEditTextPequeñoCuadrado(String hint) {
        EditText et = new EditText(requireContext());
        et.setImeOptions(EditorInfo.IME_ACTION_DONE);
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44,
                getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(0, 4, 4, 4);
        et.setLayoutParams(lp);
        et.setBackgroundResource(R.drawable.bg_input);
        et.setTextSize(13);
        et.setPadding(10, 0, 10, 0);
        if (hint != null) et.setHint(hint);
        et.setSingleLine(true);
        return et;
    }
    private List<String[]> extraerDatosDeFila(View filaView) {
        List<String[]> datos = new ArrayList<>();
        return datos;
    }

    private View getFilaEnIndice(int pagoIdx) {
        int rowFila = pagoIdx / 3;
        int col = pagoIdx % 3;
        int viewIdx = rowFila * 2;
        if (viewIdx < binding.containerPagosDinamicos.getChildCount()) {
            return binding.containerPagosDinamicos.getChildAt(viewIdx);
        }
        return null;
    }

    private void calculaEngancheDiferido() {
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
            calculaSaldoEnganche();
        } catch (Exception ignored) {}
    }

    private void calculaSaldoEnganche() {
        try {
            double engancheMonto = parseDouble(binding.editEngancheMonto.getText().toString());
            double salaMonto = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            double discounts = parseDouble(binding.editVarios.getText().toString());

            double sumaPagosDiferidos = 0;
            for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i += 2) {
                View rowMontos = binding.containerPagosDinamicos.getChildAt(i);
                if (rowMontos instanceof LinearLayout) {
                    LinearLayout lm = (LinearLayout) rowMontos;
                    for (int col = 0; col < lm.getChildCount(); col++) {
                        View child = lm.getChildAt(col);
                        if (child instanceof EditText) {
                            sumaPagosDiferidos += parseDouble(((EditText) child).getText().toString());
                        }
                    }
                }
            }

            double saldo = engancheMonto - salaMonto - sumaPagosDiferidos - discounts;
            binding.editSaldoEng.setText(String.format(Locale.US, "$%.2f", saldo));
        } catch (Exception ignored) {}
    }

    private void calculaMontoFinanciar() {
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
                calculaTotalPagoSala();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCostoContrato.addTextChangedListener(totalsWatcher);
        binding.editcostomembresia.addTextChangedListener(totalsWatcher);
    }

    private boolean isContado = false;

    private void LogicaTipodePago() {

        binding.btnContado.setOnClickListener(v -> {
            isContado = true;

            seleccionaBoton(binding.btnContado);

            binding.editEnganchePorcentaje.setText("100");
            updateEngancheMN();

            binding.EngacheColapsable.setVisibility(View.GONE);

            calculaMontoFinanciar();
            calculaEngancheDiferido();
            calculaTotalPagoSala();
        });

        binding.btnFinanciado.setOnClickListener(v -> {
            isContado = false;

            seleccionaBoton(binding.btnFinanciado);

            binding.EngacheColapsable.setVisibility(View.VISIBLE);

            calculaMontoFinanciar();
            calculaEngancheDiferido();
            calculaTotalPagoSala();
        });
    }

    private void guardaDatosViewModel() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();

        Contrato.setTipoVenta(binding.btnNueva.isChecked() ? "Nueva" : "Upgrade");
        if (binding.spUnidad.getSelectedItem() != null) Contrato.setUnidad(binding.spUnidad.getSelectedItem().toString());
        int posTemporada = binding.spTemporada.getSelectedItemPosition();
        String temporada = "";
        switch (posTemporada) {
            case 1:
                temporada = "Platino";
                break;
            case 2:
                temporada = "Oro";
                break;
            case 3:
                temporada = "Plata";
                break;
            default:
                temporada = "Seleccionar";
                break;
        }

        Contrato.setTemporada(temporada);
        int posTipoOcupacion = binding.spTipoOcupacion.getSelectedItemPosition();
        String tipo = "";
        switch (posTipoOcupacion) {
            case 1:
                tipo = "Corridos";
                break;
            case 2:
                tipo = "Alternados Pares";
                break;
            case 3:
                tipo = "Alternados Nones";
                break;
            default:
                tipo = "Seleccionar";
                break;
        }

        Contrato.setTipoOcupacion(tipo);
        if (binding.spTipoOcupacion.getSelectedItem() != null) Contrato.setTipoOcupacion(binding.spTipoOcupacion.getSelectedItem().toString());

        if (binding.spAnioUso.getSelectedItem() != null) Contrato.setAnioUso(binding.spAnioUso.getSelectedItem().toString());
        Contrato.setNoAnios(binding.editNoAnios.getText().toString());

        if (binding.spMoneda.getSelectedItem() != null) Contrato.setMoneda(binding.spMoneda.getSelectedItem().toString());
        Contrato.setTipoCambio(binding.editTipoCambio.getText().toString());
        Contrato.setPrecioBruto(binding.editPrecioBruto.getText().toString());
        Contrato.setMontoCuenta(binding.editMontoCuenta.getText().toString());
        Contrato.setNoContratosMC(binding.editNoContratosVenta.getText().toString());

        Contrato.setPrecioNeto(binding.editPrecioNeto.getText().toString());
        Contrato.setTipoPago(binding.btnContado.isChecked() ? "Contado" : "Financiado");
        Contrato.setEnganchePorcentaje(binding.editEnganchePorcentaje.getText().toString());
        Contrato.setEngancheTotal(binding.editEngancheMonto.getText().toString());
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

        // guarda contratos dinamicos
        List<String> Contratos = new ArrayList<>();
        for (int i = 0; i < binding.containerContratosDinamicos.getChildCount(); i++) {
            View v = binding.containerContratosDinamicos.getChildAt(i);
            if (v instanceof EditText) Contratos.add(((EditText) v).getText().toString());
        }
        Contrato.setContratosMontoCuenta(Contratos);

        // guarda descuentos dinamicos
        List<ContratoModelo.DescuentoDetalle> discounts = new ArrayList<>();

        for (int i = 0; i < binding.containerDescuentosDinamicos.getChildCount(); i++) {
            View row = binding.containerDescuentosDinamicos.getChildAt(i);

            if (row instanceof LinearLayout) {
                String m = ((EditText) ((LinearLayout) row)
                        .getChildAt(0))
                        .getText()
                        .toString();

                String d = ((EditText) ((LinearLayout) row)
                        .getChildAt(1))
                        .getText()
                        .toString();

                if (!m.isEmpty() || !d.isEmpty()) {
                    discounts.add(
                            new ContratoModelo.DescuentoDetalle(m, d)
                    );
                }
            }
        }

        Contrato.setDescuentosDetalle(discounts);

        // guarda pagos diferidos
        List<ContratoModelo.PagoDiferido> deferredPayments = new ArrayList<>();
// El container tiene pares de filas: rowMontos (idx par) y rowFechas (idx impar)
        for (int i = 0; i < binding.containerPagosDinamicos.getChildCount(); i += 2) {
            View rowMontos = binding.containerPagosDinamicos.getChildAt(i);
            View rowFechas = (i + 1 < binding.containerPagosDinamicos.getChildCount())
                    ? binding.containerPagosDinamicos.getChildAt(i + 1) : null;

            if (rowMontos instanceof LinearLayout) {
                LinearLayout lm = (LinearLayout) rowMontos;
                for (int col = 0; col < lm.getChildCount(); col++) {
                    View childM = lm.getChildAt(col);
                    if (!(childM instanceof EditText)) continue;

                    String monto = ((EditText) childM).getText().toString();
                    String fecha = "";

                    if (rowFechas instanceof LinearLayout) {
                        View childF = ((LinearLayout) rowFechas).getChildAt(col);
                        if (childF instanceof TextInputLayout) {
                            EditText etF = (EditText) ((TextInputLayout) childF).getEditText();
                            if (etF != null) fecha = etF.getText().toString();
                        }
                    }

                    if (!monto.isEmpty() || !fecha.isEmpty()) {
                        deferredPayments.add(new ContratoModelo.PagoDiferido(monto, fecha));
                    }
                }
            }
        }
        Contrato.setPagosDiferidos(deferredPayments);

        // Actualizar última fecha de enganche diferido y resetear fecha primer pago si cambió
        if (!deferredPayments.isEmpty()) {
            String newUltima = deferredPayments.get(deferredPayments.size() - 1).fecha;
            String oldUltima = Contrato.getUltimaFechaEnganche();

            if (newUltima != null && !newUltima.equals(oldUltima)) {
                Contrato.setUltimaFechaEnganche(newUltima);
                Contrato.setFechaPrimerPago(null); // Esto fuerza el recalculo en FinanciamientoFragment
            }
        } else if (Contrato.getUltimaFechaEnganche() != null) {
            Contrato.setUltimaFechaEnganche(null);
            Contrato.setFechaPrimerPago(null);
        }

        viewModel.setContrato(Contrato);
    }

    private void limpiarInventario() {
        binding.editNoAnios.setText("");
        binding.spUnidad.setSelection(0);
        binding.spTemporada.setSelection(0);
        binding.spTipoOcupacion.setSelection(0);
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
    //agrega comas a los montos
}
