package com.example.contrato;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.contrato.databinding.ActivityHistorialContratoBinding;
import java.text.SimpleDateFormat;
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
        contrato = (ContratoModelo) getIntent().getSerializableExtra("contract");

        configurarSpinnerPais();

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

    private void configurarSpinnerPais() {
        String[] paises = {"México", "USA Standard", "USA P.O. Box", "USA CMR/APO", "Canadá", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPais.setAdapter(adapter);

        binding.spinnerPais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cambiarFormatoDireccion(paises[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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

        llenarContenedorPersonaas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonaas(binding.containerBeneficiarios, contrato.getBeneficiarios());
        llenarContenedorTelefonos(contrato.getTelefonos());
        llenarContenedorEmails(contrato.getEmails());
        llenarContenedorRedes(contrato.getRedesSociales());
        llenarContenedorRegalos(contrato.getRegalos());
        llenarContenedorContratosMontoCuenta(contrato.getContratosMontoCuenta());
        llenarContenedorPagosDiferidos(contrato.getPagosDiferidos());
        llenarContenedorDescuentos(contrato.getDescuentosDetalle());
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

        binding.editIdioma.setText(contrato.getIdioma());

        String paisContrato = contrato.getPais() != null ? contrato.getPais() : "México";
        int pos = -1;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) binding.spinnerPais.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && item.toString().equalsIgnoreCase(paisContrato)) {
                pos = i;
                break;
            }
        }
        binding.spinnerPais.setSelection(Math.max(pos, 0));
        binding.editNacionalidad.setText(contrato.getProvince());

        llenarContenedorPersonaas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonaas(binding.containerBeneficiarios, contrato.getBeneficiarios());
        llenarContenedorTelefonos(contrato.getTelefonos());
        llenarContenedorEmails(contrato.getEmails());
        binding.checkNoCorreo.setChecked(contrato.isNoCorreo());

        binding.checkNoRedes.setChecked(contrato.isNoRedesSociales());
        llenarContenedorRedes(contrato.getRedesSociales());

        binding.editTipoVenta.setText(contrato.getTipoVenta());
        binding.editUnidad.setText(contrato.getUnidad());
        binding.editTemporada.setText(contrato.getTemporada());
        binding.editMoneda.setText(contrato.getMoneda());
        binding.editTipoCambio.setText(contrato.getTipoCambio());
        binding.editPrecioBruto.setText(contrato.getPrecioBruto());
        binding.editPrecioNeto.setText(contrato.getPrecioNeto());
        binding.editMontoCuenta.setText(contrato.getMontoCuenta());
        binding.editTipoPago.setText(contrato.getTipoPago());

        llenarContenedorContratosMontoCuenta(contrato.getContratosMontoCuenta());

        binding.editEngancheTotal.setText(contrato.getEngancheTotal());
        binding.editEngancheMonto.setText(contrato.getEngancheMonto());
        binding.editEnganchePorcentaje.setText(contrato.getEnganchePorcentaje());
        binding.editEngancheSala.setText(contrato.getEngancheSalaMonto());
        binding.editEngancheSalaPorcentaje.setText(contrato.getEngancheSalaPorcentaje());
        binding.editEngDiferidoMonto.setText(contrato.getEngDiferidoMonto());
        binding.editNoPagosEng.setText(contrato.getNoPagosEng());

        llenarContenedorPagosDiferidos(contrato.getPagosDiferidos());

        binding.editVariosMonto.setText(contrato.getVariosMonto());
        binding.editSaldoEnganche.setText(contrato.getSaldoEnganche());

        binding.editNoDesc.setText(contrato.getNoDesc());
        llenarContenedorDescuentos(contrato.getDescuentosDetalle());

        binding.editCostoContrato.setText(contrato.getCostoContrato());
        binding.editPagoSala.setText(contrato.getPagoSala());
        binding.editCostoMembresia.setText(contrato.getCostoMembresia());
        binding.editComentarios.setText(contrato.getComentarios());

        llenarContenedorRegalos(contrato.getRegalos());

        binding.editMontoFinanciar.setText(contrato.getMontoFinanciar());
        binding.editNumPagos.setText(contrato.getNumPagos());
        binding.editTasa.setText(contrato.getTasaInteres());
        binding.editTipoPeriodo.setText(contrato.getTipoPeriodo());
        binding.editAnioUso.setText(contrato.getAnioUso());
        binding.editNoAnios.setText(contrato.getNoAnios());
        binding.editFechaPrimerPago.setText(contrato.getFechaPrimerPago());
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
        ((TextView)fila.findViewById(R.id.textParentesco)).setText(p.parentesco);
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

    private void llenarContenedorContratosMontoCuenta(List<String> contratos) {
        binding.containerContratosMontoCuenta.removeAllViews();
        if (contratos == null) return;
        for (String c : contratos) {
            TextView tv = new TextView(this);
            tv.setText(c);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setPadding(10, 5, 10, 5);
            binding.containerContratosMontoCuenta.addView(tv);
        }
    }

    private void llenarContenedorPagosDiferidos(List<ContratoModelo.PagoDiferido> pagos) {
        binding.containerPagosDiferidos.removeAllViews();
        if (pagos == null) return;
        for (ContratoModelo.PagoDiferido p : pagos) {
            TextView tv = new TextView(this);
            tv.setText(String.format(Locale.getDefault(), "%s - %s", p.monto, p.fecha));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setPadding(10, 5, 10, 5);
            binding.containerPagosDiferidos.addView(tv);
        }
    }

    private void llenarContenedorDescuentos(List<ContratoModelo.DescuentoDetalle> descuentos) {
        binding.containerDescuentos.removeAllViews();
        if (descuentos == null) return;
        for (ContratoModelo.DescuentoDetalle d : descuentos) {
            TextView tv = new TextView(this);
            tv.setText(String.format(Locale.getDefault(), "%s: %s", d.descripcion, d.monto));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setPadding(10, 5, 10, 5);
            binding.containerDescuentos.addView(tv);
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
        EditText editPar = view.findViewById(R.id.editParentesco);

        editNom.setText(p.nombre);
        editPat.setText(p.paterno);
        editMat.setText(p.materno);
        editOcu.setText(p.ocupacion);
        editCum.setText(p.cumple);
        editPar.setText(p.parentesco);

        builder.setView(view);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            p.nombre = editNom.getText().toString();
            p.paterno = editPat.getText().toString();
            p.materno = editMat.getText().toString();
            p.ocupacion = editOcu.getText().toString();
            p.cumple = editCum.getText().toString();
            p.parentesco = editPar.getText().toString();
            actualizarFilaPersonaa(fila, p);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void guardarCambios() {
        if (contrato == null) return;
        contrato.setIdioma(binding.editIdioma.getText().toString());
        contrato.setPais(binding.spinnerPais.getSelectedItem().toString());
        contrato.setProvince(binding.editNacionalidad.getText().toString());
        contrato.setModifiedDate(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));

        guardarDatosDireccion();

        contrato.setNoCorreo(binding.checkNoCorreo.isChecked());
        contrato.setNoRedesSociales(binding.checkNoRedes.isChecked());

        contrato.setTipoVenta(binding.editTipoVenta.getText().toString());
        contrato.setUnidad(binding.editUnidad.getText().toString());
        contrato.setTemporada(binding.editTemporada.getText().toString());
        contrato.setMoneda(binding.editMoneda.getText().toString());
        contrato.setTipoCambio(binding.editTipoCambio.getText().toString());
        contrato.setPrecioBruto(binding.editPrecioBruto.getText().toString());
        contrato.setPrecioNeto(binding.editPrecioNeto.getText().toString());
        contrato.setMontoCuenta(binding.editMontoCuenta.getText().toString());
        contrato.setTipoPago(binding.editTipoPago.getText().toString());

        contrato.setEngancheTotal(binding.editEngancheTotal.getText().toString());
        contrato.setEngancheMonto(binding.editEngancheMonto.getText().toString());
        contrato.setEnganchePorcentaje(binding.editEnganchePorcentaje.getText().toString());
        contrato.setEngancheSalaMonto(binding.editEngancheSala.getText().toString());
        contrato.setEngancheSalaPorcentaje(binding.editEngancheSalaPorcentaje.getText().toString());
        contrato.setEngDiferidoMonto(binding.editEngDiferidoMonto.getText().toString());
        contrato.setNoPagosEng(binding.editNoPagosEng.getText().toString());

        contrato.setVariosMonto(binding.editVariosMonto.getText().toString());
        contrato.setSaldoEnganche(binding.editSaldoEnganche.getText().toString());

        contrato.setNoDesc(binding.editNoDesc.getText().toString());

        contrato.setCostoContrato(binding.editCostoContrato.getText().toString());
        contrato.setPagoSala(binding.editPagoSala.getText().toString());
        contrato.setCostoMembresia(binding.editCostoMembresia.getText().toString());
        contrato.setComentarios(binding.editComentarios.getText().toString());
        contrato.setMontoFinanciar(binding.editMontoFinanciar.getText().toString());
        contrato.setNumPagos(binding.editNumPagos.getText().toString());
        contrato.setTasaInteres(binding.editTasa.getText().toString());
        contrato.setTipoPeriodo(binding.editTipoPeriodo.getText().toString());
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
