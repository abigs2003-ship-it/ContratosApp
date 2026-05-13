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
    private SharedContractViewModel viewModel;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SharedContractViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnEditMode.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            binding.btnEditMode.setText(isEditMode ? "CANCELAR" : "EDITAR");
        });

        setupRecyclerView();
        setupObservers();
        
        viewModel.loadHistoryFromDatabase();
    }

    private void setupObservers() {
        viewModel.getHistory().observe(this, contracts -> {
            binding.progressBar.setVisibility(View.GONE);
            if (contracts != null) {
                adapter.setContracts(contracts);
                if (contracts.isEmpty()) {
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
        adapter = new ContratoAdapter(new ArrayList<>(), contract -> {
            Intent intent = new Intent(HistorialActivity.this, EditaContratoActivity.class);
            intent.putExtra("contract", contract);
            startActivity(intent);
        });

        adapter.setOnContractDeleteListener(contract -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Contrato")
                    .setMessage("¿Está seguro de que desea borrar el contrato?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        viewModel.deleteContrato(contract);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(adapter);
        
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadHistoryFromDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
