package com.example.contrato.API;

import com.google.gson.annotations.SerializedName;

public class FotoPasaporteRequest {

    @SerializedName("idTitular")
    private final int idTitular;

    @SerializedName("nombreCompleto")
    private final String nombreCompleto;

    @SerializedName("idContrato")
    private final String idContrato;

    @SerializedName("imagenBase64")
    private final String imagenBase64;

    @SerializedName("extension")
    private final String extension;

    public FotoPasaporteRequest(int idTitular, String nombreCompleto, String idContrato,
                                String imagenBase64, String extension) {
        this.idTitular      = idTitular;
        this.nombreCompleto = nombreCompleto;
        this.idContrato     = idContrato;
        this.imagenBase64   = imagenBase64;
        this.extension      = extension;
    }

    public int getIdTitular()         { return idTitular; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getIdContrato()     { return idContrato; }
    public String getImagenBase64()   { return imagenBase64; }
    public String getExtension()      { return extension; }
}