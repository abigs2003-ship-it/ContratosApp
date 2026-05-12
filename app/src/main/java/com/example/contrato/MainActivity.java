package com.example.contrato;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.contrato.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-MX"));
        }

        // Receive the generated ID and User ID from MenuActivity
        long idContrato = getIntent().getLongExtra("ID_CONTRATO", -1);
        long idUsuario = getIntent().getLongExtra("ID_USUARIO", -1);
        
        SharedContractViewModel viewModel = new ViewModelProvider(this).get(SharedContractViewModel.class);
        
        // Ensure ViewModel has the current user ID for database operations
        if (idUsuario != -1) {
            viewModel.setCurrentUserId(idUsuario);
        } else {
            // Fallback to SharedPreferences if not passed in intent
            SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
            idUsuario = prefs.getLong("userId", -1);
            viewModel.setCurrentUserId(idUsuario);
        }

        if (idContrato != -1) {
            ContratoModelo model = viewModel.getContractValue();
            if (model == null) model = new ContratoModelo();
            model.setId(String.valueOf(idContrato));
            viewModel.setContract(model);
        }

        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        String userName = prefs.getString("userName", "");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.barraTitulo);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_datos_generales, 
                R.id.nav_condiciones, 
                R.id.nav_financiamiento
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.saludobarra);
            }
        });

        binding.exitbtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Display the name after Bienvenida (or in the top bar)
        if (!userName.isEmpty()) {
            binding.usuario.setText(userName + "!");
        }

        setupLanguageSpinner();
    }

    private void setupLanguageSpinner() {
        Spinner spin = binding.traductorSpinner;
        String[] idiomas = getResources().getStringArray(R.array.idiomas);
        IdiomasAdapter adapter = new IdiomasAdapter(this, idiomas);
        spin.setAdapter(adapter);

        String activeLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        
        if (activeLangTag.toLowerCase().startsWith("en")) {
            spin.setSelection(1, false);
        } else {
            spin.setSelection(0, false);
        }

        spin.post(() -> {
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String current = AppCompatDelegate.getApplicationLocales().toLanguageTags();
                    String targetBase = (position == 1) ? "en" : "es";
                    String targetFull = (position == 1) ? "en-US" : "es-MX";

                    if (!current.toLowerCase().startsWith(targetBase)) {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetFull));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
