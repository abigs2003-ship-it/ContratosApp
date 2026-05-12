package com.example.contrato.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contrato.MenuActivity;
import com.example.contrato.databinding.ActivityLoginBinding;
import com.example.contrato.repository.LoginRepository;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginRepository loginRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginRepository = new LoginRepository();

        binding.btnEntrar.setOnClickListener(v -> {
            String usuario = binding.editUsuario.getText().toString().trim();
            String contrasena = binding.editContrasena.getText().toString().trim();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            realizarLogin(usuario, contrasena);
        });
    }

    private void realizarLogin(String usuario, String contrasena) {
        binding.btnEntrar.setEnabled(false);
        // Show progress if you have one, e.g., binding.progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                LoginRepository.LoginResult result = loginRepository.login(usuario, contrasena);
                
                runOnUiThread(() -> {
                    binding.btnEntrar.setEnabled(true);
                    if (result != null) {
                        // Save user info
                        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
                        prefs.edit()
                                .putLong("userId", result.empleadoId)
                                .putString("userName", result.nombreCompleto)
                                .apply();

                        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    binding.btnEntrar.setEnabled(true);
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
