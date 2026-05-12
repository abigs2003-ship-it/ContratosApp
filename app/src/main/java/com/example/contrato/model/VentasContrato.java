package com.example.contrato.model;

import java.sql.Timestamp;

public class VentasContrato {
    public long idContrato;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;
    public Timestamp fechaModificacion;
    public String estatus;
    public String idioma;

    public VentasContrato() {}
}
