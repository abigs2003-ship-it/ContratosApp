package com.example.contrato.API;


import com.example.contrato.API.ApiResponse;
import com.example.contrato.API.FotoTitularRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TitularApiService {

    /**
     * Envia la foto del titular (en Base64) al backend.
     * El backend la guarda en la carpeta compartida y registra la ruta
     * en PMT_App_Ventas_Titulares.RutaFirma
     */
    @Headers("Content-Type: application/json")
    @POST("api/titulares/foto")
    Call<ApiResponse> guardarFotoTitular(@Body FotoTitularRequest request);

}
