package com.example.contrato;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contrato.databinding.ActivityHistoryBinding;

import java.util.ArrayList;
import java.util.Locale;
public class HistorialActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private ContratoAdapter adapter;
    private SharedContratoViewModel viewModel;
    private boolean esModoEdicion = false;
    private boolean yaFueCargado = false;
    private boolean navegandoAEdicion = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long idUsuario = getIntent().getLongExtra("ID_USUARIO", -1);
        viewModel = new ViewModelProvider(this).get(SharedContratoViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());


        binding.btnEditMode.setOnClickListener(v -> setModoEdicion(!esModoEdicion));
        setupRecyclerView();
        setupObservers();

        binding.layoutCargando.setVisibility(View.VISIBLE);
        viewModel.cargaHistorialBaseDatos(idUsuario);

    }

    private void setModoEdicion(boolean activo) {
        esModoEdicion = activo;
        adapter.setEsModoEdicion(activo);
        binding.btnEditMode.setText(activo ? "CANCELAR" : "EDITAR");
    }
    @Override
    protected void onResume() {
        super.onResume();

        navegandoAEdicion = false;
        setModoEdicion(false);

        long idUsuario = getIntent().getLongExtra("ID_USUARIO", -1);
        if (!yaFueCargado) return;

        viewModel.cargaHistorialBaseDatos(idUsuario);
    }
    private void setupRecyclerView() {
        adapter = new ContratoAdapter(new ArrayList<>(), contrato ->  {
            Log.d("HISTORIAL", "Se ejecuta click en contrato: " + contrato.getId());
            viewModel.fetchContratoPorId(
                    Long.parseLong(contrato.getId())
            );
        });
        adapter.setOnContratoEstatusListener(this::mostrarConfirmacionEstatus);

        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(adapter);
        adapter.setOnContratoEditListener(contrato -> {
            // Solo permite editar contratos activos
            if (!"A".equalsIgnoreCase(contrato.getEstatus())) {
                Toast.makeText(this, "Solo se pueden editar contratos activos", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.layoutCargando.setVisibility(View.VISIBLE);
            viewModel.fetchContratoPorId(Long.parseLong(contrato.getId()));
        });
    }

    private void setupObservers() {
        viewModel.getHistory().observe(this, contratos -> {
            if (contratos == null) return;

            if (contratos.isEmpty() && !yaFueCargado) return;

            binding.layoutCargando.setVisibility(View.GONE);
            yaFueCargado = true;

            adapter.setContratos(contratos);
            binding.tvNoData.setVisibility(contratos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                binding.layoutCargando.setVisibility(View.GONE);
                yaFueCargado = true;
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getContrato().observe(this, contrato -> {
            if (contrato == null || contrato.getId() == null) {
                navegandoAEdicion = false;
                return;
            }
            if (!Boolean.TRUE.equals(contrato.getModoEdicion())) {
                navegandoAEdicion = false;
                return;
            }
            if (!contrato.isDatosListos()) return;
            if (navegandoAEdicion) return;

            navegandoAEdicion = true;
            Intent intent = new Intent(HistorialActivity.this, EditaContratoActivity.class);
            intent.putExtra("ID_CONTRATO", Long.parseLong(contrato.getId()));
            startActivity(intent);
        });
    }

    private void mostrarConfirmacionEstatus(ContratoModelo contrato) {
        boolean cancelado = "C".equalsIgnoreCase(contrato.getEstatus());
        String nuevoEstatus = cancelado ? "A" : "C";
        String mensaje = cancelado
                ? "¿Está seguro que desea reactivar el contrato?"
                : "¿Está seguro que desea cancelar el contrato?";

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Estatus")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    viewModel.actualizaEstatusContrato(
                            Long.parseLong(contrato.getId()),
                            nuevoEstatus
                    );
                    setModoEdicion(false);
                })
                .setNegativeButton("Regresar", null)
                .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
