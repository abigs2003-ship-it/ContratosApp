package com.example.contrato;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contrato.model.*;
import com.example.contrato.repository.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class SharedContractViewModel extends ViewModel {
    private final MutableLiveData<ContratoModelo> contract = new MutableLiveData<>(new ContratoModelo());
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

    public LiveData<ContratoModelo> getContract() {
        return contract;
    }

    public void setContract(ContratoModelo contractModel) {
        contract.setValue(contractModel);
    }
    
    public ContratoModelo getContractValue() {
        return contract.getValue();
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

    public void loadHistoryFromDatabase() {
        new Thread(() -> {
            try {
                List<VentasContrato> list = contratoRepo.getAll();
                List<ContratoModelo> models = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                
                for (VentasContrato vc : list) {
                    ContratoModelo m = new ContratoModelo();
                    m.setId(String.valueOf(vc.idContrato));
                    m.setIdioma(vc.idioma);
                    if (vc.fechaAlta != null) m.setCreationDate(sdf.format(vc.fechaAlta));
                    if (vc.fechaModificacion != null) m.setModifiedDate(sdf.format(vc.fechaModificacion));

                    // 1. Titulares & Beneficiarios
                    List<VentasTitulares> tList = titularesRepo.getByContratoId(vc.idContrato);
                    if (!tList.isEmpty()) {
                        VentasTitulares mainTitular = null;
                        for (VentasTitulares vt : tList) {
                            ContratoModelo.Person p = new ContratoModelo.Person(
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
                        m.setClientName("Contrato #" + vc.idContrato);
                    }
                    
                    loadContractDetails(m, vc.idContrato);
                    models.add(m);
                }
                history.postValue(models);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.postValue("Error al cargar historial: " + e.getMessage());
            }
        }).start();
    }

    private void loadContractDetails(ContratoModelo m, long idContrato) throws SQLException {
        SimpleDateFormat dateOnlySdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        VentasInformacionGeneral vig = infoGralRepo.getByContratoId(idContrato);
        if (vig != null) {
            m.setPais(vig.pais);
            m.setProvince(vig.nacionalidad);
            m.setNoCorreo(vig.email1 == null && vig.email2 == null);

            if ("México".equalsIgnoreCase(vig.pais)) {
                m.setMexCalle(vig.calle); m.setMexNumExt(vig.noExt); m.setMexNumInt(vig.noInt);
                m.setMexColonia(vig.colonia); m.setMexMunicipio(vig.delegacion);
                m.setMexCiudad(vig.ciudad); m.setMexEstado(vig.estado); m.setMexCP(vig.cp);
            } else if ("EEUU".equalsIgnoreCase(vig.pais) || "USA".equalsIgnoreCase(vig.pais) || vig.pais.contains("USA")) {
                m.setUsaCalle(vig.calle); m.setUsaCity(vig.ciudad); m.setUsaState(vig.estado);
                m.setUsaZip(vig.cp); m.setUsaNeighborhood(vig.colonia); m.setUsaPoBox(vig.poBox);
                m.setUsaBox(vig.box); m.setUsaCmr(vig.cmr); m.setUsaApo(vig.apo);
            } else if ("Canadá".equalsIgnoreCase(vig.pais)) {
                m.setCanCalle(vig.calle); m.setCanCity(vig.ciudad); m.setCanProvince(vig.estado); m.setCanPostalCode(vig.cp);
            } else {
                m.setOtroLinea1(vig.linea1); m.setOtroLinea2(vig.linea2); m.setOtroLinea3(vig.linea3);
                m.setOtroLinea4(vig.linea4); m.setOtroLinea5(vig.linea5); m.setOtroPais(vig.pais);
            }

            if (vig.telefonoCasa1 != null && !vig.telefonoCasa1.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Casa 1", vig.ladaCasa1, vig.telefonoCasa1, vig.whatsAppCasa1, "Casa 1".equals(vig.telefonoDefault)));
            if (vig.telefonoCasa2 != null && !vig.telefonoCasa2.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Casa 2", vig.ladaCasa2, vig.telefonoCasa2, vig.whatsAppCasa2, "Casa 2".equals(vig.telefonoDefault)));
            if (vig.telefonoCelular1 != null && !vig.telefonoCelular1.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Celular 1", vig.ladaCelular1, vig.telefonoCelular1, vig.whatsAppCelular1, "Celular 1".equals(vig.telefonoDefault)));
            if (vig.telefonoCelular2 != null && !vig.telefonoCelular2.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Celular 2", vig.ladaCelular2, vig.telefonoCelular2, vig.whatsAppCelular2, "Celular 2".equals(vig.telefonoDefault)));
            if (vig.telefonoOficina1 != null && !vig.telefonoOficina1.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Oficina 1", vig.ladaOficina1, vig.telefonoOficina1, vig.whatsAppOficina1, "Oficina 1".equals(vig.telefonoDefault)));
            if (vig.telefonoOficina2 != null && !vig.telefonoOficina2.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Oficina 2", vig.ladaOficina2, vig.telefonoOficina2, vig.whatsAppOficina2, "Oficina 2".equals(vig.telefonoDefault)));
            if (vig.telefonoMensajes != null && !vig.telefonoMensajes.isEmpty()) m.getTelefonos().add(new ContratoModelo.PhoneInfo("Mensajes", vig.ladaMensajes, vig.telefonoMensajes, vig.whatsAppMensajes, "Mensajes".equals(vig.telefonoDefault)));

            if (vig.email1 != null) m.getEmails().add(vig.email1);
            if (vig.email2 != null) m.getEmails().add(vig.email2);
            if (vig.email3 != null) m.getEmails().add(vig.email3);
            if (vig.email4 != null) m.getEmails().add(vig.email4);
        }

        VentasInventario vi = inventarioRepo.getByContratoId(idContrato);
        if (vi != null) {
            m.setUnidad(vi.unidad); m.setTemporada(vi.temporada); m.setAnioUso(String.valueOf(vi.primerAnioUso));
            m.setNoAnios(String.valueOf(vi.aniosComprados)); m.setMoneda(vi.monedaVenta);
            m.setTipoCambio(String.valueOf(vi.tipoCambioVenta)); m.setPrecioBruto(String.valueOf(vi.precioBruto));
            m.setMontoCuenta(String.valueOf(vi.montoCta)); m.setPrecioNeto(String.valueOf(vi.precioNeto));
            m.setTipoPago(vi.tipoPago); m.setEngancheMonto(String.valueOf(vi.engancheTotal));
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
            if (vrs.usuarioFacebook != null) m.getRedesSociales().add(new ContratoModelo.SocialAccount("Facebook", vrs.usuarioFacebook));
            if (vrs.usuarioInstagram != null) m.getRedesSociales().add(new ContratoModelo.SocialAccount("Instagram", vrs.usuarioInstagram));
            if (vrs.usuarioTwitter != null) m.getRedesSociales().add(new ContratoModelo.SocialAccount("Twitter", vrs.usuarioTwitter));
        }
    }

    public void updateContractInDatabase(ContratoModelo model) {
        new Thread(() -> {
            try {
                long idContrato = Long.parseLong(model.getId());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long idUsuario = currentUserId != -1 ? currentUserId : 1;

                // 1. Update Main Contract
                VentasContrato vc = contratoRepo.getById(idContrato);
                if (vc != null) {
                    vc.fechaModificacion = now;
                    vc.idioma = model.getIdioma();
                    contratoRepo.update(vc);
                }

                // 2. Update Info General
                VentasInformacionGeneral vig = infoGralRepo.getByContratoId(idContrato);
                if (vig == null) vig = new VentasInformacionGeneral();
                vig.idContrato = idContrato;
                vig.pais = model.getPais();
                vig.nacionalidad = model.getProvince();
                vig.tipoDir = model.getPais();

                if ("México".equalsIgnoreCase(model.getPais())) {
                    vig.calle = model.getMexCalle(); vig.noExt = model.getMexNumExt(); vig.noInt = model.getMexNumInt();
                    vig.colonia = model.getMexColonia(); vig.delegacion = model.getMexMunicipio();
                    vig.ciudad = model.getMexCiudad(); vig.estado = model.getMexEstado(); vig.cp = model.getMexCP();
                } else if ("EEUU".equalsIgnoreCase(model.getPais()) || "USA".equalsIgnoreCase(model.getPais()) || model.getPais().contains("USA")) {
                    vig.calle = model.getUsaCalle(); vig.ciudad = model.getUsaCity(); vig.estado = model.getUsaState();
                    vig.cp = model.getUsaZip(); vig.colonia = model.getUsaNeighborhood(); vig.poBox = model.getUsaPoBox();
                    vig.box = model.getUsaBox(); vig.cmr = model.getUsaCmr(); vig.apo = model.getUsaApo();
                } else if ("Canadá".equalsIgnoreCase(model.getPais())) {
                    vig.calle = model.getCanCalle(); vig.ciudad = model.getCanCity(); vig.estado = model.getCanProvince(); vig.cp = model.getCanPostalCode();
                } else {
                    vig.linea1 = model.getOtroLinea1(); vig.linea2 = model.getOtroLinea2(); vig.linea3 = model.getOtroLinea3();
                    vig.linea4 = model.getOtroLinea4(); vig.linea5 = model.getOtroLinea5(); vig.pais = model.getOtroPais();
                }

                // Reset phones in VIG
                clearVigPhones(vig);
                for (ContratoModelo.PhoneInfo p : model.getTelefonos()) {
                    String cleanNum = p.numero != null ? p.numero.replaceAll("[^0-9]", "") : "";
                    if (p.etiqueta.contains("Casa 1")) { vig.ladaCasa1 = p.lada; vig.telefonoCasa1 = cleanNum; vig.whatsAppCasa1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Casa 2")) { vig.ladaCasa2 = p.lada; vig.telefonoCasa2 = cleanNum; vig.whatsAppCasa2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 1")) { vig.ladaCelular1 = p.lada; vig.telefonoCelular1 = cleanNum; vig.whatsAppCelular1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 2")) { vig.ladaCelular2 = p.lada; vig.telefonoCelular2 = cleanNum; vig.whatsAppCelular2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 1")) { vig.ladaOficina1 = p.lada; vig.telefonoOficina1 = cleanNum; vig.whatsAppOficina1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 2")) { vig.ladaOficina2 = p.lada; vig.telefonoOficina2 = cleanNum; vig.whatsAppOficina2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Mensajes")) { vig.ladaMensajes = p.lada; vig.telefonoMensajes = cleanNum; vig.whatsAppMensajes = p.isWhatsApp; }
                    if (p.isPrincipal) vig.telefonoDefault = p.etiqueta;
                }

                vig.email1 = (model.getEmails().size() > 0) ? model.getEmails().get(0) : null;
                vig.email2 = (model.getEmails().size() > 1) ? model.getEmails().get(1) : null;
                vig.email3 = (model.getEmails().size() > 2) ? model.getEmails().get(2) : null;
                vig.email4 = (model.getEmails().size() > 3) ? model.getEmails().get(3) : null;

                infoGralRepo.update(vig);

                // 3. Update Titulares (Delete and Re-insert)
                titularesRepo.deleteByContratoId(idContrato);
                saveTitulares(model.getTitulares(), idContrato, "T", idUsuario, now);
                saveTitulares(model.getBeneficiarios(), idContrato, "B", idUsuario, now);

                // 4. Update Inventory
                VentasInventario vi = inventarioRepo.getByContratoId(idContrato);
                if (vi != null) {
                    vi.unidad = model.getUnidad(); vi.temporada = model.getTemporada(); vi.tipoVenta = model.getTipoVenta();
                    vi.aniosComprados = parseInt(model.getNoAnios()); vi.primerAnioUso = parseLong(model.getAnioUso());
                    vi.monedaVenta = model.getMoneda(); vi.tipoCambioVenta = parseDouble(model.getTipoCambio());
                    vi.precioBruto = parseDouble(model.getPrecioBruto()); vi.montoCta = parseDouble(model.getMontoCuenta());
                    vi.precioNeto = parseDouble(model.getPrecioNeto()); vi.tipoPago = model.getTipoPago();
                    vi.engancheTotal = parseDouble(model.getEngancheMonto()); vi.engancheTotalPorcentaje = parseDouble(model.getEnganchePorcentaje());
                    vi.enganchePagarSala = parseDouble(model.getEngancheSalaMonto()); vi.enganchePagarSalaPorcentaje = parseDouble(model.getEngancheSalaPorcentaje());
                    vi.descuentos = parseDouble(model.getVariosMonto()); vi.noDescuentos = parseInt(model.getNoDesc());
                    vi.engancheDiferido = parseDouble(model.getEngDiferidoMonto()); vi.noPagosEngancheDiferido = parseLong(model.getNoPagosEng());
                    vi.saldoEnganche = parseDouble(model.getSaldoEnganche()); vi.montoFinanciar = parseDouble(model.getMontoFinanciar());
                    vi.costoContrato = parseDouble(model.getCostoContrato()); vi.totalPagoSala = parseDouble(model.getPagoSala());
                    vi.costoMembresia = parseDouble(model.getCostoMembresia()); vi.comentariosRegalos = model.getComentarios();
                    vi.noContratosMontoCta = model.getContratosMontoCuenta().size();
                    inventarioRepo.update(vi);
                }

                // 5. Update Discounts, Regalos, Monto Cta, Deferred (Delete and Re-insert)
                descuentosRepo.deleteByContratoId(idContrato);
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

                regalosRepo.deleteByContratoId(idContrato);
                for (String regalo : model.getRegalos()) {
                    VentasRegalos vr = new VentasRegalos();
                    vr.idRegalo = regalosRepo.getNextId();
                    vr.idContrato = idContrato;
                    vr.descripcion = regalo;
                    vr.fechaAlta = now;
                    vr.idUsuarioAlta = idUsuario;
                    regalosRepo.insert(vr);
                }

                montoCtaRepo.deleteByContratoId(idContrato);
                for (String xref : model.getContratosMontoCuenta()) {
                    VentasMontoCta vmc = new VentasMontoCta();
                    vmc.idMontoCta = montoCtaRepo.getNextId();
                    vmc.idContrato = idContrato;
                    vmc.xref = xref;
                    vmc.fechaAlta = now;
                    vmc.idUsuarioAlta = idUsuario;
                    montoCtaRepo.insert(vmc);
                }

                engancheDiferidoRepo.deleteByContratoId(idContrato);
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

                // 6. Update Financing
                VentasFinanciamientos vf = financiamientoRepo.getByContratoId(idContrato);
                if (vf != null) {
                    vf.tipoPeriodo = model.getTipoPeriodo();
                    vf.fechaPrimerPago = parseSqlDate(model.getFechaPrimerPago());
                    vf.montoAFinanciar = parseDouble(model.getMontoFinanciar());
                    vf.numeroPagos = parseInt(model.getNumPagos());
                    vf.tasaInteres = parseDouble(model.getTasaInteres());
                    financiamientoRepo.update(vf);
                }

                // 7. Update Social Accounts
                redesSocialesRepo.deleteByContratoId(idContrato);
                if (!model.isNoRedesSociales() && !model.getRedesSociales().isEmpty()) {
                    VentasRedesSociales vrs = new VentasRedesSociales();
                    vrs.idRedSocial = redesSocialesRepo.getNextId();
                    vrs.idContrato = idContrato;
                    for (ContratoModelo.SocialAccount sa : model.getRedesSociales()) {
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
                errorMessage.postValue("Error al actualizar: " + e.getMessage());
            }
        }).start();
    }

    private void clearVigPhones(VentasInformacionGeneral vig) {
        vig.telefonoCasa1 = vig.ladaCasa1 = null; vig.whatsAppCasa1 = false;
        vig.telefonoCasa2 = vig.ladaCasa2 = null; vig.whatsAppCasa2 = false;
        vig.telefonoCelular1 = vig.ladaCelular1 = null; vig.whatsAppCelular1 = false;
        vig.telefonoCelular2 = vig.ladaCelular2 = null; vig.whatsAppCelular2 = false;
        vig.telefonoOficina1 = vig.ladaOficina1 = null; vig.whatsAppOficina1 = false;
        vig.telefonoOficina2 = vig.ladaOficina2 = null; vig.whatsAppOficina2 = false;
        vig.telefonoMensajes = vig.ladaMensajes = null; vig.whatsAppMensajes = false;
    }

    public void saveToDatabase() {
        ContratoModelo model = getContractValue();
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
                vc.idioma = model.getIdioma();
                contratoRepo.insert(vc);

                VentasInformacionGeneral vig = new VentasInformacionGeneral();
                vig.idDatosVenta = infoGralRepo.getNextId();
                vig.idContrato = idContrato;
                vig.pais = model.getPais();
                vig.tipoDir = model.getPais(); 
                vig.nacionalidad = model.getProvince();

                if ("México".equalsIgnoreCase(model.getPais())) {
                    vig.calle = model.getMexCalle(); vig.noExt = model.getMexNumExt(); vig.noInt = model.getMexNumInt();
                    vig.colonia = model.getMexColonia(); vig.delegacion = model.getMexMunicipio();
                    vig.ciudad = model.getMexCiudad(); vig.estado = model.getMexEstado(); vig.cp = model.getMexCP();
                } else if ("EEUU".equalsIgnoreCase(model.getPais()) || "USA".equalsIgnoreCase(model.getPais()) || model.getPais().contains("USA")) {
                    vig.calle = model.getUsaCalle(); vig.ciudad = model.getUsaCity(); vig.estado = model.getUsaState();
                    vig.cp = model.getUsaZip(); vig.colonia = model.getUsaNeighborhood(); vig.poBox = model.getUsaPoBox();
                    vig.box = model.getUsaBox(); vig.cmr = model.getUsaCmr(); vig.apo = model.getUsaApo();
                } else if ("Canadá".equalsIgnoreCase(model.getPais())) {
                    vig.calle = model.getCanCalle(); vig.ciudad = model.getCanCity(); vig.estado = model.getCanProvince(); vig.cp = model.getCanPostalCode();
                } else {
                    vig.linea1 = model.getOtroLinea1(); vig.linea2 = model.getOtroLinea2(); vig.linea3 = model.getOtroLinea3();
                    vig.linea4 = model.getOtroLinea4(); vig.linea5 = model.getOtroLinea5(); vig.pais = model.getOtroPais();
                }

                for (ContratoModelo.PhoneInfo p : model.getTelefonos()) {
                    String cleanNum = p.numero != null ? p.numero.replaceAll("[^0-9]", "") : "";
                    if (p.etiqueta.contains("Casa 1")) { vig.ladaCasa1 = p.lada; vig.telefonoCasa1 = cleanNum; vig.whatsAppCasa1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Casa 2")) { vig.ladaCasa2 = p.lada; vig.telefonoCasa2 = cleanNum; vig.whatsAppCasa2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 1")) { vig.ladaCelular1 = p.lada; vig.telefonoCelular1 = cleanNum; vig.whatsAppCelular1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Celular 2")) { vig.ladaCelular2 = p.lada; vig.telefonoCelular2 = cleanNum; vig.whatsAppCelular2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 1")) { vig.ladaOficina1 = p.lada; vig.telefonoOficina1 = cleanNum; vig.whatsAppOficina1 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Oficina 2")) { vig.ladaOficina2 = p.lada; vig.telefonoOficina2 = cleanNum; vig.whatsAppOficina2 = p.isWhatsApp; }
                    else if (p.etiqueta.contains("Mensajes")) { vig.ladaMensajes = p.lada; vig.telefonoMensajes = cleanNum; vig.whatsAppMensajes = p.isWhatsApp; }
                    if (p.isPrincipal) vig.telefonoDefault = p.etiqueta;
                }

                List<String> emails = model.getEmails();
                if (emails.size() > 0) vig.email1 = emails.get(0);
                if (emails.size() > 1) vig.email2 = emails.get(1);
                if (emails.size() > 2) vig.email3 = emails.get(2);
                if (emails.size() > 3) vig.email4 = emails.get(3);

                vig.fechaAlta = now;
                vig.idUsuarioAlta = idUsuario;
                infoGralRepo.insert(vig);

                saveTitulares(model.getTitulares(), idContrato, "T", idUsuario, now);
                saveTitulares(model.getBeneficiarios(), idContrato, "B", idUsuario, now);

                VentasInventario vi = new VentasInventario();
                vi.idCondicionesVenta = inventarioRepo.getNextId();
                vi.idContrato = idContrato;
                vi.unidad = model.getUnidad();
                vi.temporada = model.getTemporada();
                vi.tipoVenta = model.getTipoVenta();
                vi.aniosComprados = parseInt(model.getNoAnios());
                vi.primerAnioUso = parseLong(model.getAnioUso());
                vi.monedaVenta = model.getMoneda();
                vi.tipoCambioVenta = parseDouble(model.getTipoCambio());
                vi.precioBruto = parseDouble(model.getPrecioBruto());
                vi.montoCta = parseDouble(model.getMontoCuenta());
                vi.noContratosMontoCta = model.getContratosMontoCuenta().size();
                vi.precioNeto = parseDouble(model.getPrecioNeto());
                vi.tipoPago = model.getTipoPago();
                vi.engancheTotal = parseDouble(model.getEngancheMonto());
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
                    for (ContratoModelo.SocialAccount sa : model.getRedesSociales()) {
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

    private void saveTitulares(List<ContratoModelo.Person> persons, long idContrato, String tipo, long idUsuario, Timestamp now) throws SQLException {
        for (ContratoModelo.Person p : persons) {
            VentasTitulares vt = new VentasTitulares();
            vt.idTitular = titularesRepo.getNextId();
            vt.idContrato = idContrato;
            vt.nombre = p.nombre;
            vt.paterno = p.paterno;
            vt.materno = p.materno;
            vt.tipoTitular = tipo;
            vt.ocupacion = p.ocupacion;
            vt.fechaCumpleaños = parseSqlDate(p.cumple);
            vt.parentesco = parseLong(p.parentesco); 
            vt.idUsuarioAlta = idUsuario;
            vt.fechaAlta = now;
            titularesRepo.insert(vt);
        }
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
