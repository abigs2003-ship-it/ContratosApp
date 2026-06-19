package com.example.contrato.API;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("exito")
    private boolean exito;

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("rutaFirma")
    private String rutaFirma;

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRutaFirma() {
        return rutaFirma;
    }
}
