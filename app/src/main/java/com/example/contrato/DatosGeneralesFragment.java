package com.example.contrato;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.contrato.databinding.FragmentDatosgeneralesBinding;
import com.google.android.material.tabs.TabLayout;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class DatosGeneralesFragment extends Fragment {

    private FragmentDatosgeneralesBinding binding;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDatosgeneralesBinding.inflate(inflater, container, false);

        NavHostFragment navHostFragment =
                (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_dg_container);

        navController = Objects.requireNonNull(navHostFragment).getNavController();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.topTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    navController.navigate(R.id.nav_titulares);
                } else if (tab.getPosition() == 1) {
                    navController.navigate(R.id.nav_direcciones);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_titulares) {
                Objects.requireNonNull(binding.topTabs.getTabAt(0)).select();
            } else if (destination.getId() == R.id.nav_direcciones) {
                Objects.requireNonNull(binding.topTabs.getTabAt(1)).select();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
