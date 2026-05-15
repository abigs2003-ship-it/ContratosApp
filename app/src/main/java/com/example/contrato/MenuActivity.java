package com.example.contrato;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contrato.Login.LoginActivity;
import com.example.contrato.databinding.ActivityHomepageBinding;
import com.example.contrato.repository.VentasContratoRepository;

public class MenuActivity extends AppCompatActivity {

    private ActivityHomepageBinding binding;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref1 = getSharedPreferences("sesion", MODE_PRIVATE);
        boolean loggeado = pref1.getBoolean("loggeado", false);
        long expira = pref1.getLong("sesion_expira", 0);

        if (!loggeado || System.currentTimeMillis() >= expira) {
            cerrarSesion();
            return;
        }

        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // obtiene info del usuario de sharedpreferences
        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        currentUserId = prefs.getLong("userId", -1);
        String userName = prefs.getString("userName", "");

        if (!userName.isEmpty()) {
            binding.bienvenida.setText("¡Bienvenido " + userName + "!");
        }

        binding.crearContrato.setOnClickListener(v -> {
            v.setEnabled(false);
            new Thread(() -> {
                try {
                    VentasContratoRepository repo = new VentasContratoRepository();
                    long nextId = repo.getNextId();
                    runOnUiThread(() -> {
                        v.setEnabled(true);
                        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                        intent.putExtra("ID_CONTRATO", nextId);
                        // Pass userId to MainActivity so it can be used for the DB inserts
                        intent.putExtra("ID_USUARIO", currentUserId);
                        startActivity(intent);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        v.setEnabled(true);
                        Toast.makeText(MenuActivity.this, "Error al generar ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        binding.historial.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, HistorialActivity.class);
            // Pasar el ID de usuario al historial
            intent.putExtra("ID_USUARIO", currentUserId);
            startActivity(intent);
        });

    }

    private void cerrarSesion() {
        SharedPreferences pref1 = getSharedPreferences("sesion", MODE_PRIVATE);
        pref1.edit().clear().apply();

        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
