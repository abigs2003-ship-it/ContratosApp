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
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import com.google.android.material.button.MaterialButton;
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

        binding.btnBack.setOnClickListener(v -> finish());

        MaterialButton btnImprimir = findViewById(R.id.btnImprimir);
        btnImprimir.setOnClickListener(v -> imprimirContrato());

        long idContrato = getIntent().getLongExtra("ID_CONTRATO", -1);
        if (idContrato == -1) {
            finish();
            return;
        }

        viewModel.getContrato().observe(this, c -> {
            if (c == null || !c.isDatosListos()) return;
            contrato = c;
            llenarDatos();
            prepararWebViewParaImprimir();
            binding.btnImprimir.setEnabled(true);
        });
        binding.tvId.setText("#" + idContrato);

        viewModel.fetchContratoPorId(idContrato);
    }

    private WebView printWebView;
    private boolean webViewReady = false;

    private void prepararWebViewParaImprimir() {
        if (printWebView != null) {
            binding.getRoot().removeView(printWebView);
        }
        webViewReady = false;
        printWebView = new WebView(this);
        printWebView.getSettings().setJavaScriptEnabled(false);
        printWebView.getSettings().setBlockNetworkLoads(true);

        binding.getRoot().addView(printWebView, new ViewGroup.LayoutParams(1, 1));

        printWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                android.util.Log.d("PRINT_DEBUG", "✅ WebView listo");
                webViewReady = true;
            }
        });

        String encodedHtml = android.util.Base64.encodeToString(
                buildPrintHtml().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                android.util.Base64.NO_PADDING
        );
        printWebView.loadData(encodedHtml, "text/html", "base64");
    }

    private void llenarDatos() {
        if (contrato == null) return;
        String creacion = String.format("Creado: %s", convertirMesANombre(contrato.getFechaCreacion()));
        String modificacion = String.format("| Modificado: %s", convertirMesANombre(contrato.getFechaModificacion()));
        binding.tvCreacion.setText(creacion);
        if (contrato.getFechaModificacion() == null) {
            binding.tvModificado.setText("");
        } else {
            binding.tvModificado.setText(modificacion);
        }

        binding.tvIdioma.setText(contrato.getIdioma());
        binding.tvTipoDiferido.setText(contrato.getTipoPagoEnganche());
        llenarContenedorPersonas(binding.containerTitulares, contrato.getTitulares());
        llenarContenedorPersonas(binding.containerBeneficiarios, contrato.getBeneficiarios());

        binding.tvPais.setText(contrato.getPais());
        binding.tvNacionalidad.setText(contrato.getNacionalidad());
        llenarDireccionDinamica();

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

        if (!contrato.isNoCorreo()) {
            binding.checkNoCorreo.setVisibility(View.GONE);
        } else {
            binding.checkNoCorreo.setVisibility(View.VISIBLE);
            binding.checkNoCorreo.setChecked(contrato.isNoCorreo());
        }

        if (!contrato.isNoRedesSociales()) {
            binding.checkNoRedes.setVisibility(View.GONE);
        } else {
            binding.checkNoRedes.setVisibility(View.VISIBLE);
            binding.checkNoRedes.setChecked(contrato.isNoRedesSociales());
        }

        binding.containerRedes.removeAllViews();
        if (contrato.getRedesSociales() != null) {
            for (ContratoModelo.CuentaRed sa : contrato.getRedesSociales()) {
                binding.containerRedes.addView(createCuentaRedView(sa));
            }
        }

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
        boolean esContado = "Contado".equalsIgnoreCase(contrato.getTipoPago());

        binding.seccionEnganche.setVisibility(esContado ? View.GONE : View.VISIBLE);

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

            binding.containerContratosMontoCuenta.addView(tv);
        }

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
            case "US1":
                viewDir = inflater.inflate(R.layout.historial_standard_format, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvUsaCalle, contrato.getUsaCalle());
                setText(viewDir, R.id.tvUsaCity, contrato.getUsaCity());
                setText(viewDir, R.id.tvUsaState, contrato.getUsaState());
                setText(viewDir, R.id.tvUsaZip, contrato.getUsaZip());
                break;
            case "US2":
                viewDir = inflater.inflate(R.layout.historial_pobox_format, binding.containerDireccionDinamica, false);
                setText(viewDir, R.id.tvUsaCalle, contrato.getUsaCalle());
                setText(viewDir, R.id.tvUsaPoBox, contrato.getPoBox());
                setText(viewDir, R.id.tvUsaCity, contrato.getUsaCity());
                setText(viewDir, R.id.tvUsaState, contrato.getUsaState());
                setText(viewDir, R.id.tvUsaZip, contrato.getUsaZip());
                break;
            case "US3":
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
            contenedor.addView(fila);
        }
    }

    private void actualizarFilaPersona(View fila, ContratoModelo.Persona p) {
        String fullName = p.nombre + " " + (p.paterno != null ? p.paterno : "") + " " + (p.materno != null ? p.materno : "");
        ((TextView) fila.findViewById(R.id.textNombre)).setText(fullName.trim());
        ((TextView) fila.findViewById(R.id.textCumple)).setText(p.cumple);
        ((TextView) fila.findViewById(R.id.textOcupacion)).setText(p.ocupacion);
        if (p.archivoFirma != null) {
            ((TextView) fila.findViewById(R.id.tvFirma)).setText("Con Firma");
        } else {
            ((TextView) fila.findViewById(R.id.tvFirma)).setText("Sin Firma");
        }

        if (p.archivoINEFrente != null || p.archivoPasaporte != null) {
            ((TextView) fila.findViewById(R.id.tvId)).setText("Con Id");
        } else {
            ((TextView) fila.findViewById(R.id.tvId)).setText("Sin Id");
        }
    
        String parentescoDisplay = p.parentesco;
        try {
            int pos = Integer.parseInt(p.parentesco);
            String[] array = getResources().getStringArray(R.array.parentescos);
            if (pos >= 0 && pos < array.length) parentescoDisplay = array[pos];
        } catch (Exception ignored) {}
        ((TextView) fila.findViewById(R.id.textParentesco)).setText(parentescoDisplay);
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
        TextView tvNombre = new TextView(this);
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

    private String convertirMesANombre(String texto) {
        if (texto == null || texto.isEmpty()) return "";

        try {
            SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

            SimpleDateFormat output;

            if (esIngles()) {
                output = new SimpleDateFormat("MMM/dd/yyyy HH:mm", Locale.US);
            } else {
                output = new SimpleDateFormat("dd/MMM/yyyy HH:mm", new Locale("es"));
            }

            return output.format(input.parse(texto)).toLowerCase();

        } catch (Exception e) {
            e.printStackTrace();
            return texto;
        }
    }

    private void imprimirContrato() {
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setBlockNetworkLoads(true);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );
        binding.getRoot().addView(webView, params);

        final String jobName = "Contrato_" + binding.tvId.getText().toString().trim();
        final String encodedHtml = android.util.Base64.encodeToString(
                buildPrintHtml().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                android.util.Base64.NO_PADDING
        );

        webView.setWebViewClient(new WebViewClient() {
            private boolean printed = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                android.util.Log.d("PRINT_DEBUG", "✅ onPageFinished!");
                if (!printed) {
                    printed = true;
                    triggerPrint(view, jobName);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                android.util.Log.d("PRINT_DEBUG", "onPageStarted: " + url);
            }
        });

        webView.loadData(encodedHtml, "text/html", "base64");

        binding.getRoot().postDelayed(() -> {
            android.util.Log.d("PRINT_DEBUG", "⏱ Fallback timer fired");
            triggerPrint(webView, jobName);
        }, 3000);
    }

    private boolean hasPrinted = false;

    private void triggerPrint(WebView webView, String jobName) {
        if (hasPrinted) {
            android.util.Log.d("PRINT_DEBUG", "Already printed, skipping");
            return;
        }
        hasPrinted = true;

        runOnUiThread(() -> {
            android.util.Log.d("PRINT_DEBUG", "🖨️ Triggering print...");
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            if (printManager != null) {
                printManager.print(
                        jobName,
                        webView.createPrintDocumentAdapter(jobName),
                        new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                                .build()
                );
                android.util.Log.d("PRINT_DEBUG", "✅ print() called");
            }
            binding.getRoot().postDelayed(() -> {
                if (webView.getParent() != null) {
                    binding.getRoot().removeView(webView);
                }
                hasPrinted = false;
            }, 1000);
        });
    }

    private String getTextOf(int id) {
        TextView view = findViewById(id);
        if (view == null) return "—";
        String text = view.getText().toString().trim();
        return text.isEmpty() ? "—" : text;
    }

    private String cb(int id) {
        CheckBox box = findViewById(id);
        return (box != null && box.isChecked()) ? "✓ Sí" : "✗ No";
    }

    private String row(String label1, String val1, String label2, String val2) {
        return "<div class='row'>"
                + "<div class='col'><div class='label'>" + label1 + "</div><div class='value'>" + val1 + "</div></div>"
                + "<div class='col'><div class='label'>" + label2 + "</div><div class='value'>" + val2 + "</div></div>"
                + "</div>";
    }

    private String field(String label, String val) {
        return "<div class='field'>"
                + "<div class='label'>" + label + "</div>"
                + "<div class='value'>" + val + "</div>"
                + "</div>";
    }

    private String extractRedesSociales(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<div class='value'>—</div>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View item = container.getChildAt(i);
            TextView tvNombre = item.findViewById(R.id.tvNombre);
            TextView tvTag    = item.findViewById(R.id.tvPlatformTag);
            if (tvNombre != null && tvTag != null) {
                sb.append("<div class='value'>")
                        .append(tvTag.getText()).append(": ")
                        .append(tvNombre.getText())
                        .append("</div>");
            }
        }
        return sb.length() > 0 ? sb.toString() : "<div class='value'>—</div>";
    }

    private String section(String title) {
        return "<div class='section-block'><div class='section-title'>" + title + "</div>";
    }

    private String endSection() {
        return "</div>";
    }

    private String divider() {
        return "<hr class='divider'>";
    }

    private String buildPrintHtml() {

        String css = "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { font-family: Arial, sans-serif; font-size: 10px; color: #111;"
                + "  background: #fff; padding: 10px; }"

                + ".header { border-bottom: 2px solid #000; padding-bottom: 6px; margin-bottom: 10px; }"
                + ".header h1 { font-size: 14px; font-weight: bold; }"
                + ".header .meta { font-size: 9px; color: #555; margin-top: 2px; }"

                + ".section-title { font-size: 8px; font-weight: bold; letter-spacing: 0.08em;"
                + "  text-transform: uppercase; margin-top: 4px; margin-bottom: 3px;"
                + "  background: #333; color: #fff; padding: 2px 6px; }"

                + ".side-by-side { display: table; width: 100%; border-collapse: separate;"
                + "  border-spacing: 4px; margin-bottom: 2px; }"
                + ".side-panel { display: table-cell; width: 50%; vertical-align: top; }"
                + ".side-panel .section-title { margin-top: 0; }"

                + "table.card { border: 1px solid #ccc; width: 100%;"
                + "  border-collapse: collapse; margin-bottom: 3px; }"
                + "table.card td { padding: 2px 5px; vertical-align: top; font-size: 10px; }"
                + "table.card tr { page-break-inside: avoid; break-inside: avoid; }"

                + "td.lbl { font-size: 8px; font-weight: bold; color: #444;"
                + "  text-transform: uppercase; letter-spacing: 0.02em;"
                + "  padding-top: 4px; padding-bottom: 1px; }"
                + "td.val { font-size: 10px; color: #111; padding-bottom: 4px; }"

                + "td.lbl3 { font-size: 8px; font-weight: bold; color: #444;"
                + "  text-transform: uppercase; letter-spacing: 0.02em;"
                + "  padding-top: 4px; padding-bottom: 1px; width: 33%; }"
                + "td.val3 { font-size: 10px; color: #111; padding-bottom: 4px; width: 33%; }"

                + "tr.div-row td { border-top: 1px solid #ddd; padding:0; height:1px; }"

                + "tr.persona-name td { font-weight: bold; font-size: 10px; padding-top: 4px; }"
                + "tr.persona-sep td { border-top: 1px solid #eee; padding:0; height:1px; }"

                + ".comments { background: #f7f7f7; border: 1px solid #ddd;"
                + "  padding: 5px; font-size: 10px; margin-top: 3px; }"

                + "@media print {"
                + "  @page { margin: 6mm; size: letter portrait; }"
                + "  body { padding: 0; }"
                + "  .side-by-side { display: table !important; }"
                + "  .side-panel { display: table-cell !important; }"
                + "  table.card tr { page-break-inside: avoid !important; break-inside: avoid !important; }"
                + "  .section-title { page-break-after: avoid !important; }"
                + "}"
                + "</style>";

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
                .append(css).append("</head><body>");

        html.append("<div class='header'>")
                .append("<h1>Detalle del Contrato &nbsp;").append(getTextOf(R.id.tvId)).append("</h1>")
                .append("<div class='meta'>")
                .append(getTextOf(R.id.tvCreacion)).append(" &nbsp;&nbsp; ").append(getTextOf(R.id.tvModificado))
                .append("</div></div>");

        html.append("<div class='section-title'>Idioma</div>")
                .append("<table class='card'><tbody>")
                .append(trow2("", getTextOf(R.id.tvIdioma), null, null))
                .append("</tbody></table>");

        html.append("<div class='side-by-side'>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Titulares</div>")
                .append("<table class='card'><tbody>")
                .append(extractPersonasTable(binding.containerTitulares))
                .append("</tbody></table>")
                .append("</div>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Beneficiarios</div>")
                .append("<table class='card'><tbody>")
                .append(extractPersonasTable(binding.containerBeneficiarios))
                .append("</tbody></table>")
                .append("</div>")

                .append("</div>");

        html.append("<div class='side-by-side'>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Domicilio y Nacionalidad</div>")
                .append("<table class='card'><tbody>")
                .append(trow2("País", getTextOf(R.id.tvPais), "Nacionalidad", getTextOf(R.id.tvNacionalidad)))
                .append(extractDireccionTable())
                .append("</tbody></table>")
                .append("</div>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Teléfonos</div>")
                .append("<table class='card'><tbody>")
                .append(extractSimpleContainerTable(binding.containerTelefonos))
                .append("</tbody></table>")
                .append("<div class='section-title'>Correos Electrónicos</div>")
                .append("<table class='card'><tbody>");
        if (binding.checkNoCorreo.getVisibility() == View.VISIBLE && binding.checkNoCorreo.isChecked()) {
            html.append(trow2("✓ No cuenta con correo electrónico", null, null, null));
        } else {
            html.append(extractSimpleContainerTable(binding.containerEmails));
        }
        html.append("</tbody></table>")
                .append("<div class='section-title'>Redes Sociales</div>")
                .append("<table class='card'><tbody>");
        if (binding.checkNoRedes.getVisibility() == View.VISIBLE && binding.checkNoRedes.isChecked()) {
            html.append(trow2("✓ No cuenta con redes sociales", null, null, null));
        } else {
            html.append(extractRedesSocialesTable(binding.containerRedes));
        }
        html.append("</tbody></table>")
                .append("</div>")
                .append("</div>");
        html.append("<div class='section-title'>Condiciones de Venta</div>")
                .append("<table class='card'><tbody>")
                .append(trow3("Tipo de Venta",      getTextOf(R.id.tvTipoVenta),
                        "No. Años de Uso",    getTextOf(R.id.tvAniosUso),
                        "Tipo de Ocupación",  getTextOf(R.id.tvTipoOcupacion)))
                .append(trow3("Año Primer Uso",      getTextOf(R.id.tvAnioPrimerUso),
                        "Unidad",             getTextOf(R.id.tvUnidad),
                        "Temporada",          getTextOf(R.id.tvTemporada)))
                .append(trow3("Moneda",             getTextOf(R.id.tvMoneda),
                        "Tipo de Cambio",     getTextOf(R.id.tvTipoCambio),
                        "Tipo de Pago",       getTextOf(R.id.tvTipoPago)))
                .append(tdivider3())
                .append(trow3("Precio Bruto",       getTextOf(R.id.tvPrecioBruto),
                        "Precio Neto",        getTextOf(R.id.tvPrecioNeto),
                        "Monto a Cuenta",     getTextOf(R.id.tvMontoCuenta)))
                .append(tdivider3())
                .append(trow3("Enganche Total",     getTextOf(R.id.tvEngancheMonto),
                        "Enganche (%)",       getTextOf(R.id.tvEnganchePorcentaje),
                        "Tipo Pago Diferido", getTextOf(R.id.tvTipoDiferido)))
                .append(trow3("Enganche en Sala",   getTextOf(R.id.tvEngancheSala),
                        "Enganche Sala (%)",  getTextOf(R.id.tvEngancheSalaPorcentaje),
                        "Eng. Diferido",      getTextOf(R.id.tvEngDiferidoMonto)))
                .append(trow3("No. Pagos Eng.",     getTextOf(R.id.tvNoPagosEng),
                        "Varios (Desc.)",     getTextOf(R.id.tvVariosMonto),
                        "Saldo Enganche",     getTextOf(R.id.tvSaldoEnganche)))
                .append(tdivider3())
                .append(trow3("Costo Contrato",     getTextOf(R.id.tvCostoContrato),
                        "Pago Sala",          getTextOf(R.id.tvPagoSala),
                        "Costo Membresía",    getTextOf(R.id.tvCostoMembresia)))
                .append(tdivider3())
                .append("<tr><td class='lbl3' colspan='3'>Pagos Diferidos</td></tr>")
                .append("<tr><td colspan='3' class='val'>")
                .append(extractInlineList(binding.containerPagosDiferidos))
                .append("</td></tr>")
                .append("<tr><td class='lbl3'>No. Contratos M.C.</td>")
                .append("<td class='val3' colspan='2'>").append(getTextOf(R.id.tvNoContratosMontoCta)).append("</td></tr>")
                .append("<tr><td colspan='3' class='val'>")
                .append(extractInlineList(binding.containerContratosMontoCuenta))
                .append("</td></tr>")
                .append("<tr><td class='lbl3'>No. Descuentos</td>")
                .append("<td class='val3' colspan='2'>").append(getTextOf(R.id.tvNoDesc)).append("</td></tr>")
                .append("<tr><td colspan='3' class='val'>")
                .append(extractInlineList(binding.containerDescuentos))
                .append("</td></tr>")
                .append("</tbody></table>");

        html.append("<div class='side-by-side'>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Regalos</div>")
                .append("<table class='card'><tbody>")
                .append(extractSimpleContainerTable(binding.containerRegalos))
                .append("</tbody></table>")
                .append("<div class='section-title'>Comentarios</div>")
                .append("<div class='comments'>").append(getTextOf(R.id.tvComentarios)).append("</div>")
                .append("</div>")

                .append("<div class='side-panel'>")
                .append("<div class='section-title'>Financiamiento</div>")
                .append("<table class='card'><tbody>")
                .append(trow2("Monto a Financiar",   getTextOf(R.id.tvMontoFinanciar),
                        "Número de Pagos",     getTextOf(R.id.tvNumPagos)))
                .append(trow2("Tasa de Interés (%)", getTextOf(R.id.tvTasa),
                        "Tipo de Periodo",     getTextOf(R.id.tvTipoPeriodo)))
                .append(trow2("Fecha Primer Pago",   getTextOf(R.id.tvFechaPrimerPago), null, null))
                .append("</tbody></table>")
                .append("</div>")

                .append("</div>");

        html.append("</body></html>");
        return html.toString();
    }

    private String extractPersonasTable(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<tr><td colspan='2' class='val'>—</td></tr>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View fila = container.getChildAt(i);
            if (i > 0) sb.append("<tr class='persona-sep'><td colspan='2'></td></tr>");
            sb.append("<tr class='persona-name' style='page-break-inside:avoid;break-inside:avoid;'>")
                    .append("<td colspan='2'>").append(textOf(fila, R.id.textNombre)).append("</td>")
                    .append("</tr>")
                    .append(trow("Parentesco", textOf(fila, R.id.textParentesco),
                            "Cumpleaños", textOf(fila, R.id.textCumple)))
                    .append(trow("Ocupación",  textOf(fila, R.id.textOcupacion),
                            "Firma",     textOf(fila, R.id.tvFirma)))
                    .append(trow("Identificación", textOf(fila, R.id.tvId), null, null));
        }
        return sb.toString();
    }

    private String extractDireccionTable() {
        LinearLayout container = binding.containerDireccionDinamica;
        if (container == null || container.getChildCount() == 0)
            return "<tr><td colspan='2' class='val'>—</td></tr>";

        View viewDir = container.getChildAt(0);
        if (viewDir == null || contrato == null) return "<tr><td colspan='2' class='val'>—</td></tr>";

        String tipoDir = contrato.getTipoDir();
        if (tipoDir == null) return "<tr><td colspan='2' class='val'>—</td></tr>";

        StringBuilder sb = new StringBuilder();
        switch (tipoDir) {
            case "MEX":
                sb.append(trow("Calle",               textOf(viewDir, R.id.tvMexCalle),
                                "Colonia",             textOf(viewDir, R.id.tvMexColonia)))
                        .append(trow("Núm. Exterior",        textOf(viewDir, R.id.tvMexNumExt),
                                "Núm. Interior",       textOf(viewDir, R.id.tvMexNumInt)))
                        .append(trow("Municipio/Delegación", textOf(viewDir, R.id.tvMexMunicipio),
                                "Estado",              textOf(viewDir, R.id.tvMexEstado)))
                        .append(trow("Ciudad",               textOf(viewDir, R.id.tvMexCiudad),
                                "C.P.",                textOf(viewDir, R.id.tvMexCP)));
                break;
            case "CAN":
                sb.append(trow("Calle",          textOf(viewDir, R.id.tvCanCalle),      null, null))
                        .append(trow("Ciudad",         textOf(viewDir, R.id.tvCanCity),
                                "Provincia",    textOf(viewDir, R.id.tvCanProvince)))
                        .append(trow("Código Postal",  textOf(viewDir, R.id.tvCanPostalCode), null, null));
                break;
            case "US1":
                sb.append(trow("Calle", textOf(viewDir, R.id.tvUsaCalle), null, null))
                        .append(trow("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(trow("ZIP", textOf(viewDir, R.id.tvUsaZip),
                                null, null));
                break;
            case "US2":
                sb.append(trow("Calle", textOf(viewDir, R.id.tvUsaCalle), null, null))
                        .append(trow("PO Box", textOf(viewDir, R.id.tvUsaPoBox), null, null))
                        .append(trow("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(trow("ZIP", textOf(viewDir, R.id.tvUsaZip),
                                null, null));
                break;
            case "US3":
                sb.append(trow("CMR",    textOf(viewDir, R.id.tvUsaCmr),
                                "Box",   textOf(viewDir, R.id.tvUsaBox)))
                        .append(trow("APO",    textOf(viewDir, R.id.tvUsaApo),              null, null))
                        .append(trow("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(trow("ZIP",    textOf(viewDir, R.id.tvUsaZip),              null, null));
                break;
            case "OTR":
                sb.append(trow("Línea 1", textOf(viewDir, R.id.tvOtroLinea1), null, null))
                        .append(trow("Línea 2", textOf(viewDir, R.id.tvOtroLinea2), null, null))
                        .append(trow("Línea 3", textOf(viewDir, R.id.tvOtroLinea3), null, null))
                        .append(trow("Línea 4", textOf(viewDir, R.id.tvOtroLinea4), null, null))
                        .append(trow("Línea 5", textOf(viewDir, R.id.tvOtroLinea5), null, null))
                        .append(trow("País",    textOf(viewDir, R.id.tvOtroPais),   null, null));
                break;
            default:
                sb.append("<tr><td colspan='2' class='val'>—</td></tr>");
        }
        return sb.toString();
    }

    private String trow(String label1, String val1, String label2, String val2) {
        if (label2 == null) {
            return "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                    + "<td class='lbl' colspan='2'>" + label1 + "</td>"
                    + "</tr>"
                    + "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                    + "<td class='val' colspan='2'>" + (val1 != null ? val1 : "—") + "</td>"
                    + "</tr>";
        }
        return "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='lbl'>" + label1 + "</td>"
                + "<td class='lbl'>" + label2 + "</td>"
                + "</tr>"
                + "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='val'>" + (val1 != null ? val1 : "—") + "</td>"
                + "<td class='val'>" + (val2 != null ? val2 : "—") + "</td>"
                + "</tr>";
    }

    private String extractSimpleContainerTable(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<tr><td colspan='2' class='val'>—</td></tr>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            StringBuilder text = new StringBuilder();
            extractAllTextViews(child, text);
            if (text.length() > 0) {
                sb.append("<tr style='page-break-inside:avoid;break-inside:avoid;'>")
                        .append("<td colspan='2' class='val'>").append(text).append("</td>")
                        .append("</tr>");
            }
        }
        return sb.length() > 0 ? sb.toString() : "<tr><td colspan='2' class='val'>—</td></tr>";
    }

    private String extractRedesSocialesTable(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<tr><td colspan='2' class='val'>—</td></tr>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View item = container.getChildAt(i);
            TextView tvNombre = item.findViewById(R.id.tvNombre);
            TextView tvTag    = item.findViewById(R.id.tvPlatformTag);
            if (tvNombre != null && tvTag != null) {
                sb.append("<tr style='page-break-inside:avoid;break-inside:avoid;'>")
                        .append("<td class='lbl'>").append(tvTag.getText()).append("</td>")
                        .append("<td class='val'>").append(tvNombre.getText()).append("</td>")
                        .append("</tr>");
            }
        }
        return sb.length() > 0 ? sb.toString() : "<tr><td colspan='2' class='val'>—</td></tr>";
    }

    private String extractPersonas(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<div class='value'>—</div>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View fila = container.getChildAt(i);
            String nombre     = textOf(fila, R.id.textNombre);
            String cumple     = textOf(fila, R.id.textCumple);
            String ocupacion  = textOf(fila, R.id.textOcupacion);
            String parentesco = textOf(fila, R.id.textParentesco);
            String firma      = textOf(fila, R.id.tvFirma);

            sb.append("<div class='persona-block'>")
                    .append("<div class='value'><b>").append(nombre).append("</b></div>")
                    .append("<div class='row'>")
                    .append("<div class='col'>")
                    .append("<div class='label'>Parentesco</div>")
                    .append("<div class='value'>").append(parentesco).append("</div>")
                    .append("</div>")
                    .append("<div class='col'>")
                    .append("<div class='label'>Cumpleaños</div>")
                    .append("<div class='value'>").append(cumple).append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("<div class='row'>")
                    .append("<div class='col'>")
                    .append("<div class='label'>Ocupación</div>")
                    .append("<div class='value'>").append(ocupacion).append("</div>")
                    .append("</div>")
                    .append("<div class='col'>")
                    .append("<div class='label'>Firma</div>")
                    .append("<div class='value'>").append(firma).append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("</div>");
        }
        return sb.toString();
    }

    private String trow2(String l1, String v1, String l2, String v2) {
        if (l2 == null) {
            return "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                    + "<td class='lbl' colspan='2'>" + l1 + "</td></tr>"
                    + "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                    + "<td class='val' colspan='2'>" + (v1 != null ? v1 : "—") + "</td></tr>";
        }
        return "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='lbl'>" + l1 + "</td><td class='lbl'>" + l2 + "</td></tr>"
                + "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='val'>" + (v1 != null ? v1 : "—") + "</td>"
                + "<td class='val'>" + (v2 != null ? v2 : "—") + "</td></tr>";
    }

    private String trow3(String l1, String v1, String l2, String v2, String l3, String v3) {
        return "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='lbl3'>" + l1 + "</td>"
                + "<td class='lbl3'>" + l2 + "</td>"
                + "<td class='lbl3'>" + l3 + "</td></tr>"
                + "<tr style='page-break-inside:avoid;break-inside:avoid;'>"
                + "<td class='val3'>" + (v1 != null ? v1 : "—") + "</td>"
                + "<td class='val3'>" + (v2 != null ? v2 : "—") + "</td>"
                + "<td class='val3'>" + (v3 != null ? v3 : "—") + "</td></tr>";
    }

    private String tdivider3() {
        return "<tr class='div-row'><td colspan='3'></td></tr>";
    }

    private String extractInlineList(LinearLayout container) {
        if (container == null || container.getChildCount() == 0) return "—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            StringBuilder text = new StringBuilder();
            extractAllTextViews(child, text);
            if (text.length() > 0) {
                if (sb.length() > 0) sb.append(" &nbsp;|&nbsp; ");
                sb.append(text);
            }
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    private String extractDireccion() {
        LinearLayout container = binding.containerDireccionDinamica;
        if (container == null || container.getChildCount() == 0)
            return "<div class='value'>—</div>";

        View viewDir = container.getChildAt(0);
        if (viewDir == null) return "<div class='value'>—</div>";

        String tipoDir = contrato.getTipoDir();
        if (tipoDir == null) return "<div class='value'>—</div>";

        StringBuilder sb = new StringBuilder();

        switch (tipoDir) {
            case "MEX":
                sb.append(row("Calle", textOf(viewDir, R.id.tvMexCalle),
                                "Colonia", textOf(viewDir, R.id.tvMexColonia)))
                        .append(row("Num. Ext.", textOf(viewDir, R.id.tvMexNumExt),
                                "Num. Int.", textOf(viewDir, R.id.tvMexNumInt)))
                        .append(row("Municipio/Delegación", textOf(viewDir, R.id.tvMexMunicipio),
                                "Estado", textOf(viewDir, R.id.tvMexEstado)))
                        .append(row("Ciudad", textOf(viewDir, R.id.tvMexCiudad),
                                "C.P.", textOf(viewDir, R.id.tvMexCP)));
                break;

            case "CAN":
                sb.append(field("Calle",       textOf(viewDir, R.id.tvCanCalle)))
                        .append(row("Ciudad",        textOf(viewDir, R.id.tvCanCity),
                                "Provincia",    textOf(viewDir, R.id.tvCanProvince)))
                        .append(field("Código Postal", textOf(viewDir, R.id.tvCanPostalCode)));
                break;

            case "US1":
                sb.append(trow("Calle", textOf(viewDir, R.id.tvUsaCalle), null, null))
                        .append(trow("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(trow("ZIP", textOf(viewDir, R.id.tvUsaZip),
                                null, null));
                break;

            case "US2":
                sb.append(trow("Calle", textOf(viewDir, R.id.tvUsaCalle), null, null))
                        .append(trow("PO Box", textOf(viewDir, R.id.tvUsaPoBox), null, null))
                        .append(trow("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(trow("ZIP", textOf(viewDir, R.id.tvUsaZip),
                                null, null));
                break;

            case "US3":
                sb.append(row("CMR",   textOf(viewDir, R.id.tvUsaCmr),
                                "Box",   textOf(viewDir, R.id.tvUsaBox)))
                        .append(field("APO", textOf(viewDir, R.id.tvUsaApo)))
                        .append(row("Ciudad", textOf(viewDir, R.id.tvUsaCity),
                                "Estado", textOf(viewDir, R.id.tvUsaState)))
                        .append(field("ZIP", textOf(viewDir, R.id.tvUsaZip)));
                break;

            case "OTR":
                sb.append(field("Línea 1", textOf(viewDir, R.id.tvOtroLinea1)))
                        .append(field("Línea 2", textOf(viewDir, R.id.tvOtroLinea2)))
                        .append(field("Línea 3", textOf(viewDir, R.id.tvOtroLinea3)))
                        .append(field("Línea 4", textOf(viewDir, R.id.tvOtroLinea4)))
                        .append(field("Línea 5", textOf(viewDir, R.id.tvOtroLinea5)))
                        .append(field("País",    textOf(viewDir, R.id.tvOtroPais)));
                break;

            default:
                sb.append("<div class='value'>—</div>");
                break;
        }

        return sb.toString();
    }

    private String extractSimpleContainer(LinearLayout container) {
        if (container == null || container.getChildCount() == 0)
            return "<div class='value'>—</div>";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                String t = ((TextView) child).getText().toString().trim();
                if (!t.isEmpty()) {
                    sb.append("<div class='value'>").append(t).append("</div>");
                }
            } else if (child instanceof ViewGroup) {
                StringBuilder rowText = new StringBuilder();
                extractAllTextViews(child, rowText);
                if (rowText.length() > 0) {
                    sb.append("<div class='value'>").append(rowText).append("</div>");
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "<div class='value'>—</div>";
    }

    private void extractAllTextViews(View view, StringBuilder sb) {
        if (view instanceof TextView) {
            String t = ((TextView) view).getText().toString().trim();
            if (!t.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(t);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                extractAllTextViews(group.getChildAt(i), sb);
            }
        }
    }

    private String textOf(View parent, int id) {
        if (parent == null) return "—";
        TextView tv = parent.findViewById(id);
        if (tv == null) return "—";
        String t = tv.getText().toString().trim();
        return t.isEmpty() ? "—" : t;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPrinted = false;
    }
}
