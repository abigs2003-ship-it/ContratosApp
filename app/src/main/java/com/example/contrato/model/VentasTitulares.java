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

    public VentasTitulares() {}
}
