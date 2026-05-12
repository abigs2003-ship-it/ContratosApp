package com.example.contrato;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.CuentasSocialesAdapter.CuentaSocial;
import com.example.contrato.CuentasSocialesAdapter.Plataforma;

import java.util.ArrayList;
import java.util.List;

public class RedesSocialesFragment extends Fragment {

    private CuentasSocialesAdapter adapter;
    private final List<CuentaSocial> cuentas = new ArrayList<>();
    private SharedContractViewModel viewModel;
    private CheckBox cbNoRedes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContractViewModel.class);
        return inflater.inflate(R.layout.fragment_redes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CuentasSocialesAdapter(cuentas, position -> {
            saveDataToViewModel();
        });

        RecyclerView rv = view.findViewById(R.id.rvCuentasSociales);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);

        cbNoRedes = view.findViewById(R.id.cbNoRedes);
        
        loadExistingData();

        cbNoRedes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cuentas.clear();
                adapter.notifyDataSetChanged();
                saveDataToViewModel();
                //no se va a condiciones

            }
        });

        EditText etFbUsuario = view.findViewById(R.id.etFacebookUsuario);
        view.findViewById(R.id.btnAgregarFacebook).setOnClickListener(v -> {
            String usuario = etFbUsuario.getText().toString().trim();
            if (TextUtils.isEmpty(usuario)) {
                Toast.makeText(requireContext(), "Completa el usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            cbNoRedes.setChecked(false);
            adapter.agregarCuenta(new CuentaSocial(usuario, Plataforma.FACEBOOK));
            saveDataToViewModel();
            etFbUsuario.setText("");
        });

        EditText etIgUsuario = view.findViewById(R.id.etInstagramUsuario);
        view.findViewById(R.id.btnAgregarInstagram).setOnClickListener(v -> {
            String usuario = etIgUsuario.getText().toString().trim();
            if (TextUtils.isEmpty(usuario)) {
                Toast.makeText(requireContext(), "Completa el usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            cbNoRedes.setChecked(false);
            adapter.agregarCuenta(new CuentaSocial(usuario, Plataforma.INSTAGRAM));
            saveDataToViewModel();
            etIgUsuario.setText("");
        });

        EditText etTwUsuario = view.findViewById(R.id.etTwitterUsuario);
        view.findViewById(R.id.btnAgregarTwitter).setOnClickListener(v -> {
            String usuario = etTwUsuario.getText().toString().trim();
            if (TextUtils.isEmpty(usuario)) {
                Toast.makeText(requireContext(), "Completa el usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            cbNoRedes.setChecked(false);
            adapter.agregarCuenta(new CuentaSocial(usuario, Plataforma.TWITTER));
            saveDataToViewModel();
            etTwUsuario.setText("");
        });

        view.findViewById(R.id.AceptarTarea).setOnClickListener(v -> {
            saveDataToViewModel();
            irACondiciones();
        });
    }

    private void irACondiciones() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            // Simulamos la pulsación en el menú inferior para que el NavController maneje las pilas de retroceso correctamente
            activity.binding.bottomNav.setSelectedItemId(R.id.nav_condiciones);
        }
    }

    private void loadExistingData() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract != null) {
            cbNoRedes.setChecked(contract.isNoRedesSociales());
            
            if (contract.getRedesSociales() != null) {
                cuentas.clear();
                for (ContratoModelo.SocialAccount sa : contract.getRedesSociales()) {
                    Plataforma p = Plataforma.FACEBOOK;
                    if (sa.red.equalsIgnoreCase("Instagram")) p = Plataforma.INSTAGRAM;
                    if (sa.red.equalsIgnoreCase("Twitter") || sa.red.equalsIgnoreCase("X")) p = Plataforma.TWITTER;
                    cuentas.add(new CuentaSocial(sa.usuario, p));
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDataToViewModel();
    }

    private void saveDataToViewModel() {
        ContratoModelo contract = viewModel.getContractValue();
        if (contract == null) contract = new ContratoModelo();

        contract.setNoRedesSociales(cbNoRedes.isChecked());

        List<ContratoModelo.SocialAccount> socialList = new ArrayList<>();
        for (CuentaSocial cs : cuentas) {
            String platform = "Facebook";
            if (cs.getPlataforma() == Plataforma.INSTAGRAM) platform = "Instagram";
            if (cs.getPlataforma() == Plataforma.TWITTER) platform = "Twitter";
            socialList.add(new ContratoModelo.SocialAccount(platform, cs.getNombre()));
        }
        contract.setRedesSociales(socialList);
        viewModel.setContract(contract);
    }
}
