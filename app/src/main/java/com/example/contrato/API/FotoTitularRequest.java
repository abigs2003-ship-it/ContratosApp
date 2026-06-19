package com.example.contrato.API;

import com.google.gson.annotations.SerializedName;

public class FotoTitularRequest {

    @SerializedName("idTitular")
    private final int idTitular;

    @SerializedName("nombreCompleto")
    private final String nombreCompleto;

    @SerializedName("imagenBase64")
    private final String imagenBase64;

    @SerializedName("extension")
    private final String extension;

    public FotoTitularRequest(int idTitular, String nombreCompleto, String imagenBase64, String extension) {
        this.idTitular = idTitular;
        this.nombreCompleto = nombreCompleto;
        this.imagenBase64 = imagenBase64;
        this.extension = extension;
    }

    public int getIdTitular() {
        return idTitular;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public String getExtension() {
        return extension;
    }
}