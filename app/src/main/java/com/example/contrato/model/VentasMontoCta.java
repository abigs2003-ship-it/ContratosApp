package com.example.contrato.model;

import java.sql.Timestamp;

public class VentasMontoCta {
    public long idMontoCta;
    public long idContrato;
    public String xref;
    public Timestamp fechaAlta;
    public long idUsuarioAlta;
    public long idUsuarioModificacion;
    public String estatus;
    public Timestamp fechaModificacion;
    public VentasMontoCta() {}
}
