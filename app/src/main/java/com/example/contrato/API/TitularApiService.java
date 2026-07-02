package com.example.contrato.API;


import com.example.contrato.API.ApiResponse;
import com.example.contrato.API.FotoTitularRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TitularApiService {

    @Headers("Content-Type: application/json")
    @POST("api/titulares/foto")
    Call<ApiResponse> guardarFotoTitular(@Body FotoTitularRequest request);

    @Headers("Content-Type: application/json")
    @POST("api/titulares/ine/frente")
    Call<ApiResponse> guardarINEFrente(@Body FotoINERequest request);

    @Headers("Content-Type: application/json")
    @POST("api/titulares/ine/reverso")
    Call<ApiResponse> guardarINEReverso(@Body FotoINERequest request);

    @Headers("Content-Type: application/json")
    @POST("api/titulares/pasaporte")
    Call<ApiResponse> guardarPasaporteTitular(@Body FotoPasaporteRequest request);
}
