package com.example.contrato;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.contrato.databinding.FragmentCondicionesBinding;
import com.google.android.material.tabs.TabLayout;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class CondicionesFragment extends Fragment {

    private FragmentCondicionesBinding binding;
    private NavController navController;
    private SharedContratoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCondicionesBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        NavHostFragment navHostFragment =
                (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_condiciones_container);

        navController = Objects.requireNonNull(navHostFragment).getNavController();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restaurar la última pestaña seleccionada
        int lastTab = viewModel.getLastCondTab();
        if (lastTab > 0) {
            binding.condicionesTopTab.post(() -> {
                TabLayout.Tab tab = binding.condicionesTopTab.getTabAt(lastTab);
                if (tab != null) {
                    tab.select();
                    // Navegar al destino correspondiente
                    int destinationId = -1;
                    if (lastTab == 1) destinationId = R.id.nav_regalos;
                    
                    if (destinationId != -1) {
                        navController.navigate(destinationId);
                    }
                }
            });
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.nav_datos_venta, false)
                .build();

        binding.condicionesTopTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewModel.setLastCondTab(position); // Guardar estado
                
                int destinationId = -1;
                if (position == 0) {
                    destinationId = R.id.nav_datos_venta;
                } else if (position == 1) {
                    destinationId = R.id.nav_regalos;
                }

                if (destinationId != -1 && navController.getCurrentDestination() != null 
                        && navController.getCurrentDestination().getId() != destinationId) {
                    navController.navigate(destinationId, null, navOptions);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int tabIndex = -1;
            if (destination.getId() == R.id.nav_datos_venta) tabIndex = 0;
            else if (destination.getId() == R.id.nav_regalos) tabIndex = 1;

            if (tabIndex != -1) {
                viewModel.setLastCondTab(tabIndex);
                if (binding.condicionesTopTab.getSelectedTabPosition() != tabIndex) {
                    Objects.requireNonNull(binding.condicionesTopTab.getTabAt(tabIndex)).select();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
