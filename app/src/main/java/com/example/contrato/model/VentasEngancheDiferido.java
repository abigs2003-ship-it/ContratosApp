package com.example.contrato.model;

import java.sql.Date;
import java.sql.Timestamp;

public class VentasEngancheDiferido {
    public long idPago;
    public long idContrato;
    public Double cantidadPago;
    public Date fechaPago;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;

    public VentasEngancheDiferido() {}
}
