package com.example.contrato;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contrato.databinding.ActivityHistoryBinding;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private ContratoAdapter adapter;
    private SharedContratoViewModel viewModel;
    private boolean isEditMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        long idUsuario = getIntent().getLongExtra("ID_USUARIO", -1); //recupera el id del usuario que inicio sesión desde el MainActivity



        viewModel = new ViewModelProvider(this).get(SharedContratoViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnEditMode.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            binding.btnEditMode.setText(isEditMode ? "CANCELAR" : "EDITAR");
        });

        setupRecyclerView();
        setupObservers();
        
        viewModel.cargaHistorialBaseDatos(idUsuario); //por ahora se cargan todos los historiales
    }

    private void setupObservers() {
        viewModel.getHistory().observe(this, Contratos -> {
            binding.progressBar.setVisibility(View.GONE);
            if (Contratos != null) {
                adapter.setContratos(Contratos);
                if (Contratos.isEmpty()) {
                    binding.tvNoData.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoData.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ContratoAdapter(new ArrayList<>(), contrato -> {
            Intent intent = new Intent(HistorialActivity.this, EditaContratoActivity.class);
            intent.putExtra("contrato", contrato);
            startActivity(intent);
        });


        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(adapter);
        
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        long idUsuario = getIntent().getLongExtra("ID_USUARIO", -1); //recupera el id del usuario que inicio sesión desde el MainActivity

        viewModel.cargaHistorialBaseDatos(idUsuario);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
