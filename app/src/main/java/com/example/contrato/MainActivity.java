package com.example.contrato;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.contrato.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();

        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        Spinner spin = binding.traductorSpinner;
        String[] idiomas = getResources().getStringArray(R.array.idiomas);
        IdiomasAdapter adapter = new IdiomasAdapter(this, idiomas);
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String idiomaSeleccionado = parent.getItemAtPosition(position).toString();
                if (idiomaSeleccionado.equals("English")) {
                    setLocale("en");
                } else if (idiomaSeleccionado.equals("Español")) {
                    setLocale("es");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        setSupportActionBar(binding.barraSaludo);
    }

    private void setLocale(String idioma) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(idioma);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
