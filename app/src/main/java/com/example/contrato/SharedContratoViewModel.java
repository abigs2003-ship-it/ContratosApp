package com.example.contrato;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contrato.model.VentasContrato;
import com.example.contrato.model.VentasDescuentos;
import com.example.contrato.model.VentasEngancheDiferido;
import com.example.contrato.model.VentasFinanciamientos;
import com.example.contrato.model.VentasInformacionGeneral;
import com.example.contrato.model.VentasInventario;
import com.example.contrato.model.VentasMontoCta;
import com.example.contrato.model.VentasRedesSociales;
import com.example.contrato.model.VentasRegalos;
import com.example.contrato.model.VentasTitulares;
import com.example.contrato.repository.VentasContratoRepository;
import com.example.contrato.repository.VentasDescuentosRepository;
import com.example.contrato.repository.VentasEngancheDiferidoRepository;
import com.example.contrato.repository.VentasFinanciamientosRepository;
import com.example.contrato.repository.VentasInformacionGeneralRepository;
import com.example.contrato.repository.VentasInventarioRepository;
import com.example.contrato.repository.VentasMontoCtaRepository;
import com.example.contrato.repository.VentasRedesSocialesRepository;
import com.example.contrato.repository.VentasRegalosRepository;
import com.example.contrato.repository.VentasTitularesRepository;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SharedContratoViewModel extends ViewModel {
    private final MutableLiveData<ContratoModelo> Contrato = new MutableLiveData<>(new ContratoModelo());
    private final MutableLiveData<List<ContratoModelo>> history = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> unidades = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private long currentUserId = -1;
    private int lastDgTab = 0;
    private int lastCondTab = 0;
    private boolean modoMensual = false;
    private String fechaInicialMensual = null;
    private ContratoModelo.Persona personaParaFirma;


    public void setPersonaParaFirma(ContratoModelo.Persona persona) {
        personaParaFirma = persona;
    }


    public ContratoModelo.Persona getPersonaParaFirma() {
        return personaParaFirma;
    }

    public boolean isModoMensual() { return modoMensual; }
    public void setModoMensual(boolean v) { modoMensual = v; }
    public String getFechaInicialMensual() { return fechaInicialMensual; }
    public void setFechaInicialMensual(String f) { fechaInicialMensual = f; }
    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};



    private final VentasContratoRepository contratoRepo = new VentasContratoRepository();
    private final VentasInformacionGeneralRepository infoGralRepo = new VentasInformacionGeneralRepository();
    private final VentasTitularesRepository titularesRepo = new VentasTitularesRepository();
    private final VentasInventarioRepository inventarioRepo = new VentasInventarioRepository();
    private final VentasDescuentosRepository descuentosRepo = new VentasDescuentosRepository();
    private final VentasEngancheDiferidoRepository engancheDiferidoRepo = new VentasEngancheDiferidoRepository();
    private final VentasMontoCtaRepository montoCtaRepo = new VentasMontoCtaRepository();
    private final VentasFinanciamientosRepository financiamientoRepo = new VentasFinanciamientosRepository();
    private final VentasRegalosRepository regalosRepo = new VentasRegalosRepository();
    private final VentasRedesSocialesRepository redesSocialesRepo = new VentasRedesSocialesRepository();

    public LiveData<ContratoModelo> getContrato() {
        return Contrato;
    }

    public void setContrato(ContratoModelo ContratoModel) {
        Contrato.setValue(ContratoModel);
    }
    
    public ContratoModelo getContratoValue() {
        return Contrato.getValue();
    }

    public LiveData<List<ContratoModelo>> getHistory() {
        return history;
    }

    public LiveData<List<String>> getUnidades() {
        return unidades;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void fetchUnidades() {
        new Thread(() -> {
            try {
                List<String> list = inventarioRepo.getUnidades();
                unidades.postValue(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void fetchContratoPorId(long idContrato) {
        Log.d("HISTORIAL",
                "Buscando contrato en BD. ID = " + idContrato);
        new Thread(() -> {
            try {
                ContratoModelo m = contratoRepo.getContratoCompleto(idContrato);
                Contrato.postValue(m);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al cargar contrato: " + e.getMessage());
            }
        }).start();
    }



    public void cargaHistorialBaseDatos(long usuarioId) {
        new Thread(() -> {
            try {
                List<ContratoModelo> models = contratoRepo.getResumenByUserId(usuarioId);
                history.postValue(models);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al cargar historial: " + e.getMessage());
            }
        }).start();
    }




    public void actualizaContratoBaseDatos(ContratoModelo model) {
        new Thread(() -> {
            try {
                long idContrato = Long.parseLong(model.getId());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long idUsuario = currentUserId != -1 ? currentUserId : 1;

                VentasContrato vc = contratoRepo.getById(idContrato);
                Timestamp originalFechaAlta = now;
                long originalIdUsuarioAlta = idUsuario;
                if (vc != null) {
                    if (vc.fechaAlta != null) originalFechaAlta = vc.fechaAlta;
                    originalIdUsuarioAlta = vc.idUsuarioAlta;
                    vc.fechaModificacion = now;
                    vc.idioma = mapeaIdiomaBD(model.getIdioma());
                    vc.estatus = "M";
                    contratoRepo.update(vc);
                }

                // ── Información General ──────────────────────────────────────────
                VentasInformacionGeneral vigActual = infoGralRepo.getByContratoId(idContrato);
                VentasInformacionGeneral vigNuevo  = new VentasInformacionGeneral(); // ✅ siempre nuevo
                vigNuevo.idContrato   = idContrato;
                vigNuevo.pais         = truncate(model.getPais(), 50);
                vigNuevo.nacionalidad = truncate(model.getNacionalidad(), 50);
                vigNuevo.tipoDir      = model.getTipoDir();

                if ("México".equalsIgnoreCase(model.getPais())) {
                    vigNuevo.calle      = truncate(model.getMexCalle(), 150);
                    vigNuevo.noExt      = truncate(model.getMexNumExt(), 10);
                    vigNuevo.noInt      = truncate(model.getMexNumInt(), 10);
                    vigNuevo.colonia    = truncate(model.getMexColonia(), 50);
                    vigNuevo.delegacion = truncate(model.getDelegacion(), 50);
                    vigNuevo.ciudad     = truncate(model.getMexCiudad(), 50);
                    vigNuevo.estado     = truncate(model.getMexEstado(), 50);
                    vigNuevo.cp         = truncate(model.getMexCP(), 15);
                } else if ("EEUU".equalsIgnoreCase(model.getPais()) || "USA".equalsIgnoreCase(model.getPais()) || (model.getPais() != null && model.getPais().contains("USA"))) {
                    vigNuevo.calle   = truncate(model.getUsaCalle(), 150);
                    vigNuevo.ciudad  = truncate(model.getUsaCity(), 50);
                    vigNuevo.estado  = truncate(model.getUsaState(), 50);
                    vigNuevo.cp      = truncate(model.getUsaZip(), 15);
                    vigNuevo.colonia = truncate(model.getUsaNeighborhood(), 50);
                    vigNuevo.poBox   = truncate(model.getPoBox(), 10);
                    vigNuevo.box     = truncate(model.getBox(), 10);
                    vigNuevo.cmr     = truncate(model.getCmr(), 10);
                    vigNuevo.apo     = truncate(model.getApo(), 10);
                } else if ("Canadá".equalsIgnoreCase(model.getPais())) {
                    vigNuevo.calle  = truncate(model.getCanCalle(), 150);
                    vigNuevo.ciudad = truncate(model.getCanCity(), 50);
                    vigNuevo.estado = truncate(model.getCanProvince(), 50);
                    vigNuevo.cp     = truncate(model.getCanPostalCode(), 15);
                } else {
                    vigNuevo.linea1 = truncate(model.getLinea1(), 150);
                    vigNuevo.linea2 = truncate(model.getLinea2(), 150);
                    vigNuevo.linea3 = truncate(model.getLinea3(), 150);
                    vigNuevo.linea4 = truncate(model.getLinea4(), 150);
                    vigNuevo.linea5 = truncate(model.getLinea5(), 150);
                    vigNuevo.pais   = truncate(model.getPaisOtro(), 50);
                }

                limpiaTelefonos(vigNuevo);
                for (ContratoModelo.InfoTelefono p : model.getTelefonos()) {
                    String cleanNum  = p.numero != null ? p.numero.replaceAll("[^0-9]", "") : "";
                    String cleanLada = p.lada   != null ? p.lada.replaceAll("[^0-9]", "")   : "";
                    if      (p.etiqueta.contains("Casa 1"))    { vigNuevo.ladaCasa1      = truncate(cleanLada, 5); vigNuevo.telefonoCasa1      = truncate(cleanNum, 15); vigNuevo.whatsAppCasa1      = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Casa 2"))    { vigNuevo.ladaCasa2      = truncate(cleanLada, 5); vigNuevo.telefonoCasa2      = truncate(cleanNum, 15); vigNuevo.whatsAppCasa2      = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 1")) { vigNuevo.ladaCelular1   = truncate(cleanLada, 5); vigNuevo.telefonoCelular1   = truncate(cleanNum, 15); vigNuevo.whatsAppCelular1   = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 2")) { vigNuevo.ladaCelular2   = truncate(cleanLada, 5); vigNuevo.telefonoCelular2   = truncate(cleanNum, 15); vigNuevo.whatsAppCelular2   = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 1")) { vigNuevo.ladaOficina1   = truncate(cleanLada, 5); vigNuevo.telefonoOficina1   = truncate(cleanNum, 15); vigNuevo.whatsAppOficina1   = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 2")) { vigNuevo.ladaOficina2   = truncate(cleanLada, 5); vigNuevo.telefonoOficina2   = truncate(cleanNum, 15); vigNuevo.whatsAppOficina2   = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Mensajes"))  { vigNuevo.ladaMensajes   = truncate(cleanLada, 5); vigNuevo.telefonoMensajes   = truncate(cleanNum, 15); vigNuevo.whatsAppMensajes   = p.isWhatsApp; }
                    if (p.esPrincipal) vigNuevo.telefonoDefault = truncate(p.etiqueta, 20);
                }

                vigNuevo.email1 = (model.getEmails().size() > 0) ? truncate(model.getEmails().get(0), 60) : null;
                vigNuevo.email2 = (model.getEmails().size() > 1) ? truncate(model.getEmails().get(1), 60) : null;
                vigNuevo.email3 = (model.getEmails().size() > 2) ? truncate(model.getEmails().get(2), 60) : null;
                vigNuevo.email4 = (model.getEmails().size() > 3) ? truncate(model.getEmails().get(3), 60) : null;

                if (vigActual == null || infoGralRepo.huboCambios(vigActual, vigNuevo)) {
                    infoGralRepo.replaceByContrato(vigNuevo, idUsuario);
                }

                // ── Titulares y Beneficiarios ────────────────────────────────────
                List<VentasTitulares> titularesActuales = titularesRepo.getByContratoId(idContrato);
                boolean titularesCambiaron     = titularesRepo.huboCambios(titularesActuales, model.getTitulares(),     "Titular");
                boolean beneficiariosCambiaron = titularesRepo.huboCambios(titularesActuales, model.getBeneficiarios(), "Beneficiario");

                if (titularesCambiaron) {
                    titularesRepo.desactivarPorTipo(idContrato, "Titular", idUsuario);
                    guardaTitulares(model.getTitulares(), idContrato, "Titular", originalIdUsuarioAlta);
                }
                if (beneficiariosCambiaron) {
                    titularesRepo.desactivarPorTipo(idContrato, "Beneficiario", idUsuario);
                    guardaTitulares(model.getBeneficiarios(), idContrato, "Beneficiario", originalIdUsuarioAlta);
                }
                // ── Inventario ───────────────────────────────────────────────────
                VentasInventario viActual = inventarioRepo.getByContratoId(idContrato);

                VentasInventario viNuevo  = new VentasInventario();
                viNuevo.idContrato              = idContrato;
                viNuevo.unidad                  = model.getUnidad();
                viNuevo.temporada               = model.getTemporada();
                viNuevo.tipoVenta               = model.getTipoVenta();
                viNuevo.tipoOcupacion           = model.getTipoOcupacion();
                viNuevo.aniosComprados          = parseInt(model.getNoAnios());
                viNuevo.primerAnioUso           = parseLong(model.getAnioUso());
                viNuevo.monedaVenta             = model.getMoneda();
                viNuevo.tipoCambioVenta         = parseDouble(model.getTipoCambio());
                viNuevo.precioBruto             = parseDouble(model.getPrecioBruto());
                viNuevo.montoCta                = parseDouble(model.getMontoCuenta());
                viNuevo.noContratosMontoCta     = parseLong(model.getNoContratosMC());
                viNuevo.precioNeto              = parseDouble(model.getPrecioNeto());
                viNuevo.tipoPago                = model.getTipoPago();
                viNuevo.engancheTotal           = parseDouble(model.getEngancheTotal());
                viNuevo.engancheTotalPorcentaje = parseDouble(model.getEnganchePorcentaje());
                viNuevo.enganchePagarSala       = parseDouble(model.getEngancheSalaMonto());
                viNuevo.enganchePagarSalaPorcentaje = parseDouble(model.getEngancheSalaPorcentaje());
                viNuevo.descuentos              = parseDouble(model.getVariosMonto());
                viNuevo.noDescuentos            = parseInt(model.getNoDesc());
                viNuevo.engancheDiferido        = parseDouble(model.getEngDiferidoMonto());
                viNuevo.noPagosEngancheDiferido = parseLong(model.getNoPagosEng());
                viNuevo.saldoEnganche           = parseDouble(model.getSaldoEnganche());
                viNuevo.tipoPagoDiferido = model.getTipoPagoEnganche();

                viNuevo.montoFinanciar          = parseDouble(model.getMontoFinanciar());
                viNuevo.costoContrato           = parseDouble(model.getCostoContrato());
                viNuevo.totalPagoSala           = parseDouble(model.getPagoSala());
                viNuevo.costoMembresia          = parseDouble(model.getCostoMembresia());
                viNuevo.comentariosRegalos      = model.getComentarios();
                viNuevo.idUsuarioAlta           = originalIdUsuarioAlta;

                if (viActual == null || inventarioRepo.huboCambios(viActual, viNuevo)) {
                    inventarioRepo.replaceByContrato(viNuevo, idUsuario);
                }
                // ── Descuentos ───────────────────────────────────────────────────
                List<VentasDescuentos> descuentosActuales = descuentosRepo.getByContratoId(idContrato);
                if (descuentosRepo.huboCambios(descuentosActuales, model.getDescuentosDetalle())) {
                    descuentosRepo.desactivarPorContrato(idContrato, idUsuario);
                    for (ContratoModelo.DescuentoDetalle dd : model.getDescuentosDetalle()) {
                        VentasDescuentos vd = new VentasDescuentos();
                        vd.idDescuento    = descuentosRepo.getNextId();
                        vd.idContrato     = idContrato;
                        vd.montoDescuento = parseDouble(dd.monto);
                        vd.descripcion    = dd.descripcion;
                        vd.idUsuarioAlta  = originalIdUsuarioAlta;
                        descuentosRepo.insert(vd);
                    }
                }

                // ── Regalos ──────────────────────────────────────────────────────
                List<VentasRegalos> regalosActuales = regalosRepo.getByContratoId(idContrato);
                if (regalosRepo.huboCambios(regalosActuales, model.getRegalos())) {
                    regalosRepo.desactivarPorContrato(idContrato, idUsuario);
                    for (String regalo : model.getRegalos()) {
                        VentasRegalos vr = new VentasRegalos();
                        vr.idRegalo      = regalosRepo.getNextId();
                        vr.idContrato    = idContrato;
                        vr.descripcion   = regalo;
                        vr.idUsuarioAlta = originalIdUsuarioAlta;
                        regalosRepo.insert(vr);
                    }
                }

                // ── Monto Cuenta ─────────────────────────────────────────────────
                List<VentasMontoCta> montoCtaActuales = montoCtaRepo.getByContratoId(idContrato);
                if (montoCtaRepo.huboCambios(montoCtaActuales, model.getContratosMontoCuenta())) {
                    montoCtaRepo.desactivarPorContrato(idContrato, idUsuario);
                    for (String xref : model.getContratosMontoCuenta()) {
                        VentasMontoCta vmc = new VentasMontoCta();
                        vmc.idMontoCta   = montoCtaRepo.getNextId();
                        vmc.idContrato   = idContrato;
                        vmc.xref         = xref;
                        vmc.idUsuarioAlta = originalIdUsuarioAlta;
                        montoCtaRepo.insert(vmc);
                    }
                }

                // ── Enganche Diferido ────────────────────────────────────────────
                List<VentasEngancheDiferido> engancheActuales = engancheDiferidoRepo.getByContratoId(idContrato);
                if (engancheDiferidoRepo.huboCambios(engancheActuales, model.getPagosDiferidos())) {
                    engancheDiferidoRepo.desactivarPorContrato(idContrato, idUsuario);
                    for (ContratoModelo.PagoDiferido pd : model.getPagosDiferidos()) {
                        VentasEngancheDiferido ved = new VentasEngancheDiferido();
                        ved.idPago        = engancheDiferidoRepo.getNextId();
                        ved.idContrato    = idContrato;
                        ved.cantidadPago  = parseDouble(pd.monto);
                        ved.fechaPago     = parseSqlDate(convertirMesANumero(pd.fecha));
                        ved.idUsuarioAlta = originalIdUsuarioAlta;
                        engancheDiferidoRepo.insert(ved);
                    }
                }

                // ── Financiamiento ───────────────────────────────────────────────
                VentasFinanciamientos vfActual = financiamientoRepo.getByContratoId(idContrato);
                VentasFinanciamientos vfNuevo  = new VentasFinanciamientos(); // ✅ siempre nuevo
                vfNuevo.idContrato      = idContrato;
                vfNuevo.tipoPeriodo     = model.getTipoPeriodo();
                vfNuevo.fechaPrimerPago = parseSqlDate(convertirMesANumero(model.getFechaPrimerPago()));
                vfNuevo.montoAFinanciar = parseDouble(model.getMontoFinanciar());
                vfNuevo.numeroPagos     = parseInt(model.getNumPagos());
                vfNuevo.tasaInteres     = parseDouble(model.getTasaInteres());
                vfNuevo.idUsuarioAlta   = originalIdUsuarioAlta;

                if (vfActual == null || financiamientoRepo.huboCambios(vfActual, vfNuevo)) {
                    financiamientoRepo.replaceByContrato(vfNuevo, idUsuario);
                }

                // ── Redes Sociales ───────────────────────────────────────────────
                VentasRedesSociales vrsActual = redesSocialesRepo.getByContratoId(idContrato);
                VentasRedesSociales vrsNuevo  = new VentasRedesSociales();
                vrsNuevo.idContrato   = idContrato;
                vrsNuevo.idUsuarioAlta = originalIdUsuarioAlta;
                if (!model.isNoRedesSociales()) {
                    for (ContratoModelo.CuentaRed sa : model.getRedesSociales()) {
                        if      ("Instagram".equalsIgnoreCase(sa.red)) vrsNuevo.usuarioInstagram = sa.usuario;
                        else if ("Facebook".equalsIgnoreCase(sa.red))  vrsNuevo.usuarioFacebook  = sa.usuario;
                        else if ("Twitter".equalsIgnoreCase(sa.red) || "X".equalsIgnoreCase(sa.red)) vrsNuevo.usuarioTwitter = sa.usuario;
                    }
                }

                if (vrsActual == null && !model.isNoRedesSociales() && !model.getRedesSociales().isEmpty()) {
                    // Primera vez — insertar
                    vrsNuevo.idRedSocial = redesSocialesRepo.getNextId();
                    redesSocialesRepo.insert(vrsNuevo);
                } else if (vrsActual != null && redesSocialesRepo.huboCambios(vrsActual, vrsNuevo)) {
                    // Hubo cambio — cancelar y crear nuevo
                    redesSocialesRepo.deleteByContratoId(idContrato, idUsuario);
                    if (!model.isNoRedesSociales() && !model.getRedesSociales().isEmpty()) {
                        vrsNuevo.idRedSocial = redesSocialesRepo.getNextId();
                        redesSocialesRepo.insert(vrsNuevo);
                    }
                }

                ContratoModelo modelFinal = getContratoValue();
                if (modelFinal != null) {
                    Contrato.postValue(modelFinal);
                }
                saveSuccess.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al actualizar: " + e.getMessage());
                saveSuccess.postValue(false);

            }
        }).start();
    }
    private void limpiaTelefonos(VentasInformacionGeneral vig) {
        vig.telefonoCasa1 = vig.ladaCasa1 = null; vig.whatsAppCasa1 = false;
        vig.telefonoCasa2 = vig.ladaCasa2 = null; vig.whatsAppCasa2 = false;
        vig.telefonoCelular1 = vig.ladaCelular1 = null; vig.whatsAppCelular1 = false;
        vig.telefonoCelular2 = vig.ladaCelular2 = null; vig.whatsAppCelular2 = false;
        vig.telefonoOficina1 = vig.ladaOficina1 = null; vig.whatsAppOficina1 = false;
        vig.telefonoOficina2 = vig.ladaOficina2 = null; vig.whatsAppOficina2 = false;
        vig.telefonoMensajes = vig.ladaMensajes = null; vig.whatsAppMensajes = false;
    }

    public void guardaContratoBaseDatos() {
        ContratoModelo model = getContratoValue();
        if (model == null) return;

        new Thread(() -> {
            try {
                long idContrato = contratoRepo.getNextId();
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long idUsuario = currentUserId != -1 ? currentUserId : 1;

                VentasContrato vc = new VentasContrato();
                vc.idContrato = idContrato;
                vc.fechaAlta = now;
                vc.idUsuarioAlta = idUsuario;
                vc.fechaModificacion = null;
                vc.estatus = "A";
                vc.idioma = mapeaIdiomaBD(model.getIdioma());
                contratoRepo.insert(vc);

                VentasInformacionGeneral vig = new VentasInformacionGeneral();
                vig.idDatosVenta = infoGralRepo.getNextId();
                vig.idContrato = idContrato;
                vig.pais = truncate(model.getPais(), 50);
                vig.tipoDir = model.getTipoDir();
                vig.nacionalidad = truncate(model.getNacionalidad(), 50);

                if ("México".equalsIgnoreCase(model.getPais())) {
                    vig.calle = truncate(model.getMexCalle(), 150); 
                    vig.noExt = truncate(model.getMexNumExt(), 10); 
                    vig.noInt = truncate(model.getMexNumInt(), 10);
                    vig.colonia = truncate(model.getMexColonia(), 50); 
                    vig.delegacion = truncate(model.getDelegacion(), 50);
                    vig.ciudad = truncate(model.getMexCiudad(), 50); 
                    vig.estado = truncate(model.getMexEstado(), 50); 
                    vig.cp = truncate(model.getMexCP(), 15);
                } else if ("EEUU".equalsIgnoreCase(model.getPais()) || "USA".equalsIgnoreCase(model.getPais()) || (model.getPais() != null && model.getPais().contains("USA"))) {
                    vig.calle = truncate(model.getUsaCalle(), 150); 
                    vig.ciudad = truncate(model.getUsaCity(), 50); 
                    vig.estado = truncate(model.getUsaState(), 50);
                    vig.cp = truncate(model.getUsaZip(), 15); 
                    vig.colonia = truncate(model.getUsaNeighborhood(), 50); 
                    vig.poBox = truncate(model.getPoBox(), 10);
                    vig.box = truncate(model.getBox(), 10);
                    vig.cmr = truncate(model.getCmr(), 10);
                    vig.apo = truncate(model.getApo(), 10);
                } else if ("Canadá".equalsIgnoreCase(model.getPais())) {
                    vig.calle = truncate(model.getCanCalle(), 150); 
                    vig.ciudad = truncate(model.getCanCity(), 50); 
                    vig.estado = truncate(model.getCanProvince(), 50); 
                    vig.cp = truncate(model.getCanPostalCode(), 15);
                } else {
                    vig.linea1 = truncate(model.getLinea1(), 150);
                    vig.linea2 = truncate(model.getLinea2(), 150);
                    vig.linea3 = truncate(model.getLinea3(), 150);
                    vig.linea4 = truncate(model.getLinea4(), 150);
                    vig.linea5 = truncate(model.getLinea5(), 150);
                    vig.pais = truncate(model.getPaisOtro(), 50);
                }

                for (ContratoModelo.InfoTelefono p : model.getTelefonos()) {
                    String cleanNum = p.numero != null ? p.numero.replaceAll("[^0-9]", "") : "";
                    String cleanLada = p.lada != null ? p.lada.replaceAll("[^0-9]", "") : "";
                    if (p.etiqueta.contains("Casa 1")) { 
                        vig.ladaCasa1 = truncate(cleanLada, 5); 
                        vig.telefonoCasa1 = truncate(cleanNum, 15); 
                        vig.whatsAppCasa1 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Casa 2")) { 
                        vig.ladaCasa2 = truncate(cleanLada, 5); 
                        vig.telefonoCasa2 = truncate(cleanNum, 15); 
                        vig.whatsAppCasa2 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Celular 1")) { 
                        vig.ladaCelular1 = truncate(cleanLada, 5); 
                        vig.telefonoCelular1 = truncate(cleanNum, 15); 
                        vig.whatsAppCelular1 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Celular 2")) { 
                        vig.ladaCelular2 = truncate(cleanLada, 5); 
                        vig.telefonoCelular2 = truncate(cleanNum, 15); 
                        vig.whatsAppCelular2 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Oficina 1")) { 
                        vig.ladaOficina1 = truncate(cleanLada, 5); 
                        vig.telefonoOficina1 = truncate(cleanNum, 15); 
                        vig.whatsAppOficina1 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Oficina 2")) { 
                        vig.ladaOficina2 = truncate(cleanLada, 5); 
                        vig.telefonoOficina2 = truncate(cleanNum, 15); 
                        vig.whatsAppOficina2 = p.isWhatsApp; 
                    }
                    else if (p.etiqueta.contains("Mensajes")) { 
                        vig.ladaMensajes = truncate(cleanLada, 5); 
                        vig.telefonoMensajes = truncate(cleanNum, 15); 
                        vig.whatsAppMensajes = p.isWhatsApp; 
                    }
                    if (p.esPrincipal) vig.telefonoDefault = truncate(p.etiqueta, 20);
                }

                List<String> emails = model.getEmails();
                if (emails.size() > 0) vig.email1 = truncate(emails.get(0), 60);
                if (emails.size() > 1) vig.email2 = truncate(emails.get(1), 60);
                if (emails.size() > 2) vig.email3 = truncate(emails.get(2), 60);
                if (emails.size() > 3) vig.email4 = truncate(emails.get(3), 60);

                vig.fechaAlta = now;
                vig.idUsuarioAlta = idUsuario;
                infoGralRepo.insert(vig);

                guardaTitulares(model.getTitulares(), idContrato, "Titular", idUsuario);
                guardaTitulares(model.getBeneficiarios(), idContrato, "Beneficiario", idUsuario);

                VentasInventario vi = new VentasInventario();
                vi.idCondicionesVenta = inventarioRepo.getNextId();
                vi.idContrato = idContrato;
                vi.unidad = model.getUnidad();
                vi.temporada = model.getTemporada();
                vi.tipoVenta = model.getTipoVenta();
                vi.tipoOcupacion = model.getTipoOcupacion();
                vi.aniosComprados = parseInt(model.getNoAnios());
                vi.primerAnioUso = parseLong(model.getAnioUso());
                vi.monedaVenta = model.getMoneda();
                vi.tipoCambioVenta = parseDouble(model.getTipoCambio());
                vi.precioBruto = parseDouble(model.getPrecioBruto());
                vi.montoCta = parseDouble(model.getMontoCuenta());
                vi.noContratosMontoCta = parseLong(model.getNoContratosMC());
                vi.precioNeto = parseDouble(model.getPrecioNeto());
                vi.tipoPago = model.getTipoPago();
                vi.engancheTotal = parseDouble(model.getEngancheTotal());
                vi.engancheTotalPorcentaje = parseDouble(model.getEnganchePorcentaje());
                vi.enganchePagarSala = parseDouble(model.getEngancheSalaMonto());
                vi.enganchePagarSalaPorcentaje = parseDouble(model.getEngancheSalaPorcentaje());
                vi.descuentos = parseDouble(model.getVariosMonto());
                vi.noDescuentos = parseInt(model.getNoDesc());
                vi.engancheDiferido = parseDouble(model.getEngDiferidoMonto());
                vi.noPagosEngancheDiferido = parseLong(model.getNoPagosEng());
                vi.saldoEnganche = parseDouble(model.getSaldoEnganche());
                vi.tipoPagoDiferido = model.getTipoPagoEnganche();
                vi.montoFinanciar = parseDouble(model.getMontoFinanciar());
                vi.costoContrato = parseDouble(model.getCostoContrato());
                vi.totalPagoSala = parseDouble(model.getPagoSala());
                vi.costoMembresia = parseDouble(model.getCostoMembresia());
                vi.comentariosRegalos = model.getComentarios();
                vi.fechaAlta = now;
                vi.idUsuarioAlta = idUsuario;
                inventarioRepo.insert(vi);

                for (ContratoModelo.DescuentoDetalle dd : model.getDescuentosDetalle()) {
                    VentasDescuentos vd = new VentasDescuentos();
                    vd.idDescuento = descuentosRepo.getNextId();
                    vd.idContrato = idContrato;
                    vd.montoDescuento = parseDouble(dd.monto);
                    vd.descripcion = dd.descripcion;
                    vd.fechaAlta = now;
                    vd.idUsuarioAlta = idUsuario;
                    descuentosRepo.insert(vd);
                }

                for (ContratoModelo.PagoDiferido pd : model.getPagosDiferidos()) {
                    VentasEngancheDiferido ved = new VentasEngancheDiferido();
                    ved.idPago = engancheDiferidoRepo.getNextId();
                    ved.idContrato = idContrato;
                    ved.cantidadPago = parseDouble(pd.monto);
                    ved.fechaPago = parseSqlDate(convertirMesANumero(pd.fecha));
                    ved.fechaAlta = now;
                    ved.idUsuarioAlta = idUsuario;
                    engancheDiferidoRepo.insert(ved);
                }

                for (String xref : model.getContratosMontoCuenta()) {
                    VentasMontoCta vmc = new VentasMontoCta();
                    vmc.idMontoCta = montoCtaRepo.getNextId();
                    vmc.idContrato = idContrato;
                    vmc.xref = xref;
                    vmc.fechaAlta = now;
                    vmc.idUsuarioAlta = idUsuario;
                    montoCtaRepo.insert(vmc);
                }

                VentasFinanciamientos vf = new VentasFinanciamientos();
                vf.idFinanciamiento = financiamientoRepo.getNextId();
                vf.idContrato = idContrato;
                vf.tipoPeriodo = model.getTipoPeriodo();
                vf.fechaPrimerPago = parseSqlDate(convertirMesANumero(model.getFechaPrimerPago()));
                vf.montoAFinanciar = parseDouble(model.getMontoFinanciar());
                vf.numeroPagos = parseInt(model.getNumPagos());
                vf.tasaInteres = parseDouble(model.getTasaInteres());
                vf.fechaAlta = now;
                vf.idUsuarioAlta = idUsuario;
                financiamientoRepo.insert(vf);

                for (String regalo : model.getRegalos()) {
                    VentasRegalos vr = new VentasRegalos();
                    vr.idRegalo = regalosRepo.getNextId();
                    vr.idContrato = idContrato;
                    vr.descripcion = regalo;
                    vr.fechaAlta = now;
                    vr.idUsuarioAlta = idUsuario;
                    regalosRepo.insert(vr);
                }

                if (model.getRedesSociales().size() > 0) {
                    VentasRedesSociales vrs = new VentasRedesSociales();
                    vrs.idRedSocial = redesSocialesRepo.getNextId();
                    vrs.idContrato = idContrato;
                    for (ContratoModelo.CuentaRed sa : model.getRedesSociales()) {
                        if ("Instagram".equalsIgnoreCase(sa.red)) vrs.usuarioInstagram = sa.usuario;
                        else if ("Facebook".equalsIgnoreCase(sa.red)) vrs.usuarioFacebook = sa.usuario;
                        else if ("Twitter".equalsIgnoreCase(sa.red) || "X".equalsIgnoreCase(sa.red)) vrs.usuarioTwitter = sa.usuario;
                    }
                    vrs.fechaAlta = now;
                    vrs.idUsuarioAlta = idUsuario;
                    redesSocialesRepo.insert(vrs);
                }

                saveSuccess.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al guardar: " + e.getMessage());
                saveSuccess.postValue(false);
            }
        }).start();
    }

    private void guardaTitulares(List<ContratoModelo.Persona> personas, long idContrato, String tipo, long idUsuarioAlta) throws SQLException {
        int tipoRegistro = "Titular".equals(tipo) ? 0 : 1;
        int orden = 1;
        for (ContratoModelo.Persona p : personas) {
            VentasTitulares vt = new VentasTitulares();
            vt.idTitular       = titularesRepo.getNextId();
            vt.idContrato      = idContrato;
            vt.nombre          = truncate(p.nombre, 50);
            vt.paterno         = truncate(p.paterno, 50);
            vt.materno         = truncate(p.materno, 50);
            vt.tipoTitular     = tipo;
            vt.tipoRegistro    = tipoRegistro;
            vt.ordenTitulares  = orden++;
            vt.ocupacion       = truncate(p.ocupacion, 50);
            vt.fechaCumpleaños = parseSqlDate(convertirMesANumero(p.cumple));
            vt.parentesco      = parseLong(p.parentesco);
            vt.idUsuarioAlta   = idUsuarioAlta;
            titularesRepo.insert(vt);
        }
    }

    private String truncate(String value, int length) {
        if (value == null) return null;
        if (value.length() <= length) return value;
        return value.substring(0, length);
    }



    private String mapeaIdiomaBD(String idioma) {
        if (idioma == null) return "ESP";
        // idioma viene del ContratoModelo, que se setea desde el fragment
        if (idioma.equalsIgnoreCase("en") || idioma.equalsIgnoreCase("English")
                || idioma.equalsIgnoreCase("ING")) return "ING";
        return "ESP";
    }

    private String mapIdiomaFromDb(String dbIdioma) {
        if (dbIdioma == null) return "Español";
        if (dbIdioma.equalsIgnoreCase("ing")) return "English";
        return "Español";
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            String clean = value.replaceAll("[^\\d.]", "");
            return clean.isEmpty() ? 0.0 : Double.parseDouble(clean);
        } catch (NumberFormatException e) { return 0.0; }
    }

    private int parseInt(String value) {
        if (value == null || value.isEmpty()) return 0;
        try { return Integer.parseInt(value.replaceAll("[^0-9]", "")); } catch (NumberFormatException e) { return 0; }
    }

    private long parseLong(String value) {
        if (value == null || value.isEmpty()) return 0;
        try { return Long.parseLong(value.replaceAll("[^0-9]", "")); } catch (NumberFormatException e) { return 0; }
    }
    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }

    /**
     * Convierte una fecha con mes en texto a formato numérico para guardar en BD.
     * Acepta tanto "15/mar/2025" (español) / "mar/15/2025" (inglés) con mes en texto,
     * como "15/03/2025" / "03/15/2025" con mes ya numérico — por si viene directo de BD.
     */
    private String convertirMesANumero(String s) {
        if (s == null || s.isEmpty()) return "";

        // Si ya viene en formato numérico DD/MM/YYYY o MM/DD/YYYY (largo 10, sin letras)
        // lo regresamos tal cual para que parseSqlDate lo procese directamente
        if (s.length() == 10 && s.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return s;
        }

        // Formato con mes en texto: largo esperado = 11 (ej. "15/mar/2025" o "mar/15/2025")
        if (s.length() != 11) return "";

        try {
            if (esIngles()) {
                // "mar/15/2025" → buscamos las primeras 3 letras como mes
                String mesPalabra = s.substring(0, 3);
                for (int i = 0; i < MESES_EN.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_EN[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);
                        return mesNumero + s.substring(3); // "03/15/2025"
                    }
                }
            } else {
                // "15/mar/2025" → buscamos las letras en posición 3-5
                String mesPalabra = s.substring(3, 6);
                for (int i = 0; i < MESES_ES.length; i++) {
                    if (mesPalabra.equalsIgnoreCase(MESES_ES[i])) {
                        String mesNumero = String.format(Locale.US, "%02d", i + 1);
                        return s.substring(0, 3) + mesNumero + s.substring(6); // "15/03/2025"
                    }
                }
            }
        } catch (Exception ignorado) {}

        return "";
    }
    private Date parseSqlDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            java.util.Date utilDate = sdf.parse(dateStr);
            if (utilDate != null) return new Date(utilDate.getTime());
        } catch (Exception e) {
            try {
                SimpleDateFormat sdfUS = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                java.util.Date utilDate = sdfUS.parse(dateStr);
                if (utilDate != null) return new Date(utilDate.getTime());
            } catch (Exception e2) { e2.printStackTrace(); }
        }
        return null;
    }
    public void setIdiomaActual(String lang) {
        ContratoModelo contrato = getContratoValue();
        if (contrato == null) return;
        contrato.setIdioma(lang);
        Contrato.setValue(contrato);
    }
    public void resetSaveState() {
        saveSuccess.setValue(null);
        errorMessage.setValue(null);
    }
    public void actualizaEstatusContrato(long idContrato, String nuevoEstatus) {
        new Thread(() -> {
            try {
                VentasContrato vc = contratoRepo.getById(idContrato);
                if (vc == null) {
                    errorMessage.postValue("Contrato no encontrado");
                    return;
                }
                vc.estatus = nuevoEstatus;
                vc.fechaModificacion = new Timestamp(System.currentTimeMillis());
                contratoRepo.update(vc);

                // Recarga el historial para reflejar el cambio
                long idUsuario = currentUserId != -1 ? currentUserId : 1;
                List<ContratoModelo> models = contratoRepo.getResumenByUserId(idUsuario);
                history.postValue(models);

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al actualizar estatus: " + e.getMessage());
            }
        }).start();
    }
    public int getLastDgTab() { return lastDgTab; }
    public void setLastDgTab(int lastDgTab) { this.lastDgTab = lastDgTab; }
    public int getLastCondTab() { return lastCondTab; }
    public void setLastCondTab(int lastCondTab) { this.lastCondTab = lastCondTab; }
}
