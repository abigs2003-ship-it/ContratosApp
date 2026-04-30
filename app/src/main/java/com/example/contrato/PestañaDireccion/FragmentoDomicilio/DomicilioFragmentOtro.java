package com.example.contrato.PestañaDireccion.FragmentoDomicilio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.contrato.databinding.FragmentDomiciliomexicoBinding;
import com.example.contrato.databinding.FragmentDomiciliootroBinding;

public class DomicilioFragmentOtro extends Fragment {
    private FragmentDomiciliootroBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDomiciliootroBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}