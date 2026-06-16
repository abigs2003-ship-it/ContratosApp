package com.example.contrato.model;

import java.sql.Date;
import java.sql.Timestamp;

public class VentasTitulares {
    public long idTitular;
    public long idContrato;
    public String nombre;
    public String paterno;
    public String materno;
    public String tipoTitular;
    public long idUsuarioAlta;
    public Timestamp fechaAlta;
    public Date fechaCumpleaños;
    public String ocupacion;
    public long parentesco;
    public long idUsuarioModificacion;
    public String estatus;
    public Timestamp fechaModificacion;
    public int tipoRegistro;     // 0 = Titular, 1 = Beneficiario
    public int ordenTitulares;   // posición en la lista (1, 2, 3...)
    public VentasTitulares() {}
}
