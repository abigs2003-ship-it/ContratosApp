package com.example.contrato.model;

import java.sql.Timestamp;

public class VentasInventario {
    public long idCondicionesVenta;
    public long idContrato;
    public String unidad;
    public String temporada;
    public String tipoVenta;
    public int aniosComprados;
    public long primerAnioUso;
    public String monedaVenta;
    public Double tipoCambioVenta;

    public String tipoOcupacion;
    public Double precioBruto;
    public Double montoCta;
    public long noContratosMontoCta;
    public Double precioNeto;
    public String tipoPago;
    public Double engancheTotal;
    public Double engancheTotalPorcentaje;
    public Double enganchePagarSala;
    public Double enganchePagarSalaPorcentaje;
    public Double descuentos;
    public long noDescuentos;
    public Double engancheDiferido;
    public long noPagosEngancheDiferido;
    public Double saldoEnganche;
    public Double montoFinanciar;
    public Double costoContrato;
    public Double totalPagoSala;
    public Double costoMembresia;
    public String comentariosRegalos;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;
    public long idUsuarioModificacion;
    public String estatus;
    public Timestamp fechaModificacion;
    public String tipoPagoDiferido;
    public String idUnidad;
    public Long idTemporada;
    public String idTipoOcupacion;


    public VentasInventario() {}
}
