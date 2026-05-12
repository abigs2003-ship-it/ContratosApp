package com.example.contrato.model;

import java.sql.Timestamp;

public class VentasRegalos {
    public long idRegalo;
    public long idContrato;
    public String descripcion;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;

    public VentasRegalos() {}
}
