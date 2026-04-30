package com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.TiposFormato;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contrato.databinding.FragmentCmrFormatBinding;
import com.example.contrato.databinding.FragmentStandardFormatBinding;


public class CMRFormatFragment extends Fragment {

    private FragmentCmrFormatBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCmrFormatBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}