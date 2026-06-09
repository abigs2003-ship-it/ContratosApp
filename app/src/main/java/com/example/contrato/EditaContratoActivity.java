package com.example.contrato;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.contrato.databinding.ActivityHistorialContratoBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EditaContratoActivity extends AppCompatActivity {

    private ActivityHistorialContratoBinding binding;
    private ContratoModelo contrato;
    private SharedContratoViewModel viewModel;
    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorialContratoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SharedContratoViewModel.class);

        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        long userId = prefs.getLong("userId", -1);
        viewModel.setCurrentUserId(userId);

        contrato = (ContratoModelo) getIntent().getSerializableExtra("contrato");

        if (contrato != null) {
            llenarDatos();
        }

        binding.btnBack.setOnClickListener(v -> finish());
        

        binding.btnEditToggle.setVisibility(View.GONE);
        binding.btnGuardar.setVisibility(View.GONE);
    }

    private void llenarDatos() {
        if (contrato == null) return;
        String creacion = String.format("Creado: %s", convertirMesANombre(contrato.getFechaCreacion()));
        String modificacion = String.format("| Modificado: %s", convertirMesANombre(contrato.getFechaModificacion()));
        binding.tvCreacion.setText(creacion);
        if(contrato.getFechaModificacion() == null){
            binding.tvModificado.setText("");
        }else{
            binding.tvModificado.setText(modificacion);
        }


        binding.tvIdioma.setText(contrato.getIdioma());
        llenarContenedorPersonas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonas(binding.containerBeneficiarios, contrato.getBeneficiarios());

        // Dirección
        binding.tvPais.setText(contrato.getPais());
        binding.tvNacionalidad.setText(contrato.getNacionalidad());
        llenarDireccionDinamica();

        // Contacto
        binding.containerTelefonos.removeAllViews();
        if (contrato.getTelefonos() != null) {
            for (ContratoModelo.InfoTelefono t : contrato.getTelefonos()) {
                binding.containerTelefonos.addView(createTelefonoView(t));
            }
        }

        binding.containerEmails.removeAllViews();
        if (contrato.getEmails() != null) {
            for (String email : contrato.getEmails()) {
                binding.containerEmails.addView(createEmailView(email));
            }
        }

        if(!contrato.isNoCorreo()){
            binding.checkNoCorreo.setVisibility(View.GONE);
        }else {
            binding.checkNoCorreo.setVisibility(View.VISIBLE);
            binding.checkNoCorreo.setChecked(contrato.isNoCorreo());
        }

        if(!contrato.isNoRedesSociales()){
            binding.checkNoRedes.setVisibility(View.GONE);
        }else{
            binding.checkNoRedes.setVisibility(View.VISIBLE);
            binding.checkNoRedes.setChecked(contrato.isNoRedesSociales());

        }
        binding.containerRedes.removeAllViews();
        if (contrato.getRedesSociales() != null) {
            for (ContratoModelo.CuentaRed sa : contrato.getRedesSociales()) {
                binding.containerRedes.addView(createCuentaRedView(sa));
            }
        }

        // Datos Venta
        binding.tvTipoVenta.setText(contrato.getTipoVenta());
        binding.tvAniosUso.setText(contrato.getNoAnios());
        binding.tvTipoOcupacion.setText(contrato.getTipoOcupacion());
        binding.tvAnioPrimerUso.setText(contrato.getAnioUso());
        binding.tvUnidad.setText(contrato.getUnidad());
        binding.tvTemporada.setText(contrato.getTemporada());
        binding.tvMoneda.setText(contrato.getMoneda());
        binding.tvTipoCambio.setText(contrato.getTipoCambio());
        binding.tvPrecioBruto.setText(contrato.getPrecioBruto());
        binding.tvPrecioNeto.setText(contrato.getPrecioNeto());
        binding.tvMontoCuenta.setText(contrato.getMontoCuenta());
        binding.tvTipoPago.setText(contrato.getTipoPago());

        binding.tvNoContratosMontoCta.setText(contrato.getNoContratosMC());
        binding.containerContratosMontoCuenta.removeAllViews();
        for (String c : contrato.getContratosMontoCuenta()) {
            String etiqueta = "Contrato " + (binding.containerContratosMontoCuenta.getChildCount() + 1);
            String textoCompleto = etiqueta + ": " + c;

            SpannableString spannable = new SpannableString(textoCompleto);

            spannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    0,
                    etiqueta.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            TextView tv = creaTextViewFila("");
            tv.setText(spannable);

            binding.containerContratosMontoCuenta.addView(tv);        }


        binding.tvEngancheMonto.setText(contrato.getEngancheTotal());
        binding.tvEnganchePorcentaje.setText(contrato.getEnganchePorcentaje());
        binding.tvEngancheSala.setText(contrato.getEngancheSalaMonto());
        binding.tvEngancheSalaPorcentaje.setText(contrato.getEngancheSalaPorcentaje());
        binding.tvEngDiferidoMonto.setText(contrato.getEngDiferidoMonto());
        binding.tvNoPagosEng.setText(contrato.getNoPagosEng());

        binding.containerPagosDiferidos.removeAllViews();
        for (ContratoModelo.PagoDiferido p : contrato.getPagosDiferidos()) {
            binding.containerPagosDiferidos.addView(createPagoDiferidoRow(p));
        }

        binding.tvVariosMonto.setText(contrato.getVariosMonto());
        binding.tvSaldoEnganche.setText(contrato.getSaldoEnganche());

        binding.tvNoDesc.setText(contrato.getNoDesc());
        binding.containerDescuentos.removeAllViews();
        for (ContratoModelo.DescuentoDetalle d : contrato.getDescuentosDetalle()) {
            String numDescuento = String.valueOf(binding.containerDescuentos.getChildCount() + 1);
            binding.containerDescuentos.addView(creaFilaDescuentos(d, numDescuento));
        }


        binding.tvCostoContrato.setText(contrato.getCostoContrato());
        binding.tvPagoSala.setText(contrato.getPagoSala());
        binding.tvCostoMembresia.setText(contrato.getCostoMembresia());
        binding.tvComentarios.setText(contrato.getComentarios());

        binding.containerRegalos.removeAllViews();
        if (contrato.getRegalos() != null) {
            for (String r : contrato.getRegalos()) {
                binding.containerRegalos.addView(createRegaloView(r));
            }
        }

        // Financiamiento
        binding.tvMontoFinanciar.setText(contrato.getMontoFinanciar());
        binding.tvNumPagos.setText(contrato.getNumPagos());
        binding.tvTasa.setText(contrato.getTasaInteres());
        binding.tvTipoPeriodo.setText(contrato.getTipoPeriodo());

        binding.tvFechaPrimerPago.setText(contrato.getFechaPrimerPago());
    }

    private void llenarDireccionDinamica() {
        binding.containerDireccionDinamica.removeAllViews();
        String tipoDir = contrato.getTipoDir();
        if (tipoDir == null) return;

        View viewDir = null;
        LayoutInflater inflater = getLayoutInflater();

        switch (tipoDir) {
            case "MEX":
                viewDir = inflater.inflate(R.layout.historial_domiciliomexico, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvMexCalle, contrato.getMexCalle());
                setText(viewDir, R.id.tvMexNumExt, contrato.getMexNumExt());
                setText(viewDir, R.id.tvMexNumInt, contrato.getMexNumInt());
                setText(viewDir, R.id.tvMexColonia, contrato.getMexColonia());
                setText(viewDir, R.id.tvMexMunicipio, contrato.getDelegacion());
                setText(viewDir, R.id.tvMexEstado, contrato.getMexEstado());
                setText(viewDir, R.id.tvMexCiudad, contrato.getMexCiudad());
                setText(viewDir, R.id.tvMexCP, contrato.getMexCP());
                break;
            case "CAN":
                viewDir = inflater.inflate(R.layout.historial_domiciliocanada, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvCanCalle, contrato.getCanCalle());
                setText(viewDir, R.id.tvCanCity, contrato.getCanCity());
                setText(viewDir, R.id.tvCanProvince, contrato.getCanProvince());
                setText(viewDir, R.id.tvCanPostalCode, contrato.getCanPostalCode());
                break;
            case "US1": // Standard
                viewDir = inflater.inflate(R.layout.historial_standard_format, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvUsaCalle, contrato.getUsaCalle());
                setText(viewDir, R.id.tvUsaCity, contrato.getUsaCity());
                setText(viewDir, R.id.tvUsaState, contrato.getUsaState());
                setText(viewDir, R.id.tvUsaZip, contrato.getUsaZip());
                setText(viewDir, R.id.tvUsaNeighborhood, contrato.getUsaNeighborhood());
                break;
            case "US2": // PO Box
                viewDir = inflater.inflate(R.layout.historial_pobox_format, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvUsaCalle, contrato.getUsaCalle());
                setText(viewDir, R.id.tvUsaPoBox, contrato.getPoBox());
                setText(viewDir, R.id.tvUsaCity, contrato.getUsaCity());
                setText(viewDir, R.id.tvUsaState, contrato.getUsaState());
                setText(viewDir, R.id.tvUsaZip, contrato.getUsaZip());
                setText(viewDir, R.id.tvUsaNeighborhood, contrato.getUsaNeighborhood());
                break;
            case "US3": // CMR
                viewDir = inflater.inflate(R.layout.historial_cmr_format, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvUsaCmr, contrato.getCmr());
                setText(viewDir, R.id.tvUsaBox, contrato.getBox());
                setText(viewDir, R.id.tvUsaApo, contrato.getApo());
                setText(viewDir, R.id.tvUsaCity, contrato.getUsaCity());
                setText(viewDir, R.id.tvUsaState, contrato.getUsaState());
                setText(viewDir, R.id.tvUsaZip, contrato.getUsaZip());
                break;
            case "OTR":
                viewDir = inflater.inflate(R.layout.historial_domiciliootro, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvOtroLinea1, contrato.getLinea1());
                setText(viewDir, R.id.tvOtroLinea2, contrato.getLinea2());
                setText(viewDir, R.id.tvOtroLinea3, contrato.getLinea3());
                setText(viewDir, R.id.tvOtroLinea4, contrato.getLinea4());
                setText(viewDir, R.id.tvOtroLinea5, contrato.getLinea5());
                setText(viewDir, R.id.tvOtroPais, contrato.getPaisOtro());
                break;
        }

        if (viewDir != null) {
            binding.containerDireccionDinamica.addView(viewDir);
        }
    }

    private void setText(View container, int id, String text) {
        TextView tv = container.findViewById(id);
        if (tv != null) tv.setText(text != null ? text : "");
    }

    private void llenarContenedorPersonas(ViewGroup contenedor, List<ContratoModelo.Persona> personas) {
        contenedor.removeAllViews();
        if (personas == null) return;
        for (ContratoModelo.Persona p : personas) {
            View fila = LayoutInflater.from(this).inflate(R.layout.list_item_person_historial, contenedor, false);
            actualizarFilaPersona(fila, p);
            fila.findViewById(R.id.btnEditar).setVisibility(View.GONE);
            fila.findViewById(R.id.btnEliminar).setVisibility(View.GONE);
            contenedor.addView(fila);
        }
    }

    private void actualizarFilaPersona(View fila, ContratoModelo.Persona p) {
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

    private TextView creaTextViewFila(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(0, 4, 0, 4);
        tv.setTextSize(14);
        return tv;
    }

    private View createPagoDiferidoRow(ContratoModelo.PagoDiferido p) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 4, 0, 4);
        TextView tv = new TextView(this);
        tv.setText(String.format("%s - %s", p.monto, p.fecha));
        row.addView(tv);
        return row;
    }

    private View creaFilaDescuentos(ContratoModelo.DescuentoDetalle d, String numDescuento) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 4, 0, 4);

        TextView tv = new TextView(this);

        String monto = "Monto: " + d.monto;
        SpannableString montoSpanned = new SpannableString(monto);
        montoSpanned.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                "Monto:".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        String descripcion = "Descripción: " + d.descripcion;
        SpannableString descSpanned = new SpannableString(descripcion);
        descSpanned.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                "Descripción:".length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(numDescuento)
                .append(": ")
                .append(montoSpanned)
                .append(" ")
                .append(descSpanned);

        tv.setText(builder);

        row.addView(tv);
        return row;
    }


    private View createTelefonoView(ContratoModelo.InfoTelefono t) {
        View item = getLayoutInflater().inflate(R.layout.item_historial_telefono, binding.containerTelefonos, false);
        TextView tvLabel = new TextView(this);
        String principal = t.esPrincipal ? " (Principal)" : "";
        String whatsapp = t.isWhatsApp ? " [tiene WhatsApp]" : "";
        tvLabel.setText(String.format("%s: %s %s%s%s", t.etiqueta, t.lada, t.numero, whatsapp, principal));
        tvLabel.setPadding(0, 4, 0, 4);
        return tvLabel;
    }

    private View createEmailView(String email) {
        TextView tv = new TextView(this);
        tv.setText(email);
        tv.setPadding(0, 4, 0, 4);
        return tv;
    }

    private View createCuentaRedView(ContratoModelo.CuentaRed sa) {
        View item = getLayoutInflater().inflate(R.layout.item_cuenta_social_historial, binding.containerRedes, false);
        ImageView ivIcon = item.findViewById(R.id.ivPlatformIcon);
        TextView tvNombre = item.findViewById(R.id.tvNombre);
        TextView tvTag = item.findViewById(R.id.tvPlatformTag);
        item.findViewById(R.id.btnEliminar).setVisibility(View.GONE);

        tvNombre.setText(sa.usuario);
        setupSocialIcons(ivIcon, tvTag, sa.red);
        return item;
    }

    private void setupSocialIcons(ImageView iv, TextView tag, String platform) {
        if (platform == null) return;
        switch (platform.toLowerCase()) {
            case "facebook":
                iv.setImageResource(R.drawable.ic_facebook);
                iv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_facebook));
                tag.setText("Facebook");
                tag.setTextColor(0xFF1877F2);
                tag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tag_facebook));
                break;
            case "instagram":
                iv.setImageResource(R.drawable.ic_instagram);
                iv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_instagram));
                tag.setText("Instagram");
                tag.setTextColor(0xFFC13584);
                tag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tag_instagram));
                break;
            case "x":
                iv.setImageResource(R.drawable.ic_x_twitter);
                iv.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_circle_twitter));
                tag.setText("X");
                tag.setTextColor(0xFF1DA1F2);
                tag.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_tag_twitter));
                break;
        }
    }

    private View createRegaloView(String regalo) {
        View item = getLayoutInflater().inflate(R.layout.item_regalos, binding.containerRegalos, false);
        TextView noRegalo = item.findViewById(R.id.noRegalo);
        TextView tvNombre = new TextView(this); // Using TextView for consistency
        tvNombre.setText(regalo);
        
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView tvNo = new TextView(this);
        tvNo.setText(String.format("%d. ", binding.containerRegalos.getChildCount() + 1));
        row.addView(tvNo);
        row.addView(tvNombre);
        row.setPadding(0, 4, 0, 4);
        return row;
    }
    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }


    //cambia de 01/02/2000 a 01/feb/2000
    //cambia de 01/02/2000 a 01/feb/2000
    private String convertirMesANombre(String texto) {
        if (texto == null || texto.isEmpty()) return "";

        try {
            // Input format EXACTLY as your DB gives it
            SimpleDateFormat input = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.US
            );

            SimpleDateFormat output;

            if (esIngles()) {
                output = new SimpleDateFormat(
                        "MMM/dd/yyyy HH:mm",
                        Locale.US
                );
            } else {
                output = new SimpleDateFormat(
                        "dd/MMM/yyyy HH:mm",
                        new Locale("es")
                );
            }

            return output.format(input.parse(texto))
                    .toLowerCase();


        } catch (Exception e) {
            e.printStackTrace();
            return texto;
        }
    }}