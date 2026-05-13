package com.example.contrato;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.contrato.databinding.ActivityHistorialContratoBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditaContratoActivity extends AppCompatActivity {

    private ActivityHistorialContratoBinding binding;
    private ContratoModelo contrato;
    private boolean estaEditando = false;
    private SharedContractViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorialContratoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SharedContractViewModel.class);
        
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        viewModel.setCurrentUserId(userId);

        contrato = (ContratoModelo) getIntent().getSerializableExtra("contract");

        configurarSpinners();
        setupDynamicWatchers();

        if (contrato != null) {
            llenarDatos();
        }

        establecerHabilitacionCampos(binding.mainContent, false);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEditToggle.setOnClickListener(v -> alternarModoEdicion());
        binding.btnGuardar.setOnClickListener(v -> guardarCambios());

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                alternarModoEdicion();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarSpinners() {
        // País
        String[] paises = {"México", "USA Standard", "USA P.O. Box", "USA CMR/APO", "Canadá", "Otro"};
        setupSpinner(binding.spinnerPais, paises);
        binding.spinnerPais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cambiarFormatoDireccion(paises[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Idioma
        setupSpinner(binding.spinnerIdioma, new String[]{"Español", "English"});

        // Nacionalidad (Placeholder list, update as needed)
        setupSpinner(binding.spinnerNacionalidad, new String[]{"Mexicana", "Estadounidense", "Canadiense", "Otra"});

        // Tipo Venta
        setupSpinner(binding.spinnerTipoVenta, new String[]{"Nueva", "Upgrade"});

        // Unidad (Placeholder list)
        setupSpinner(binding.spinnerUnidad, new String[]{"Unidad 1", "Unidad 2", "Unidad 3"});

        // Temporada (Placeholder list)
        setupSpinner(binding.spinnerTemporada, new String[]{"Alta", "Baja", "Media"});

        // Moneda
        setupSpinner(binding.spinnerMoneda, new String[]{"MXN", "USD"});

        // Tipo Pago
        setupSpinner(binding.spinnerTipoPago, new String[]{"Financiado", "Contado"});

        // Tipo Periodo
        setupSpinner(binding.spinnerTipoPeriodo, new String[]{"Mensual", "Bimensual", "Trimestral", "Semestral", "Anual"});
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupDynamicWatchers() {
        binding.editNoContratosMontoCta.addTextChangedListener(new DynamicWatcher(binding.containerContratosMontoCuenta, 1));
        binding.editNoPagosEng.addTextChangedListener(new DynamicWatcher(binding.containerPagosDiferidos, 2));
        binding.editNoDesc.addTextChangedListener(new DynamicWatcher(binding.containerDescuentos, 3));
    }

    private class DynamicWatcher implements TextWatcher {
        private ViewGroup container;
        private int type;
        public DynamicWatcher(ViewGroup container, int type) { this.container = container; this.type = type; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!estaEditando) return;
            try {
                int num = Integer.parseInt(s.toString());
                if (num > 15) num = 15;
                rebuildContainer(container, type, num);
            } catch (Exception e) {
                container.removeAllViews();
            }
        }
        @Override public void afterTextChanged(Editable s) {}
    }

    private void rebuildContainer(ViewGroup container, int type, int num) {
        container.removeAllViews();
        for (int i = 0; i < num; i++) {
            if (type == 1) container.addView(createContractEditText(""));
            else if (type == 2) container.addView(createPagoDiferidoRow(null));
            else if (type == 3) container.addView(createDescuentoRow(null));
        }
    }

    private View createContractEditText(String text) {
        EditText et = new EditText(new ContextThemeWrapper(this, R.style.FieldInput), null, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        et.setLayoutParams(params);
        et.setHint("No. Contrato");
        et.setText(text);
        et.setEnabled(estaEditando);
        return et;
    }

    private View createPagoDiferidoRow(ContratoModelo.PagoDiferido p) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));

        EditText etMonto = new EditText(new ContextThemeWrapper(this, R.style.FieldInput), null, 0);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p1.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        etMonto.setLayoutParams(p1);
        etMonto.setHint("Monto");
        etMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText etFecha = new EditText(new ContextThemeWrapper(this, R.style.FieldInput), null, 0);
        etFecha.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f));
        etFecha.setHint("DD/MM/YYYY");
        etFecha.setFocusable(false);
        etFecha.setOnClickListener(v -> { if (estaEditando) showDatePicker(etFecha); });

        if (p != null) { etMonto.setText(p.monto); etFecha.setText(p.fecha); }
        etMonto.setEnabled(estaEditando);
        etFecha.setEnabled(estaEditando);

        row.addView(etMonto);
        row.addView(etFecha);
        return row;
    }

    private View createDescuentoRow(ContratoModelo.DescuentoDetalle d) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));

        EditText etMonto = new EditText(new ContextThemeWrapper(this, R.style.FieldInput), null, 0);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p1.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        etMonto.setLayoutParams(p1);
        etMonto.setHint("Monto");
        etMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        EditText etDesc = new EditText(new ContextThemeWrapper(this, R.style.FieldInput), null, 0);
        etDesc.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
        etDesc.setHint("Descripción");

        if (d != null) { etMonto.setText(d.monto); etDesc.setText(d.descripcion); }
        etMonto.setEnabled(estaEditando);
        etDesc.setEnabled(estaEditando);

        row.addView(etMonto);
        row.addView(etDesc);
        return row;
    }

    private void showDatePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> et.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y)), 
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cambiarFormatoDireccion(String pais) {
        binding.containerDireccionDinamica.removeAllViews();
        int layoutRes;

        switch (pais) {
            case "México": layoutRes = R.layout.historial_domiciliomexico; break;
            case "USA Standard": layoutRes = R.layout.historial_standard_format; break;
            case "USA P.O. Box": layoutRes = R.layout.historial_pobox_format; break;
            case "USA CMR/APO": layoutRes = R.layout.historial_cmr_format; break;
            case "Canadá": layoutRes = R.layout.historial_domiciliocanada; break;
            default: layoutRes = R.layout.historial_domiciliootro; break;
        }

        getLayoutInflater().inflate(layoutRes, binding.containerDireccionDinamica, true);
        establecerHabilitacionCampos(binding.containerDireccionDinamica, estaEditando);
        llenarCamposDireccion(pais);
    }

    private void llenarCamposDireccion(String pais) {
        if (contrato == null) return;
        View v = binding.containerDireccionDinamica;
        switch (pais) {
            case "México":
                setTextIfExist(v, R.id.editMexCalle, contrato.getMexCalle());
                setTextIfExist(v, R.id.editMexNumExt, contrato.getMexNumExt());
                setTextIfExist(v, R.id.editMexNumInt, contrato.getMexNumInt());
                setTextIfExist(v, R.id.editMexColonia, contrato.getMexColonia());
                setTextIfExist(v, R.id.editMexMunicipio, contrato.getMexMunicipio());
                setTextIfExist(v, R.id.editMexCiudad, contrato.getMexCiudad());
                setTextIfExist(v, R.id.editMexEstado, contrato.getMexEstado());
                setTextIfExist(v, R.id.editMexCP, contrato.getMexCP());
                break;
            case "USA Standard":
            case "USA P.O. Box":
            case "USA CMR/APO":
                setTextIfExist(v, R.id.editUsaCalle, contrato.getUsaCalle());
                setTextIfExist(v, R.id.editUsaCity, contrato.getUsaCity());
                setTextIfExist(v, R.id.editUsaState, contrato.getUsaState());
                setTextIfExist(v, R.id.editUsaZip, contrato.getUsaZip());
                setTextIfExist(v, R.id.editUsaNeighborhood, contrato.getUsaNeighborhood());
                setTextIfExist(v, R.id.editUsaPoBox, contrato.getUsaPoBox());
                setTextIfExist(v, R.id.editUsaBox, contrato.getUsaBox());
                setTextIfExist(v, R.id.editUsaCmr, contrato.getUsaCmr());
                setTextIfExist(v, R.id.editUsaApo, contrato.getUsaApo());
                break;
            case "Canadá":
                setTextIfExist(v, R.id.editCanCalle, contrato.getCanCalle());
                setTextIfExist(v, R.id.editCanCity, contrato.getCanCity());
                setTextIfExist(v, R.id.editCanProvince, contrato.getCanProvince());
                setTextIfExist(v, R.id.editCanPostalCode, contrato.getCanPostalCode());
                break;
            default:
                setTextIfExist(v, R.id.editOtroLinea1, contrato.getOtroLinea1());
                setTextIfExist(v, R.id.editOtroLinea2, contrato.getOtroLinea2());
                setTextIfExist(v, R.id.editOtroLinea3, contrato.getOtroLinea3());
                setTextIfExist(v, R.id.editOtroLinea4, contrato.getOtroLinea4());
                setTextIfExist(v, R.id.editOtroLinea5, contrato.getOtroLinea5());
                setTextIfExist(v, R.id.editOtroPais, contrato.getOtroPais());
                break;
        }
    }

    private void setTextIfExist(View container, int id, String text) {
        View v = container.findViewById(id);
        if (v instanceof EditText) {
            ((EditText) v).setText(text != null ? text : "");
        }
    }

    private void alternarModoEdicion() {
        estaEditando = !estaEditando;
        binding.btnEditToggle.setText(estaEditando ? "CANCELAR" : "EDITAR");
        binding.btnGuardar.setVisibility(estaEditando ? View.VISIBLE : View.GONE);
        binding.tvTitle.setText(estaEditando ? "Editando Contrato" : "Detalle del Contrato");

        if (!estaEditando) llenarDatos();
        establecerHabilitacionCampos(binding.mainContent, estaEditando);

        enableGeneratedViews(binding.containerContratosMontoCuenta, estaEditando);
        enableGeneratedViews(binding.containerPagosDiferidos, estaEditando);
        enableGeneratedViews(binding.containerDescuentos, estaEditando);
        
        llenarContenedorPersonaas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonaas(binding.containerBeneficiarios, contrato.getBeneficiarios());
    }

    private void enableGeneratedViews(ViewGroup layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) enableGeneratedViews((ViewGroup) child, enabled);
            else child.setEnabled(enabled);
        }
    }

    private void establecerHabilitacionCampos(ViewGroup layout, boolean habilitado) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View hijo = layout.getChildAt(i);
            if (hijo instanceof EditText || hijo instanceof CheckBox || hijo instanceof Spinner) {
                hijo.setEnabled(habilitado);
                hijo.setAlpha(1.0f);
            } else if (hijo instanceof ViewGroup) {
                establecerHabilitacionCampos((ViewGroup) hijo, habilitado);
            }
        }
    }

    private void llenarDatos() {
        if (contrato == null) return;
        String fechas = String.format("Creado: %s | Modificado: %s", contrato.getCreationDate(), contrato.getModifiedDate());
        binding.tvFechas.setText(fechas);

        setSpinnerSelection(binding.spinnerIdioma, contrato.getIdioma());

        String paisContrato = contrato.getPais() != null ? contrato.getPais() : "México";
        setSpinnerSelection(binding.spinnerPais, paisContrato);
        setSpinnerSelection(binding.spinnerNacionalidad, contrato.getProvince());

        llenarContenedorPersonaas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonaas(binding.containerBeneficiarios, contrato.getBeneficiarios());
        llenarContenedorTelefonos(contrato.getTelefonos());
        llenarContenedorEmails(contrato.getEmails());
        binding.checkNoCorreo.setChecked(contrato.isNoCorreo());

        binding.checkNoRedes.setChecked(contrato.isNoRedesSociales());
        llenarContenedorRedes(contrato.getRedesSociales());

        setSpinnerSelection(binding.spinnerTipoVenta, contrato.getTipoVenta());
        setSpinnerSelection(binding.spinnerUnidad, contrato.getUnidad());
        setSpinnerSelection(binding.spinnerTemporada, contrato.getTemporada());
        setSpinnerSelection(binding.spinnerMoneda, contrato.getMoneda());
        binding.editTipoCambio.setText(contrato.getTipoCambio());
        binding.editPrecioBruto.setText(contrato.getPrecioBruto());
        binding.editPrecioNeto.setText(contrato.getPrecioNeto());
        binding.editMontoCuenta.setText(contrato.getMontoCuenta());
        setSpinnerSelection(binding.spinnerTipoPago, contrato.getTipoPago());

        binding.editNoContratosMontoCta.setText(String.valueOf(contrato.getContratosMontoCuenta().size()));
        binding.containerContratosMontoCuenta.removeAllViews();
        for (String c : contrato.getContratosMontoCuenta()) {
            binding.containerContratosMontoCuenta.addView(createContractEditText(c));
        }

        binding.editEngancheTotal.setText(contrato.getEngancheTotal());
        binding.editEngancheMonto.setText(contrato.getEngancheMonto());
        binding.editEnganchePorcentaje.setText(contrato.getEnganchePorcentaje());
        binding.editEngancheSala.setText(contrato.getEngancheSalaMonto());
        binding.editEngancheSalaPorcentaje.setText(contrato.getEngancheSalaPorcentaje());
        binding.editEngDiferidoMonto.setText(contrato.getEngDiferidoMonto());
        binding.editNoPagosEng.setText(contrato.getNoPagosEng());

        binding.containerPagosDiferidos.removeAllViews();
        for (ContratoModelo.PagoDiferido p : contrato.getPagosDiferidos()) {
            binding.containerPagosDiferidos.addView(createPagoDiferidoRow(p));
        }

        binding.editVariosMonto.setText(contrato.getVariosMonto());
        binding.editSaldoEnganche.setText(contrato.getSaldoEnganche());

        binding.editNoDesc.setText(contrato.getNoDesc());
        binding.containerDescuentos.removeAllViews();
        for (ContratoModelo.DescuentoDetalle d : contrato.getDescuentosDetalle()) {
            binding.containerDescuentos.addView(createDescuentoRow(d));
        }

        binding.editCostoContrato.setText(contrato.getCostoContrato());
        binding.editPagoSala.setText(contrato.getPagoSala());
        binding.editCostoMembresia.setText(contrato.getCostoMembresia());
        binding.editComentarios.setText(contrato.getComentarios());

        llenarContenedorRegalos(contrato.getRegalos());

        binding.editMontoFinanciar.setText(contrato.getMontoFinanciar());
        binding.editNumPagos.setText(contrato.getNumPagos());
        binding.editTasa.setText(contrato.getTasaInteres());
        setSpinnerSelection(binding.spinnerTipoPeriodo, contrato.getTipoPeriodo());
        binding.editAnioUso.setText(contrato.getAnioUso());
        binding.editNoAnios.setText(contrato.getNoAnios());
        binding.editFechaPrimerPago.setText(contrato.getFechaPrimerPago());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void llenarContenedorPersonaas(ViewGroup contenedor, List<ContratoModelo.Persona> Personaas) {
        contenedor.removeAllViews();
        if (Personaas == null) return;
        for (ContratoModelo.Persona p : Personaas) {
            View fila = LayoutInflater.from(this).inflate(R.layout.list_item_person_historial, contenedor, false);
            actualizarFilaPersonaa(fila, p);

            View btnEditar = fila.findViewById(R.id.btnEditar);
            View btnEliminar = fila.findViewById(R.id.btnEliminar);

            btnEditar.setVisibility(estaEditando ? View.VISIBLE : View.GONE);
            btnEliminar.setVisibility(estaEditando ? View.VISIBLE : View.GONE);

            btnEditar.setOnClickListener(v -> mostrarDialogoEditarPersonaa(p, fila));
            btnEliminar.setOnClickListener(v -> {
                Personaas.remove(p);
                contenedor.removeView(fila);
            });
            contenedor.addView(fila);
        }
    }

    private void actualizarFilaPersonaa(View fila, ContratoModelo.Persona p) {
        String fullName = p.nombre + " " + (p.paterno != null ? p.paterno : "") + " " + (p.materno != null ? p.materno : "");
        ((TextView)fila.findViewById(R.id.textNombre)).setText(fullName.trim());
        ((TextView)fila.findViewById(R.id.textCumple)).setText(p.cumple);
        ((TextView)fila.findViewById(R.id.textOcupacion)).setText(p.ocupacion);
        
        String parentescoDisplay = p.parentesco;
        try {
            int pos = Integer.parseInt(p.parentesco);
            String[] array = getResources().getStringArray(R.array.parentescos);
            if (pos >= 0 && pos < array.length) parentescoDisplay = array[pos];
        } catch (Exception ignored) {}
        ((TextView)fila.findViewById(R.id.textParentesco)).setText(parentescoDisplay);
    }

    private void llenarContenedorTelefonos(List<ContratoModelo.InfoTelefono> telefonos) {
        binding.containerTelefonos.removeAllViews();
        if (telefonos == null) return;
        for (ContratoModelo.InfoTelefono t : telefonos) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_historial_telefono, binding.containerTelefonos, false);
            ((TextView)item.findViewById(R.id.tvEtiqueta)).setText(t.etiqueta);
            ((TextView)item.findViewById(R.id.tvNumeroCompleto)).setText(String.format("(+%s) %s", t.lada, t.numero));
            ((TextView)item.findViewById(R.id.tvWhatsapp)).setText(String.format("WhatsApp: %s", t.isWhatsApp ? "Si" : "No"));
            ((TextView)item.findViewById(R.id.tvWhatsapp)).setTextColor(t.isWhatsApp ? 0xFF4CAF50 : 0xFF100F0F);

            View tvPrincipal = item.findViewById(R.id.tvPrincipal);
            tvPrincipal.setVisibility(t.isPrincipal ? View.VISIBLE : View.INVISIBLE);

            binding.containerTelefonos.addView(item);
        }
    }

    private void llenarContenedorEmails(List<String> emails) {
        binding.containerEmails.removeAllViews();
        if (emails == null) return;
        for (String email : emails) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_historial_email, binding.containerEmails, false);
            ((TextView)item.findViewById(R.id.tvEmail)).setText(email);
            binding.containerEmails.addView(item);
        }
    }

    private void llenarContenedorRedes(List<ContratoModelo.SocialAccount> redes) {
        binding.containerRedes.removeAllViews();
        if (redes == null) return;
        for (ContratoModelo.SocialAccount sa : redes) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_cuenta_social_historial, binding.containerRedes, false);
            ((TextView)item.findViewById(R.id.tvPlatformTag)).setText(sa.red);
            ((TextView)item.findViewById(R.id.tvNombre)).setText(sa.usuario);

            ImageView ivIcon = item.findViewById(R.id.ivPlatformIcon);
            if (sa.red != null) {
                String platform = sa.red.toLowerCase();
                if (platform.contains("facebook")) ivIcon.setImageResource(R.drawable.ic_facebook);
                else if (platform.contains("instagram")) ivIcon.setImageResource(R.drawable.ic_instagram);
                else if (platform.contains("twitter") || platform.contains(" x ")) ivIcon.setImageResource(R.drawable.ic_x_twitter);
                else ivIcon.setImageResource(R.drawable.ic_world);
            }

            item.findViewById(R.id.btnEliminar).setVisibility(View.GONE);
            binding.containerRedes.addView(item);
        }
    }

    private void llenarContenedorRegalos(List<String> regalos) {
        binding.containerRegalos.removeAllViews();
        if (regalos == null) return;
        int count = 1;
        for (String r : regalos) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_regalos, binding.containerRegalos, false);
            ((TextView)item.findViewById(R.id.noRegalo)).setText(String.valueOf(count++));
            ((TextView)item.findViewById(R.id.tvNombre)).setText(r);
            item.findViewById(R.id.btnBorrarRegalo).setVisibility(View.GONE);
            binding.containerRegalos.addView(item);
        }
    }

    private void mostrarDialogoEditarPersonaa(ContratoModelo.Persona p, View fila) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Personaa");

        View view = getLayoutInflater().inflate(R.layout.dialog_editar_persona, null);
        EditText editNom = view.findViewById(R.id.editNombre);
        EditText editPat = view.findViewById(R.id.editPaterno);
        EditText editMat = view.findViewById(R.id.editMaterno);
        EditText editOcu = view.findViewById(R.id.editOcupacion);
        EditText editCum = view.findViewById(R.id.editCumple);
        Spinner spinnerPar = view.findViewById(R.id.spinnerParentesco);

        String[] parentescos = getResources().getStringArray(R.array.parentescos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parentescos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPar.setAdapter(adapter);

        editNom.setText(p.nombre);
        editPat.setText(p.paterno);
        editMat.setText(p.materno);
        editOcu.setText(p.ocupacion);
        editCum.setText(p.cumple);
        
        try {
            int pos = Integer.parseInt(p.parentesco);
            if (pos >= 0 && pos < adapter.getCount()) spinnerPar.setSelection(pos);
        } catch (Exception e) {
            spinnerPar.setSelection(adapter.getCount() - 1);
        }

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            p.nombre = editNom.getText().toString();
            p.paterno = editPat.getText().toString();
            p.materno = editMat.getText().toString();
            p.ocupacion = editOcu.getText().toString();
            p.cumple = editCum.getText().toString();
            p.parentesco = String.valueOf(spinnerPar.getSelectedItemPosition());
            actualizarFilaPersonaa(fila, p);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void guardarCambios() {
        if (contrato == null) return;
        contrato.setIdioma(binding.spinnerIdioma.getSelectedItem().toString());
        contrato.setPais(binding.spinnerPais.getSelectedItem().toString());
        contrato.setProvince(binding.spinnerNacionalidad.getSelectedItem().toString());
        contrato.setModifiedDate(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));

        guardarDatosDireccion();

        contrato.setNoCorreo(binding.checkNoCorreo.isChecked());
        contrato.setNoRedesSociales(binding.checkNoRedes.isChecked());

        contrato.setTipoVenta(binding.spinnerTipoVenta.getSelectedItem().toString());
        contrato.setUnidad(binding.spinnerUnidad.getSelectedItem().toString());
        contrato.setTemporada(binding.spinnerTemporada.getSelectedItem().toString());
        contrato.setMoneda(binding.spinnerMoneda.getSelectedItem().toString());
        contrato.setTipoCambio(binding.editTipoCambio.getText().toString());
        contrato.setPrecioBruto(binding.editPrecioBruto.getText().toString());
        contrato.setPrecioNeto(binding.editPrecioNeto.getText().toString());
        contrato.setMontoCuenta(binding.editMontoCuenta.getText().toString());
        contrato.setTipoPago(binding.spinnerTipoPago.getSelectedItem().toString());

        List<String> contracts = new ArrayList<>();
        for (int i = 0; i < binding.containerContratosMontoCuenta.getChildCount(); i++) {
            View v = binding.containerContratosMontoCuenta.getChildAt(i);
            if (v instanceof EditText) contracts.add(((EditText) v).getText().toString());
        }
        contrato.setContratosMontoCuenta(contracts);

        contrato.setEngancheTotal(binding.editEngancheTotal.getText().toString());
        contrato.setEngancheMonto(binding.editEngancheMonto.getText().toString());
        contrato.setEnganchePorcentaje(binding.editEnganchePorcentaje.getText().toString());
        contrato.setEngancheSalaMonto(binding.editEngancheSala.getText().toString());
        contrato.setEngancheSalaPorcentaje(binding.editEngancheSalaPorcentaje.getText().toString());
        contrato.setEngDiferidoMonto(binding.editEngDiferidoMonto.getText().toString());
        contrato.setNoPagosEng(binding.editNoPagosEng.getText().toString());

        List<ContratoModelo.PagoDiferido> payments = new ArrayList<>();
        for (int i = 0; i < binding.containerPagosDiferidos.getChildCount(); i++) {
            View row = binding.containerPagosDiferidos.getChildAt(i);
            if (row instanceof LinearLayout) {
                String m = ((EditText)((LinearLayout)row).getChildAt(0)).getText().toString();
                String f = ((EditText)((LinearLayout)row).getChildAt(1)).getText().toString();
                payments.add(new ContratoModelo.PagoDiferido(m, f));
            }
        }
        contrato.setPagosDiferidos(payments);

        contrato.setVariosMonto(binding.editVariosMonto.getText().toString());
        contrato.setSaldoEnganche(binding.editSaldoEnganche.getText().toString());
        contrato.setNoDesc(binding.editNoDesc.getText().toString());

        List<ContratoModelo.DescuentoDetalle> discounts = new ArrayList<>();
        for (int i = 0; i < binding.containerDescuentos.getChildCount(); i++) {
            View row = binding.containerDescuentos.getChildAt(i);
            if (row instanceof LinearLayout) {
                String m = ((EditText)((LinearLayout)row).getChildAt(0)).getText().toString();
                String d = ((EditText)((LinearLayout)row).getChildAt(1)).getText().toString();
                discounts.add(new ContratoModelo.DescuentoDetalle(m, d));
            }
        }
        contrato.setDescuentosDetalle(discounts);

        contrato.setCostoContrato(binding.editCostoContrato.getText().toString());
        contrato.setPagoSala(binding.editPagoSala.getText().toString());
        contrato.setCostoMembresia(binding.editCostoMembresia.getText().toString());
        contrato.setComentarios(binding.editComentarios.getText().toString());
        contrato.setMontoFinanciar(binding.editMontoFinanciar.getText().toString());
        contrato.setNumPagos(binding.editNumPagos.getText().toString());
        contrato.setTasaInteres(binding.editTasa.getText().toString());
        contrato.setTipoPeriodo(binding.spinnerTipoPeriodo.getSelectedItem().toString());
        contrato.setAnioUso(binding.editAnioUso.getText().toString());
        contrato.setNoAnios(binding.editNoAnios.getText().toString());
        contrato.setFechaPrimerPago(binding.editFechaPrimerPago.getText().toString());

        viewModel.actualizaContratoInDatabase(contrato);
    }

    private void guardarDatosDireccion() {
        View v = binding.containerDireccionDinamica;
        String pais = binding.spinnerPais.getSelectedItem().toString();
        switch (pais) {
            case "México":
                contrato.setMexCalle(getETString(v, R.id.editMexCalle));
                contrato.setMexNumExt(getETString(v, R.id.editMexNumExt));
                contrato.setMexNumInt(getETString(v, R.id.editMexNumInt));
                contrato.setMexColonia(getETString(v, R.id.editMexColonia));
                contrato.setMexMunicipio(getETString(v, R.id.editMexMunicipio));
                contrato.setMexCiudad(getETString(v, R.id.editMexCiudad));
                contrato.setMexEstado(getETString(v, R.id.editMexEstado));
                contrato.setMexCP(getETString(v, R.id.editMexCP));
                break;
            case "USA Standard":
            case "USA P.O. Box":
            case "USA CMR/APO":
                contrato.setUsaCalle(getETString(v, R.id.editUsaCalle));
                contrato.setUsaCity(getETString(v, R.id.editUsaCity));
                contrato.setUsaState(getETString(v, R.id.editUsaState));
                contrato.setUsaZip(getETString(v, R.id.editUsaZip));
                contrato.setUsaNeighborhood(getETString(v, R.id.editUsaNeighborhood));
                contrato.setUsaPoBox(getETString(v, R.id.editUsaPoBox));
                contrato.setUsaBox(getETString(v, R.id.editUsaBox));
                contrato.setUsaCmr(getETString(v, R.id.editUsaCmr));
                contrato.setUsaApo(getETString(v, R.id.editUsaApo));
                break;
            case "Canadá":
                contrato.setCanCalle(getETString(v, R.id.editCanCalle));
                contrato.setCanCity(getETString(v, R.id.editCanCity));
                contrato.setCanProvince(getETString(v, R.id.editCanProvince));
                contrato.setCanPostalCode(getETString(v, R.id.editCanPostalCode));
                break;
            default:
                contrato.setOtroLinea1(getETString(v, R.id.editOtroLinea1));
                contrato.setOtroLinea2(getETString(v, R.id.editOtroLinea2));
                contrato.setOtroLinea3(getETString(v, R.id.editOtroLinea3));
                contrato.setOtroLinea4(getETString(v, R.id.editOtroLinea4));
                contrato.setOtroLinea5(getETString(v, R.id.editOtroLinea5));
                contrato.setOtroPais(getETString(v, R.id.editOtroPais));
                break;
        }
    }

    private String getETString(View container, int id) {
        View v = container.findViewById(id);
        if (v instanceof EditText) return ((EditText) v).getText().toString();
        return "";
    }
}
