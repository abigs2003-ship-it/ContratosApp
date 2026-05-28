package com.example.contrato;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private int contractId = -1;
    private boolean modoEditar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es-MX"));
        }

        SharedContratoViewModel vm = new ViewModelProvider(this).get(SharedContratoViewModel.class);

        long idContrato = getIntent().getLongExtra("ID_CONTRATO", -1);
        long idUsuario  = getIntent().getLongExtra("ID_USUARIO", -1);

        // Guardamos el id del usuario en el viewmodel
        if (idUsuario > 0) {
            vm.setCurrentUserId(idUsuario);
        } else {
            SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
            idUsuario = prefs.getLong("userId", -1);
            vm.setCurrentUserId(idUsuario);
        }

        if (idContrato != -1) {
            vm.fetchContratoPorId(idContrato);
        }


        SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
        String userName = prefs.getString("userName", "");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.barraTitulo);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
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
            intent.putExtra("ID_USUARIO", getIntent().getLongExtra("ID_USUARIO", -1));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        if (!userName.isEmpty()) {
            binding.usuario.setText(userName + "!");
        }

        setupSpinnerIdiomas();
    }
    private void setupSpinnerIdiomas() {
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
                        convertirFechasEnViewModel(targetBase);
                        AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(targetFull)
                        );
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }
    private void convertirFechasEnViewModel(String toLang) {
        SharedContratoViewModel viewModel =
                new ViewModelProvider(this).get(SharedContratoViewModel.class);

        ContratoModelo contrato = viewModel.getContratoValue();
        if (contrato == null) return;

        // convierte fechas pagos diferidos fragmento datosventa
        List<ContratoModelo.PagoDiferido> pagos = contrato.getPagosDiferidos();
        if (pagos != null) {
            for (ContratoModelo.PagoDiferido pago : pagos) {
                pago.fecha = FechaLocaleConvertidor.convertir(pago.fecha, toLang);
            }
            contrato.setPagosDiferidos(pagos);
        }

        // convierte fechas fragmento titulares
        if (contrato.getTitulares() != null) {
            for (ContratoModelo.Persona p : contrato.getTitulares()) {
                p.cumple = FechaLocaleConvertidor.convertir(p.cumple, toLang);
            }
        }
        if (contrato.getBeneficiarios() != null) {
            for (ContratoModelo.Persona p : contrato.getBeneficiarios()) {
                p.cumple = FechaLocaleConvertidor.convertir(p.cumple, toLang);
            }
        }
        //convierte fechas fragmento financiamiento
        if (contrato.getFechaPrimerPago() != null) {
            contrato.setFechaPrimerPago(FechaLocaleConvertidor.convertir(contrato.getFechaPrimerPago(), toLang));

        }

        viewModel.setContrato(contrato);
    }
    public static class FechaLocaleConvertidor {

        private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
        private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};



        public static String convertir(String fecha, String toLang) {
            if (fecha == null || fecha.isEmpty()) return fecha;

            try {
                int dia, mes, anio;

                if (fecha.matches("\\d{2}/[a-zA-ZáéíóúÁÉÍÓÚ]+/\\d{4}")) {
                    dia  = Integer.parseInt(fecha.substring(0, 2));
                    String nombreMes = fecha.substring(3, fecha.lastIndexOf("/"));
                    mes  = parsearNombreMes(nombreMes);
                    anio = Integer.parseInt(fecha.substring(fecha.lastIndexOf("/") + 1));

                } else if (fecha.matches("[a-zA-Z]+/\\d{2}/\\d{4}")) {

                    String nombreMes = fecha.substring(0, fecha.indexOf("/"));
                    mes  = parsearNombreMes(nombreMes);
                    dia  = Integer.parseInt(fecha.substring(fecha.indexOf("/") + 1, fecha.lastIndexOf("/")));
                    anio = Integer.parseInt(fecha.substring(fecha.lastIndexOf("/") + 1));

                } else if (fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    int part1 = Integer.parseInt(fecha.substring(0, 2));
                    int part2 = Integer.parseInt(fecha.substring(3, 5));
                    anio = Integer.parseInt(fecha.substring(6, 10));

                    if (part1 > 12) {
                        dia = part1; mes = part2;
                    } else if (part2 > 12) {

                        mes = part1; dia = part2;
                    } else {

                        if (toLang.equals("en")) { dia = part1; mes = part2; }
                        else                     { mes = part1; dia = part2; }
                    }

                } else {
                    return fecha;
                }

                if (mes < 1 || mes > 12) return fecha;


                String nombreMesNuevo = toLang.equals("en") ? MESES_EN[mes - 1] : MESES_ES[mes - 1];

                if (toLang.equals("en")) {
                    return String.format("%s/%02d/%04d", nombreMesNuevo, dia, anio); // mar/15/2025
                } else {
                    return String.format("%02d/%s/%04d", dia, nombreMesNuevo, anio); // 15/mar/2025
                }

            } catch (Exception e) {
                return fecha; // regresa original si algo falla
            }
        }


        private static int parsearNombreMes(String nombre) {
            for (int i = 0; i < MESES_EN.length; i++) {
                if (MESES_EN[i].equalsIgnoreCase(nombre.trim()) ||
                        MESES_ES[i].equalsIgnoreCase(nombre.trim())) {
                    return i + 1;
                }
            }
            return -1;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
