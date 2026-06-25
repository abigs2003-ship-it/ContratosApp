package com.example.contrato.model;

import java.sql.Date;
import java.sql.Timestamp;

public class VentasDetalleFinanciamiento {
    public long   idDetalleFinanciamiento;
    public long   idContrato;
    public long   no;
    public Date   fechaPago;
    public double monto;
    public double capital;
    public double interes;
    public double capAcumulado;
    public double saldo;
    public long      idUsuarioAlta;
    public Timestamp fechaAlta;
    public long      idUsuarioModificacion;
    public Timestamp fechaModificacion;
    public String    estatus;
}