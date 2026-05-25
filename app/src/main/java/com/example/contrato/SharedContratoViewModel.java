package com.example.contrato;

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
        new Thread(() -> {
            try {
                VentasContrato vc = contratoRepo.getById(idContrato);
                if (vc != null) {
                    ContratoModelo m = new ContratoModelo();
                    m.setId(String.valueOf(vc.idContrato));
                    m.setIdioma(mapIdiomaFromDb(vc.idioma));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    if (vc.fechaAlta != null) m.setFechaCreacion(sdf.format(vc.fechaAlta));
                    if (vc.fechaModificacion != null) m.setFechaModificacion(sdf.format(vc.fechaModificacion));

                    cargaDetallesTitulares(m, vc.idContrato);
                    cargaDetallesContrato(m, vc.idContrato);
                    Contrato.postValue(m);
                } else {
                    errorMessage.postValue("Contrato no encontrado");
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al cargar contrato: " + e.getMessage());
            }
        }).start();
    }

    private void cargaDetallesTitulares(ContratoModelo m, long idContrato) throws SQLException {
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        List<VentasTitulares> tList = titularesRepo.getByContratoId(idContrato);
        if (!tList.isEmpty()) {
            VentasTitulares mainTitular = null;
            for (VentasTitulares vt : tList) {
                ContratoModelo.Persona p = new ContratoModelo.Persona(
                    vt.nombre, vt.paterno, vt.materno, vt.ocupacion, 
                    String.valueOf(vt.parentesco),
                    vt.fechaCumpleaños != null ? dateOnlySdf.format(vt.fechaCumpleaños) : ""
                );
                if ("T".equalsIgnoreCase(vt.tipoTitular)) {
                    m.getTitulares().add(p);
                    if (mainTitular == null) mainTitular = vt;
                } else {
                    m.getBeneficiarios().add(p);
                }
            }
            if (mainTitular != null) {
                m.setClientName((mainTitular.nombre + " " + (mainTitular.paterno != null ? mainTitular.paterno : "")).trim());
            }
        } else {
            m.setClientName("Contrato #" + idContrato);
        }
    }

    public void cargaHistorialBaseDatos(long UsuarioId) {
        new Thread(() -> {
            try {
                List<VentasContrato> list = contratoRepo.getByUserId(UsuarioId);
                List<ContratoModelo> models = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                
                for (VentasContrato vc : list) {
                    ContratoModelo m = new ContratoModelo();
                    m.setEstatus(vc.estatus);
                    m.setId(String.valueOf(vc.idContrato));
                    m.setIdioma(mapIdiomaFromDb(vc.idioma));
                    if (vc.fechaAlta != null) m.setFechaCreacion(sdf.format(vc.fechaAlta));
                    if (vc.fechaModificacion != null) m.setFechaModificacion(sdf.format(vc.fechaModificacion));

                    cargaDetallesTitulares(m, vc.idContrato);
                    cargaDetallesContrato(m, vc.idContrato);
                    models.add(m);
                }
                history.postValue(models);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al cargar historial: " + e.getMessage());
            }
        }).start();
    }

    private void cargaDetallesContrato(ContratoModelo m, long idContrato) throws SQLException {
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        VentasInformacionGeneral vig = infoGralRepo.getByContratoId(idContrato);
        if (vig != null) {
            m.setPais(vig.pais);
            m.setNacionalidad(vig.nacionalidad);
            m.setTipoDir(vig.tipoDir);
            m.setNoCorreo(vig.email1 == null && vig.email2 == null);

            if ("México".equalsIgnoreCase(vig.pais)) {
                m.setMexCalle(vig.calle); m.setMexNumExt(vig.noExt); m.setMexNumInt(vig.noInt);
                m.setMexColonia(vig.colonia); m.setDelegacion(vig.delegacion);
                m.setMexCiudad(vig.ciudad); m.setMexEstado(vig.estado); m.setMexCP(vig.cp);
            } else if ("EEUU".equalsIgnoreCase(vig.pais) || "USA".equalsIgnoreCase(vig.pais) || (vig.pais != null && vig.pais.contains("USA"))) {
                m.setUsaCalle(vig.calle); m.setUsaCity(vig.ciudad); m.setUsaState(vig.estado);
                m.setUsaZip(vig.cp); m.setUsaNeighborhood(vig.colonia); m.setPoBox(vig.poBox);
                m.setBox(vig.box); m.setCmr(vig.cmr); m.setApo(vig.apo);
            } else if ("Canadá".equalsIgnoreCase(vig.pais)) {
                m.setCanCalle(vig.calle); m.setCanCity(vig.ciudad); m.setCanProvince(vig.estado); m.setCanPostalCode(vig.cp); m.setPais(vig.pais);
            } else {
                m.setLinea1(vig.linea1); m.setLinea2(vig.linea2); m.setLinea3(vig.linea3);
                m.setLinea4(vig.linea4); m.setLinea5(vig.linea5); m.setPaisOtro(vig.pais);
            }

            if (vig.telefonoCasa1 != null && !vig.telefonoCasa1.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Casa 1", vig.ladaCasa1, vig.telefonoCasa1, vig.whatsAppCasa1, "Casa 1".equals(vig.telefonoDefault)));
            if (vig.telefonoCasa2 != null && !vig.telefonoCasa2.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Casa 2", vig.ladaCasa2, vig.telefonoCasa2, vig.whatsAppCasa2, "Casa 2".equals(vig.telefonoDefault)));
            if (vig.telefonoCelular1 != null && !vig.telefonoCelular1.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Celular 1", vig.ladaCelular1, vig.telefonoCelular1, vig.whatsAppCelular1, "Celular 1".equals(vig.telefonoDefault)));
            if (vig.telefonoCelular2 != null && !vig.telefonoCelular2.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Celular 2", vig.ladaCelular2, vig.telefonoCelular2, vig.whatsAppCelular2, "Celular 2".equals(vig.telefonoDefault)));
            if (vig.telefonoOficina1 != null && !vig.telefonoOficina1.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Oficina 1", vig.ladaOficina1, vig.telefonoOficina1, vig.whatsAppOficina1, "Oficina 1".equals(vig.telefonoDefault)));
            if (vig.telefonoOficina2 != null && !vig.telefonoOficina2.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Oficina 2", vig.ladaOficina2, vig.telefonoOficina2, vig.whatsAppOficina2, "Oficina 2".equals(vig.telefonoDefault)));
            if (vig.telefonoMensajes != null && !vig.telefonoMensajes.isEmpty()) m.getTelefonos().add(new ContratoModelo.InfoTelefono("Mensajes", vig.ladaMensajes, vig.telefonoMensajes, vig.whatsAppMensajes, "Mensajes".equals(vig.telefonoDefault)));

            if (vig.email1 != null) m.getEmails().add(vig.email1);
            if (vig.email2 != null) m.getEmails().add(vig.email2);
            if (vig.email3 != null) m.getEmails().add(vig.email3);
            if (vig.email4 != null) m.getEmails().add(vig.email4);
        }

        VentasInventario vi = inventarioRepo.getByContratoId(idContrato);
        if (vi != null) {
            m.setUnidad(vi.unidad); m.setTemporada(vi.temporada); m.setAnioUso(String.valueOf(vi.primerAnioUso));
            m.setTipoOcupacion(vi.tipoOcupacion);
            m.setNoAnios(String.valueOf(vi.aniosComprados)); m.setMoneda(vi.monedaVenta);
            m.setTipoCambio(String.valueOf(vi.tipoCambioVenta)); m.setPrecioBruto(String.valueOf(vi.precioBruto));
            m.setMontoCuenta(String.valueOf(vi.montoCta)); m.setPrecioNeto(String.valueOf(vi.precioNeto));
            m.setNoContratosMC(String.valueOf(vi.noContratosMontoCta));
            m.setTipoPago(vi.tipoPago); m.setEngancheTotal(String.valueOf(vi.engancheTotal));
            m.setEnganchePorcentaje(String.valueOf(vi.engancheTotalPorcentaje));
            m.setEngancheSalaMonto(String.valueOf(vi.enganchePagarSala));
            m.setEngancheSalaPorcentaje(String.valueOf(vi.enganchePagarSalaPorcentaje));
            m.setVariosMonto(String.valueOf(vi.descuentos)); m.setNoDesc(String.valueOf(vi.noDescuentos));
            m.setEngDiferidoMonto(String.valueOf(vi.engancheDiferido)); m.setNoPagosEng(String.valueOf(vi.noPagosEngancheDiferido));
            m.setSaldoEnganche(String.valueOf(vi.saldoEnganche)); m.setMontoFinanciar(String.valueOf(vi.montoFinanciar));
            m.setCostoContrato(String.valueOf(vi.costoContrato)); m.setPagoSala(String.valueOf(vi.totalPagoSala));
            m.setCostoMembresia(String.valueOf(vi.costoMembresia)); m.setComentarios(vi.comentariosRegalos);
            m.setTipoVenta(vi.tipoVenta);
        }

        List<VentasDescuentos> discounts = descuentosRepo.getByContratoId(idContrato);
        for (VentasDescuentos vd : discounts) {
            m.getDescuentosDetalle().add(new ContratoModelo.DescuentoDetalle(String.valueOf(vd.montoDescuento), vd.descripcion));
        }

        List<VentasEngancheDiferido> payments = engancheDiferidoRepo.getByContratoId(idContrato);
        for (VentasEngancheDiferido vp : payments) {
            m.getPagosDiferidos().add(new ContratoModelo.PagoDiferido(String.valueOf(vp.cantidadPago), vp.fechaPago != null ? dateOnlySdf.format(vp.fechaPago) : ""));
        }

        List<VentasMontoCta> montosCta = montoCtaRepo.getByContratoId(idContrato);
        for (VentasMontoCta vmc : montosCta) {
            m.getContratosMontoCuenta().add(vmc.xref);
        }

        VentasFinanciamientos vf = financiamientoRepo.getByContratoId(idContrato);
        if (vf != null) {
            m.setTipoPeriodo(vf.tipoPeriodo);
            m.setFechaPrimerPago(vf.fechaPrimerPago != null ? dateOnlySdf.format(vf.fechaPrimerPago) : "");
            m.setNumPagos(String.valueOf(vf.numeroPagos));
            m.setTasaInteres(String.valueOf(vf.tasaInteres));
        }

        List<VentasRegalos> gifts = regalosRepo.getByContratoId(idContrato);
        for (VentasRegalos vr : gifts) {
            m.getRegalos().add(vr.descripcion);
        }

        VentasRedesSociales vrs = redesSocialesRepo.getByContratoId(idContrato);
        if (vrs != null) {
            if (vrs.usuarioFacebook != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Facebook", vrs.usuarioFacebook));
            if (vrs.usuarioInstagram != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Instagram", vrs.usuarioInstagram));
            if (vrs.usuarioTwitter != null) m.getRedesSociales().add(new ContratoModelo.CuentaRed("Twitter", vrs.usuarioTwitter));
        }
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
                    vc.idioma = mapIdiomaToDb(model.getIdioma());
                    vc.estatus = model.getEstatus();
                    contratoRepo.update(vc);
                }

                VentasInformacionGeneral vig = infoGralRepo.getByContratoId(idContrato);
                if (vig == null) vig = new VentasInformacionGeneral();
                vig.idContrato = idContrato;
                vig.pais = truncate(model.getPais(), 50);
                vig.nacionalidad = truncate(model.getNacionalidad(), 50);
                vig.tipoDir = model.getTipoDir();

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

                limpiaTelefonos(vig);
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

                vig.email1 = (model.getEmails().size() > 0) ? truncate(model.getEmails().get(0), 60) : null;
                vig.email2 = (model.getEmails().size() > 1) ? truncate(model.getEmails().get(1), 60) : null;
                vig.email3 = (model.getEmails().size() > 2) ? truncate(model.getEmails().get(2), 60) : null;
                vig.email4 = (model.getEmails().size() > 3) ? truncate(model.getEmails().get(3), 60) : null;

                infoGralRepo.update(vig);

                titularesRepo.deleteByContratoId(idContrato);
                guardaTitulares(model.getTitulares(), idContrato, "T", originalIdUsuarioAlta, originalFechaAlta);
                guardaTitulares(model.getBeneficiarios(), idContrato, "B", originalIdUsuarioAlta, originalFechaAlta);

                VentasInventario vi = inventarioRepo.getByContratoId(idContrato);
                if (vi != null) {

                    vi.unidad = model.getUnidad(); vi.temporada = model.getTemporada(); vi.tipoVenta = model.getTipoVenta();
                    vi.tipoOcupacion = model.getTipoOcupacion();
                    vi.aniosComprados = parseInt(model.getNoAnios()); vi.primerAnioUso = parseLong(model.getAnioUso());
                    vi.monedaVenta = model.getMoneda(); vi.tipoCambioVenta = parseDouble(model.getTipoCambio());
                    vi.precioBruto = parseDouble(model.getPrecioBruto()); vi.montoCta = parseDouble(model.getMontoCuenta());
                    vi.noContratosMontoCta = parseLong(model.getNoContratosMC());
                    vi.precioNeto = parseDouble(model.getPrecioNeto()); vi.tipoPago = model.getTipoPago();
                    vi.engancheTotal = parseDouble(model.getEngancheTotal()); vi.engancheTotalPorcentaje = parseDouble(model.getEnganchePorcentaje());
                    vi.enganchePagarSala = parseDouble(model.getEngancheSalaMonto()); vi.enganchePagarSalaPorcentaje = parseDouble(model.getEngancheSalaPorcentaje());
                    vi.descuentos = parseDouble(model.getVariosMonto()); vi.noDescuentos = parseInt(model.getNoDesc());
                    vi.engancheDiferido = parseDouble(model.getEngDiferidoMonto()); vi.noPagosEngancheDiferido = parseLong(model.getNoPagosEng());
                    vi.saldoEnganche = parseDouble(model.getSaldoEnganche()); vi.montoFinanciar = parseDouble(model.getMontoFinanciar());
                    vi.costoContrato = parseDouble(model.getCostoContrato()); vi.totalPagoSala = parseDouble(model.getPagoSala());
                    vi.costoMembresia = parseDouble(model.getCostoMembresia()); vi.comentariosRegalos = model.getComentarios();
                    inventarioRepo.update(vi);
                }

                descuentosRepo.deleteByContratoId(idContrato);
                for (ContratoModelo.DescuentoDetalle dd : model.getDescuentosDetalle()) {
                    VentasDescuentos vd = new VentasDescuentos();
                    vd.idDescuento = descuentosRepo.getNextId();
                    vd.idContrato = idContrato;
                    vd.montoDescuento = parseDouble(dd.monto);
                    vd.descripcion = dd.descripcion;
                    vd.fechaAlta = originalFechaAlta;
                    vd.idUsuarioAlta = originalIdUsuarioAlta;
                    descuentosRepo.insert(vd);
                }

                regalosRepo.deleteByContratoId(idContrato);
                for (String regalo : model.getRegalos()) {
                    VentasRegalos vr = new VentasRegalos();
                    vr.idRegalo = regalosRepo.getNextId();
                    vr.idContrato = idContrato;
                    vr.descripcion = regalo;
                    vr.fechaAlta = originalFechaAlta;
                    vr.idUsuarioAlta = originalIdUsuarioAlta;
                    regalosRepo.insert(vr);
                }

                montoCtaRepo.deleteByContratoId(idContrato);
                for (String xref : model.getContratosMontoCuenta()) {
                    VentasMontoCta vmc = new VentasMontoCta();
                    vmc.idMontoCta = montoCtaRepo.getNextId();
                    vmc.idContrato = idContrato;
                    vmc.xref = xref;
                    vmc.fechaAlta = originalFechaAlta;
                    vmc.idUsuarioAlta = originalIdUsuarioAlta;
                    montoCtaRepo.insert(vmc);
                }

                engancheDiferidoRepo.deleteByContratoId(idContrato);
                for (ContratoModelo.PagoDiferido pd : model.getPagosDiferidos()) {
                    VentasEngancheDiferido ved = new VentasEngancheDiferido();
                    ved.idPago = engancheDiferidoRepo.getNextId();
                    ved.idContrato = idContrato;
                    ved.cantidadPago = parseDouble(pd.monto);
                    ved.fechaPago = parseSqlDate(pd.fecha);
                    ved.fechaAlta = originalFechaAlta;
                    ved.idUsuarioAlta = originalIdUsuarioAlta;
                    engancheDiferidoRepo.insert(ved);
                }

                VentasFinanciamientos vf = financiamientoRepo.getByContratoId(idContrato);
                if (vf != null) {
                    vf.tipoPeriodo = model.getTipoPeriodo();
                    vf.fechaPrimerPago = parseSqlDate(model.getFechaPrimerPago());
                    vf.montoAFinanciar = parseDouble(model.getMontoFinanciar());
                    vf.numeroPagos = parseInt(model.getNumPagos());
                    vf.tasaInteres = parseDouble(model.getTasaInteres());
                    financiamientoRepo.update(vf);
                }

                redesSocialesRepo.deleteByContratoId(idContrato);
                if (!model.isNoRedesSociales() && !model.getRedesSociales().isEmpty()) {
                    VentasRedesSociales vrs = new VentasRedesSociales();
                    vrs.idRedSocial = redesSocialesRepo.getNextId();
                    vrs.idContrato = idContrato;
                    for (ContratoModelo.CuentaRed sa : model.getRedesSociales()) {
                        if ("Instagram".equalsIgnoreCase(sa.red)) vrs.usuarioInstagram = sa.usuario;
                        else if ("Facebook".equalsIgnoreCase(sa.red)) vrs.usuarioFacebook = sa.usuario;
                        else if ("Twitter".equalsIgnoreCase(sa.red) || "X".equalsIgnoreCase(sa.red)) vrs.usuarioTwitter = sa.usuario;
                    }
                    vrs.fechaAlta = originalFechaAlta;
                    vrs.idUsuarioAlta = originalIdUsuarioAlta;
                    redesSocialesRepo.insert(vrs);
                }

                saveSuccess.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al actualizar: " + e.getMessage());
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
                vc.fechaModificacion = now;
                vc.estatus = "A";
                vc.idioma = mapIdiomaToDb(model.getIdioma());
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

                guardaTitulares(model.getTitulares(), idContrato, "T", idUsuario, now);
                guardaTitulares(model.getBeneficiarios(), idContrato, "B", idUsuario, now);

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
                    ved.fechaPago = parseSqlDate(pd.fecha);
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
                vf.fechaPrimerPago = parseSqlDate(model.getFechaPrimerPago());
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

    private void guardaTitulares(List<ContratoModelo.Persona> Personas, long idContrato, String tipo, long idUsuario, Timestamp now) throws SQLException {
        for (ContratoModelo.Persona p : Personas) {
            VentasTitulares vt = new VentasTitulares();
            vt.idTitular = titularesRepo.getNextId();
            vt.idContrato = idContrato;
            vt.nombre = truncate(p.nombre, 50); 
            vt.paterno = truncate(p.paterno, 50);
            vt.materno = truncate(p.materno, 50);
            vt.tipoTitular = tipo;
            vt.ocupacion = truncate(p.ocupacion, 50);
            vt.fechaCumpleaños = parseSqlDate(p.cumple);
            vt.parentesco = parseLong(p.parentesco); 
            vt.idUsuarioAlta = idUsuario;
            vt.fechaAlta = now;
            titularesRepo.insert(vt);
        }
    }

    private String truncate(String value, int length) {
        if (value == null) return null;
        if (value.length() <= length) return value;
        return value.substring(0, length);
    }



    private String mapIdiomaToDb(String idioma) {
        if (idioma == null) return "es";
        if (idioma.equalsIgnoreCase("English")) return "eng";
        return "es";
    }

    private String mapIdiomaFromDb(String dbIdioma) {
        if (dbIdioma == null) return "Español";
        if (dbIdioma.equalsIgnoreCase("eng")) return "English";
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

    public int getLastDgTab() { return lastDgTab; }
    public void setLastDgTab(int lastDgTab) { this.lastDgTab = lastDgTab; }
    public int getLastCondTab() { return lastCondTab; }
    public void setLastCondTab(int lastCondTab) { this.lastCondTab = lastCondTab; }
}
