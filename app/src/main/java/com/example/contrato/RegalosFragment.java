package com.example.contrato;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.contrato.databinding.FragmentRegalosBinding;
import com.example.contrato.databinding.ItemRegalosBinding;

import java.util.ArrayList;
import java.util.List;

public class RegalosFragment extends Fragment {

    private FragmentRegalosBinding binding;
    private SharedContractViewModel viewModel;
    private List<String> regalosList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegalosBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadExistingData();

        binding.btnAgregarRegalo.setOnClickListener(v -> {
            String nombreRegalo = binding.editNombreRegalo.getText().toString().trim();
            if (!nombreRegalo.isEmpty()) {
                regalosList.add(nombreRegalo);
                agregarRegaloAContenedor(nombreRegalo);
                saveDataToViewModel();
                binding.editNombreRegalo.setText("");
            } else {
                Toast.makeText(getContext(), "Por favor, escribe el nombre del regalo", Toast.LENGTH_SHORT).show();
            }
        });

        binding.AceptarTarea.setOnClickListener(v -> {
            saveDataToViewModel();
            irAFinanciamiento();
        });
    }

    private void irAFinanciamiento() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // Simulamos la pulsación en el menú inferior para mantener la sincronización y permitir regresar
            activity.binding.bottomNav.setSelectedItemId(R.id.nav_financiamiento);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDataToViewModel();
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            if (contract.getRegalos() != null) {
                regalosList = new ArrayList<>(contract.getRegalos());
                binding.ContenedorRegalos.removeAllViews();
                for (String r : regalosList) {
                    agregarRegaloAContenedor(r);
                }
            }
            binding.editComentarios.setText(contract.getComentarios());
        }
    }

    private void saveDataToViewModel() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();

        contract.setRegalos(new ArrayList<>(regalosList));
        contract.setComentarios(binding.editComentarios.getText().toString().trim());

        viewModel.setContract(contract);
    }

    private void agregarRegaloAContenedor(String nombre) {
        ItemRegalosBinding bindingItem = ItemRegalosBinding.inflate(getLayoutInflater(), binding.ContenedorRegalos, false);
        bindingItem.tvNombre.setText(nombre);
        bindingItem.btnBorrarRegalo.setOnClickListener(v -> {
            binding.ContenedorRegalos.removeView(bindingItem.getRoot());
            regalosList.remove(nombre);
            saveDataToViewModel();
            reordenarNumeracion();
        });

        binding.ContenedorRegalos.addView(bindingItem.getRoot());
        reordenarNumeracion();
    }

    private void reordenarNumeracion() {
        for (int i = 0; i < binding.ContenedorRegalos.getChildCount(); i++) {
            View view = binding.ContenedorRegalos.getChildAt(i);
            TextView tvNo = view.findViewById(R.id.noRegalo);
            if (tvNo != null) {
                tvNo.setText(String.valueOf(i + 1));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
