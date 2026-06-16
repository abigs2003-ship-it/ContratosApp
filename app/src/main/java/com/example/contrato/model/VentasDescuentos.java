package com.example.contrato.model;

import java.sql.Timestamp;

public class VentasDescuentos {
    public long idDescuento;
    public long idContrato;
    public Double montoDescuento;
    public String descripcion;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;
    public long idUsuarioModificacion;
    public String estatus;
    public Timestamp fechaModificacion;

    public VentasDescuentos() {}
}
