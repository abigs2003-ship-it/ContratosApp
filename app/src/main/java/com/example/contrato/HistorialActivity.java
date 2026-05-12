package com.example.contrato;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contrato.databinding.ActivityHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private ContratoAdapter adapter;
    private SharedContractViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SharedContractViewModel.class);

        binding.btnBack.setOnClickListener(v -> finish());

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
