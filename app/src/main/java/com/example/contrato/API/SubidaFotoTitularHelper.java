package com.example.contrato.API;


import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.example.contrato.API.ApiResponse;
import com.example.contrato.API.FotoTitularRequest;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ejemplo de como tomar la foto del titular (Bitmap, por ejemplo desde
 * la camara o un ImageView) y enviarla al backend.
 */
public class SubidaFotoTitularHelper {

    private static final String TAG = "SubidaFotoTitular";

    public interface SubidaFotoCallback {
        void onExito(String rutaFirma);
        void onError(String mensaje);
    }

    public static void subirFoto(int idTitular, String nombreCompleto, Bitmap bitmap, SubidaFotoCallback callback) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();

        String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

        FotoTitularRequest request = new FotoTitularRequest(idTitular, nombreCompleto, base64, "jpg");

        TitularApiService api = RetrofitClient.getTitularApiService();
        Call<ApiResponse> call = api.guardarFotoTitular(request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse body = response.body();
                    if (body.isExito()) {
                        Log.d(TAG, "Foto guardada en: " + body.getRutaFirma());
                        callback.onExito(body.getRutaFirma());
                    } else {
                        Log.e(TAG, "Error de negocio: " + body.getMensaje());
                        callback.onError(body.getMensaje());
                    }
                } else {
                    String errorMsg = "Error HTTP " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Fallo de red", t);
                callback.onError("Fallo de conexion: " + t.getMessage());
            }
        });
    }
}
