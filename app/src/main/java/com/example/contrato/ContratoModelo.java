package com.example.contrato;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContratoModelo implements Serializable {
    private String id;
    private String clientName;
    private String creationDate;
    private String modifiedDate;
    private String idioma = "Español";
    
    // Titulares y Beneficiarios
    private List<Persona> titulares = new ArrayList<>();
    private List<Persona> beneficiarios = new ArrayList<>();

    // Dirección (Campos generales)
    private String pais;
    private String calle;
    private String ciudad;
    private String estado;
    private String cp;
    private String colonia;
    private String municipio;
    private String province;
    private String poBox;

    // Dirección específica por país
    private String mexCalle, mexNumExt, mexNumInt, mexColonia, mexMunicipio, mexCiudad, mexEstado, mexCP;
    private String usaCalle, usaCity, usaState, usaZip, usaNeighborhood, usaPoBox, usaBox, usaCmr, usaApo;
    private String canCalle, canCity, canProvince, canPostalCode;
    private String otroLinea1, otroLinea2, otroLinea3, otroLinea4, otroLinea5, otroPais;
    
    // Contacto
    private List<InfoTelefono> telefonos = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private boolean noCorreo;

    // Redes Sociales
    private List<SocialAccount> redesSociales = new ArrayList<>();
    private boolean noRedesSociales;

    // Datos Venta
    private String tipoVenta;
    private String unidad;
    private String temporada;
    private String anioUso;
    private String noAnios;
    private String moneda;
    private String tipoCambio;
    private String precioBruto;
    private String montoCuenta;
    private String precioNeto;
    private String tipoPago;
    private String engancheTotal;
    private String enganchePorcentaje;
    private String engancheMonto;
    private String engancheSalaMonto;
    private String engancheSalaPorcentaje;
    private String variosMonto;
    private String noDesc;
    private String engDiferidoMonto;
    private String noPagosEng;
    private String saldoEnganche;
    private String montoFinanciar;
    private String costoContrato;
    private String pagoSala;
    private String costoMembresia;
    private String comentarios;
    
    private List<PagoDiferido> pagosDiferidos = new ArrayList<>();
    private List<DescuentoDetalle> descuentosDetalle = new ArrayList<>();
    private List<String> contratosMontoCuenta = new ArrayList<>();

    // Financiamiento
    private String tipoPeriodo;
    private String fechaPrimerPago;
    private String numPagos;
    private String tasaInteres;
    
    private List<String> regalos = new ArrayList<>();

    public ContratoModelo() {
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public static class Persona implements Serializable {
        public String nombre, paterno, materno, ocupacion, parentesco, cumple;
        public Persona() {}
        public Persona(String n, String p, String m, String o, String par, String c) {
            this.nombre = n; this.paterno = p; this.materno = m;
            this.ocupacion = o; this.parentesco = par; this.cumple = c;
        }
    }

    public static class InfoTelefono implements Serializable {
        public String etiqueta, lada, numero;
        public boolean isWhatsApp, isPrincipal;
        public InfoTelefono() {}
        public InfoTelefono(String etiqueta, String lada, String numero, boolean isWhatsApp, boolean isPrincipal) {
            this.etiqueta = etiqueta;
            this.lada = lada;
            this.numero = numero;
            this.isWhatsApp = isWhatsApp;
            this.isPrincipal = isPrincipal;
        }
    }

    public static class SocialAccount implements Serializable {
        public String red, usuario;
        public SocialAccount() {}
        public SocialAccount(String r, String u) { this.red = r; this.usuario = u; }
    }

    public static class PagoDiferido implements Serializable {
        public String monto;
        public String fecha;
        public PagoDiferido(String m, String f) { this.monto = m; this.fecha = f; }
    }

    public static class DescuentoDetalle implements Serializable {
        public String monto;
        public String descripcion;
        public DescuentoDetalle(String m, String d) { this.monto = m; this.descripcion = d; }
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public List<Persona> getTitulares() { return titulares; }
    public void setTitulares(List<Persona> titulares) { this.titulares = titulares; }
    public List<Persona> getBeneficiarios() { return beneficiarios; }
    public void setBeneficiarios(List<Persona> beneficiarios) { this.beneficiarios = beneficiarios; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCp() { return cp; }
    public void setCp(String cp) { this.cp = cp; }
    public String getColonia() { return colonia; }
    public void setColonia(String colonia) { this.colonia = colonia; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPoBox() { return poBox; }
    public void setPoBox(String poBox) { this.poBox = poBox; }
    
    public String getMexCalle() { return mexCalle; }
    public void setMexCalle(String mexCalle) { this.mexCalle = mexCalle; }
    public String getMexNumExt() { return mexNumExt; }
    public void setMexNumExt(String mexNumExt) { this.mexNumExt = mexNumExt; }
    public String getMexNumInt() { return mexNumInt; }
    public void setMexNumInt(String mexNumInt) { this.mexNumInt = mexNumInt; }
    public String getMexColonia() { return mexColonia; }
    public void setMexColonia(String mexColonia) { this.mexColonia = mexColonia; }
    public String getMexMunicipio() { return mexMunicipio; }
    public void setMexMunicipio(String mexMunicipio) { this.mexMunicipio = mexMunicipio; }
    public String getMexCiudad() { return mexCiudad; }
    public void setMexCiudad(String mexCiudad) { this.mexCiudad = mexCiudad; }
    public String getMexEstado() { return mexEstado; }
    public void setMexEstado(String mexEstado) { this.mexEstado = mexEstado; }
    public String getMexCP() { return mexCP; }
    public void setMexCP(String mexCP) { this.mexCP = mexCP; }

    public String getUsaCalle() { return usaCalle; }
    public void setUsaCalle(String usaCalle) { this.usaCalle = usaCalle; }
    public String getUsaCity() { return usaCity; }
    public void setUsaCity(String usaCity) { this.usaCity = usaCity; }
    public String getUsaState() { return usaState; }
    public void setUsaState(String usaState) { this.usaState = usaState; }
    public String getUsaZip() { return usaZip; }
    public void setUsaZip(String usaZip) { this.usaZip = usaZip; }
    public String getUsaNeighborhood() { return usaNeighborhood; }
    public void setUsaNeighborhood(String usaNeighborhood) { this.usaNeighborhood = usaNeighborhood; }
    public String getUsaPoBox() { return usaPoBox; }
    public void setUsaPoBox(String usaPoBox) { this.usaPoBox = usaPoBox; }
    public String getUsaBox() { return usaBox; }
    public void setUsaBox(String usaBox) { this.usaBox = usaBox; }
    public String getUsaCmr() { return usaCmr; }
    public void setUsaCmr(String usaCmr) { this.usaCmr = usaCmr; }
    public String getUsaApo() { return usaApo; }
    public void setUsaApo(String usaApo) { this.usaApo = usaApo; }

    public String getCanCalle() { return canCalle; }
    public void setCanCalle(String canCalle) { this.canCalle = canCalle; }
    public String getCanCity() { return canCity; }
    public void setCanCity(String canCity) { this.canCity = canCity; }
    public String getCanProvince() { return canProvince; }
    public void setCanProvince(String canProvince) { this.canProvince = canProvince; }
    public String getCanPostalCode() { return canPostalCode; }
    public void setCanPostalCode(String canPostalCode) { this.canPostalCode = canPostalCode; }

    public String getOtroLinea1() { return otroLinea1; }
    public void setOtroLinea1(String otroLinea1) { this.otroLinea1 = otroLinea1; }
    public String getOtroLinea2() { return otroLinea2; }
    public void setOtroLinea2(String otroLinea2) { this.otroLinea2 = otroLinea2; }
    public String getOtroLinea3() { return otroLinea3; }
    public void setOtroLinea3(String otroLinea3) { this.otroLinea3 = otroLinea3; }
    public String getOtroLinea4() { return otroLinea4; }
    public void setOtroLinea4(String otroLinea4) { this.otroLinea4 = otroLinea4; }
    public String getOtroLinea5() { return otroLinea5; }
    public void setOtroLinea5(String otroLinea5) { this.otroLinea5 = otroLinea5; }
    public String getOtroPais() { return otroPais; }
    public void setOtroPais(String otroPais) { this.otroPais = otroPais; }

    public List<InfoTelefono> getTelefonos() { return telefonos; }
    public void setTelefonos(List<InfoTelefono> telefonos) { this.telefonos = telefonos; }
    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public boolean isNoCorreo() { return noCorreo; }
    public void setNoCorreo(boolean noCorreo) { this.noCorreo = noCorreo; }
    public List<SocialAccount> getRedesSociales() { return redesSociales; }
    public void setRedesSociales(List<SocialAccount> redesSociales) { this.redesSociales = redesSociales; }
    public boolean isNoRedesSociales() { return noRedesSociales; }
    public void setNoRedesSociales(boolean noRedesSociales) { this.noRedesSociales = noRedesSociales; }

    public String getTipoVenta() { return tipoVenta; }
    public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }
    public String getAnioUso() { return anioUso; }
    public void setAnioUso(String anioUso) { this.anioUso = anioUso; }
    public String getNoAnios() { return noAnios; }
    public void setNoAnios(String noAnios) { this.noAnios = noAnios; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(String tipoCambio) { this.tipoCambio = tipoCambio; }
    public String getPrecioBruto() { return precioBruto; }
    public void setPrecioBruto(String precioBruto) { this.precioBruto = precioBruto; }
    public String getMontoCuenta() { return montoCuenta; }
    public void setMontoCuenta(String montoCuenta) { this.montoCuenta = montoCuenta; }
    public String getPrecioNeto() { return precioNeto; }
    public void setPrecioNeto(String precioNeto) { this.precioNeto = precioNeto; }
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }
    public String getEngancheTotal() { return engancheTotal; }
    public void setEngancheTotal(String engancheTotal) { this.engancheTotal = engancheTotal; }
    public String getEnganchePorcentaje() { return enganchePorcentaje; }
    public void setEnganchePorcentaje(String enganchePorcentaje) { this.enganchePorcentaje = enganchePorcentaje; }
    public String getEngancheMonto() { return engancheMonto; }
    public void setEngancheMonto(String engancheMonto) { this.engancheMonto = engancheMonto; }
    public String getEngancheSalaMonto() { return engancheSalaMonto; }
    public void setEngancheSalaMonto(String engancheSalaMonto) { this.engancheSalaMonto = engancheSalaMonto; }
    public String getEngancheSalaPorcentaje() { return engancheSalaPorcentaje; }
    public void setEngancheSalaPorcentaje(String engancheSalaPorcentaje) { this.engancheSalaPorcentaje = engancheSalaPorcentaje; }
    public String getVariosMonto() { return variosMonto; }
    public void setVariosMonto(String variosMonto) { this.variosMonto = variosMonto; }
    public String getNoDesc() { return noDesc; }
    public void setNoDesc(String noDesc) { this.noDesc = noDesc; }
    public String getEngDiferidoMonto() { return engDiferidoMonto; }
    public void setEngDiferidoMonto(String engDiferidoMonto) { this.engDiferidoMonto = engDiferidoMonto; }
    public String getNoPagosEng() { return noPagosEng; }
    public void setNoPagosEng(String noPagosEng) { this.noPagosEng = noPagosEng; }
    public String getSaldoEnganche() { return saldoEnganche; }
    public void setSaldoEnganche(String saldoEnganche) { this.saldoEnganche = saldoEnganche; }
    public String getMontoFinanciar() { return montoFinanciar; }
    public void setMontoFinanciar(String montoFinanciar) { this.montoFinanciar = montoFinanciar; }
    public String getCostoContrato() { return costoContrato; }
    public void setCostoContrato(String costoContrato) { this.costoContrato = costoContrato; }
    public String getPagoSala() { return pagoSala; }
    public void setPagoSala(String pagoSala) { this.pagoSala = pagoSala; }
    public String getCostoMembresia() { return costoMembresia; }
    public void setCostoMembresia(String costoMembresia) { this.costoMembresia = costoMembresia; }
    
    public List<PagoDiferido> getPagosDiferidos() { return pagosDiferidos; }
    public void setPagosDiferidos(List<PagoDiferido> pagosDiferidos) { this.pagosDiferidos = pagosDiferidos; }
    
    public List<DescuentoDetalle> getDescuentosDetalle() { return descuentosDetalle; }
    public void setDescuentosDetalle(List<DescuentoDetalle> descuentosDetalle) { this.descuentosDetalle = descuentosDetalle; }
    
    public List<String> getContratosMontoCuenta() { return contratosMontoCuenta; }
    public void setContratosMontoCuenta(List<String> contratosMontoCuenta) { this.contratosMontoCuenta = contratosMontoCuenta; }

    public String getTipoPeriodo() { return tipoPeriodo; }
    public void setTipoPeriodo(String tipoPeriodo) { this.tipoPeriodo = tipoPeriodo; }
    public String getFechaPrimerPago() { return fechaPrimerPago; }
    public void setFechaPrimerPago(String fechaPrimerPago) { this.fechaPrimerPago = fechaPrimerPago; }
    public String getNumPagos() { return numPagos; }
    public void setNumPagos(String numPagos) { this.numPagos = numPagos; }
    public String getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(String tasaInteres) { this.tasaInteres = tasaInteres; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    public List<String> getRegalos() { return regalos; }
    public void setRegalos(List<String> regalos) { this.regalos = regalos; }
}
