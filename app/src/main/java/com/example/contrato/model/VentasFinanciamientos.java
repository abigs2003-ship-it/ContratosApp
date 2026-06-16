package com.example.contrato.model;

import java.sql.Date;
import java.sql.Timestamp;

public class VentasFinanciamientos {
    public long idFinanciamiento;
    public long idContrato;
    public String tipoPeriodo;
    public Date fechaPrimerPago;
    public Double montoAFinanciar;
    public Integer numeroPagos;
    public Double tasaInteres;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;
    public long idUsuarioModificacion;
    public String estatus;
    public Timestamp fechaModificacion;
    public VentasFinanciamientos() {}
}
