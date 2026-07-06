package com.example.contrato;

import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContratoModelo implements Serializable {
    private boolean modoEdicion;
    private String id;
    private String clientName;
    private String fechaCreacion;
    private String fechaModificacion;

    private String estatus;


    // Titulares y Beneficiarios
    private List<Persona> titulares = new ArrayList<>();
    private List<Persona> beneficiarios = new ArrayList<>();

    // Dirección

    private String paisOtro;
    private String tipoDir;
    private String pais;
    private String mexCalle;
    private String USACalle;
    private String CanCalle;

    private String MexnumExt;
    private String MexnumInt;
    private String MexCiudad;
    private String USACity;
    private String CanCity;
    private String MexEstado;
    private String USAState;

    private String idioma;
    private String MexCP;
    private String USACP;
    private String CanCP;
    private String Mexcolonia;

    private String delegacion;
    private String nacionalidad;
    private String poBox;
    private String Box;
    private String cmr;
    private String apo;
    private String province;
    public long idUsuarioModificacion;
    private String financiamientoElegido;
    private String Linea1, Linea2, Linea3, Linea4, Linea5;

    // Contacto
    private List<InfoTelefono> telefonos = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private boolean noCorreo;

    // Redes Sociales
    private List<CuentaRed> redesSociales = new ArrayList<>();
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

    private String noContratosMC;
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
    private String tipoPagoDiferido;
    private String primerPagoDiferido;


    private List<PagoDiferido> pagosDiferidos = new ArrayList<>();
    private List<DescuentoDetalle> descuentosDetalle = new ArrayList<>();
    private List<String> contratosMontoCuenta = new ArrayList<>();
    private String tipoPagoEnganche;
    private String ultimaFechaEnganche;
    // Financiamiento
    private String tipoPeriodo;
    private String fechaPrimerPago;
    private String numPagos;
    private String tasaInteres;

    //tipo ocupacion
    private String tipoOcupación;
    private boolean datosListos = false;


    private List<String> regalos = new ArrayList<>();

    public ContratoModelo() {
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public static class Persona implements Serializable {
        public String nombre, paterno, materno, ocupacion, parentesco, cumple, archivoFirma;
        public String archivoINEFrente, archivoINEReverso, archivoPasaporte;        //imagen de la firma del titular en base 64
        public String imagenFirmaBase64;
        public String imagenINEFrente;
        public String imagenINEReverso;
        public String imagenPasaporte;

        public String id;
        public long idTitularBD = -1; // se llena después de insertar en BD
        public Persona() {}
        public Persona(String n, String p, String m, String o, String par, String c, String archivo) {
            this.id = UUID.randomUUID().toString();
            this.nombre = n; this.paterno = p; this.materno = m;
            this.ocupacion = o; this.parentesco = par; this.cumple = c; this.archivoFirma = archivo;
        }
    }
    // ── Detalle de financiamiento  ──────────────────────────────────────
    public static class FilaAmortizacion {
        public int    no;
        public String fecha;
        public double monto;
        public double capital;
        public double interes;
        public double capAcumulado;
        public double saldo;
    }

    private List<FilaAmortizacion> filasAmortizacion = new ArrayList<>();

    public List<FilaAmortizacion> getFilasAmortizacion() { return filasAmortizacion; }
    public void setFilasAmortizacion(List<FilaAmortizacion> v) { filasAmortizacion = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Persona)) return false;
        Persona other = (Persona) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    public static class InfoTelefono implements Serializable {
        public String etiqueta, lada, numero;
        public boolean isWhatsApp, esPrincipal;
        public InfoTelefono() {}
        public InfoTelefono(String etiqueta, String lada, String numero, boolean isWhatsApp, boolean isPrincipal) {
            this.etiqueta = etiqueta;
            this.lada = lada;
            this.numero = numero;
            this.isWhatsApp = isWhatsApp;
            this.esPrincipal = isPrincipal;
        }
    }

    public static class CuentaRed implements Serializable {
        public String red, usuario;
        public CuentaRed() {}
        public CuentaRed(String r, String u) { this.red = r; this.usuario = u; }
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
    public String getFinanciamientoElegido() { return financiamientoElegido; }
    public void setFinanciamientoElegido(String financiamientoElegido) { this.financiamientoElegido = financiamientoElegido; }
    public void setPrimerPagoDiferido(String primerPagoDiferido) {this.primerPagoDiferido = primerPagoDiferido;}
    public String getPrimerPagoDiferido(){return primerPagoDiferido;}

    public boolean isDatosListos() { return datosListos; }
    public void setDatosListos(boolean datosListos) { this.datosListos = datosListos; }

    public boolean getModoEdicion(){return modoEdicion;}
    public void setModoEdicion(boolean modoEdicion){this.modoEdicion = modoEdicion;}
    public String getEstatus(){return estatus;}
    public void setEstatus(String estatus){this.estatus = estatus;}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(String fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public List<Persona> getTitulares() { return titulares; }
    public void setTitulares(List<Persona> titulares) { this.titulares = titulares; }
    public List<Persona> getBeneficiarios() { return beneficiarios; }
    public void setBeneficiarios(List<Persona> beneficiarios) { this.beneficiarios = beneficiarios; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }


    public void setPaisOtro(String paisOtro) { this.paisOtro = paisOtro; }
    public String getPaisOtro() { return paisOtro; }
    public void setTipoDir(String tipoDir) { this.tipoDir = tipoDir; }
    public String getTipoDir() { return tipoDir; }

    public void setMexNumExt(String MexnumExt) { this.MexnumExt = MexnumExt; }
    public String getMexNumExt() { return MexnumExt; }
    public void setMexNumInt(String MexnumInt) { this.MexnumInt = MexnumInt; }
    public String getMexNumInt() { return MexnumInt; }
    public String getMexCalle() { return mexCalle; }
    public void setMexCalle(String mexCalle) { this.mexCalle = mexCalle; }
    public String getUsaCalle() { return USACalle; }
    public void setUsaCalle(String USACalle) { this.USACalle = USACalle; }
    public String getCanCalle() { return CanCalle; }
    public void setCanCalle(String CanCalle) { this.CanCalle = CanCalle; }
    //ciudades
    public String getUsaCity() { return USACity; }
    public void setUsaCity(String USACity) { this.USACity = USACity; }
    public String getMexCiudad() { return MexCiudad; }
    public void setMexCiudad(String MexCiudad) { this.MexCiudad = MexCiudad; }
    public String getCanCity() { return CanCity; }
    public void setCanCity(String CanCity) { this.CanCity = CanCity; }
    //estados
    public String getMexEstado() { return MexEstado; }
    public void setMexEstado(String MexEstado) { this.MexEstado = MexEstado; }
    public String getUsaState() { return USAState; }
    public void setUsaState(String USAState) { this.USAState = USAState; }
    //CPs
    public String getMexCP() { return MexCP; }
    public void setMexCP(String MexCP) { this.MexCP= MexCP; }

    public String getUsaZip() { return USACP; }
    public void setUsaZip(String USACP) { this.USACP = USACP; }

    public String getCanPostalCode() { return CanCP; }
    public void setCanPostalCode(String CanCP) { this.CanCP = CanCP; }
    //colonias
    public String getMexColonia() { return Mexcolonia; }
    public void setMexColonia(String Mexcolonia) { this.Mexcolonia = Mexcolonia; }
    public String getDelegacion() { return delegacion; }
    public void setDelegacion(String delegacion) { this.delegacion = delegacion; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }


    public String getPoBox() { return poBox; }
    public void setPoBox(String poBox) { this.poBox = poBox; }
    public String getBox() { return Box; }
    public void setBox(String Box) { this.Box = Box; }
    public String getCmr() { return cmr; }
    public void setCmr(String cmr) { this.cmr = cmr; }
    public String getApo() { return apo; }
    public void setApo(String usaApo) { this.apo = usaApo; }


    public String getCanProvince() { return province; }
    public void setCanProvince(String canProvince) { this.province = canProvince; }

    public String getLinea1() { return Linea1; }
    public void setLinea1(String otroLinea1) { this.Linea1 = otroLinea1; }
    public String getLinea2() { return Linea2; }
    public void setLinea2(String otroLinea2) { this.Linea2 = otroLinea2; }
    public String getLinea3() { return Linea3; }
    public void setLinea3(String otroLinea3) { this.Linea3 = otroLinea3; }
    public String getLinea4() { return Linea4; }
    public void setLinea4(String otroLinea4) { this.Linea4 = otroLinea4; }
    public String getLinea5() { return Linea5; }
    public void setLinea5(String otroLinea5) { this.Linea5 = otroLinea5; }

    public List<InfoTelefono> getTelefonos() { return telefonos; }
    public void setTelefonos(List<InfoTelefono> telefonos) { this.telefonos = telefonos; }
    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public boolean isNoCorreo() { return noCorreo; }
    public void setNoCorreo(boolean noCorreo) { this.noCorreo = noCorreo; }
    public List<CuentaRed> getRedesSociales() { return redesSociales; }
    public void setRedesSociales(List<CuentaRed> redesSociales) { this.redesSociales = redesSociales; }
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
    public String getTipoOcupacion() { return tipoOcupación; }
    public void setTipoOcupacion(String tipoOcupación) { this.tipoOcupación = tipoOcupación; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(String tipoCambio) { this.tipoCambio = tipoCambio; }
    public String getPrecioBruto() { return precioBruto; }
    public void setPrecioBruto(String precioBruto) { this.precioBruto = precioBruto; }
    public String getMontoCuenta() { return montoCuenta; }
    public void setMontoCuenta(String montoCuenta) { this.montoCuenta = montoCuenta; }

    public String getNoContratosMC() { return noContratosMC; }

    public void setNoContratosMC(String noContratosMC) { this.noContratosMC = noContratosMC; }
    public String getPrecioNeto() { return precioNeto; }
    public void setPrecioNeto(String precioNeto) { this.precioNeto = precioNeto; }
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }
    public String getEngancheTotal() { return engancheTotal; }
    public void setEngancheTotal(String engancheTotal) { this.engancheTotal = engancheTotal; }
    public String getEnganchePorcentaje() { return enganchePorcentaje; }
    public void setEnganchePorcentaje(String enganchePorcentaje) { this.enganchePorcentaje = enganchePorcentaje; }

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

    public String getUltimaFechaEnganche() {
        return ultimaFechaEnganche;
    }

    public void setUltimaFechaEnganche(String ultimaFechaEnganche) {
        this.ultimaFechaEnganche = ultimaFechaEnganche;
    }

    public String getTipoPagoEnganche() {return tipoPagoEnganche;}

    public void setTipoPagoEnganche(String tipoPagoEnganche) {this.tipoPagoEnganche = tipoPagoEnganche;}

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
