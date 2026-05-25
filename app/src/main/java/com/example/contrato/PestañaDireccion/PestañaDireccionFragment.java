package com.example.contrato.PestañaDireccion;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.contrato.ContratoModelo;
import com.example.contrato.R;
import com.example.contrato.SharedContratoViewModel;
import com.example.contrato.databinding.FragmentDireccionBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PestañaDireccionFragment extends Fragment {

    private FragmentDireccionBinding binding;
    private List<RadioButton> phoneRadioButtons;
    private SharedContratoViewModel viewModel;
    private String PaisSeleccionado;

    public interface ValidatableFragment {
        boolean isValid();
    }

    public interface ClearableFragment {
        void clearFields();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDireccionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedContratoViewModel.class);

        setupBotones();
        setupRadioButtons();
        setupWhatsAppCheckboxes();
        setupNacionalidadSpinner();
        telefonosMask();

        cargaDatosExistentes();

        return binding.getRoot();
    }

    //añade parentesis a la lada
    private void telefonosMask() {
        EditText[] ladaFields = {
                binding.etLadaCasa1, binding.etLadaCasa2,
                binding.etLadaOficina1, binding.etLadaOficina2,
                binding.etLadaCel1, binding.etLadaCel2,
                binding.etLadaMensajes
        };

        EditText[] numberFields = {
                binding.etNumeroCasa1, binding.etNumeroCasa2,
                binding.etNumeroOficina1, binding.etNumeroOficina2,
                binding.etNumeroCel1, binding.etNumeroCel2,
                binding.etNumeroMensajes
        };
        for (EditText campo : ladaFields) {


            campo.setOnFocusChangeListener((vista, tieneFoco) -> {
                // Cuando el usuario sale del campo
                if (!tieneFoco) {
                    String numeroLimpio = campo.getText().toString().replaceAll("[^\\d]", "");

                    if (numeroLimpio.length() > 3) {
                        numeroLimpio = numeroLimpio.substring(0, 3);
                    }

                    if (!numeroLimpio.isEmpty()) {
                        String numeroFormateado = "(" + numeroLimpio + ")";
                        campo.setText(numeroFormateado);
                    }
                }
            });

            campo.setOnEditorActionListener((vista, accionId, evento) -> {
                String numeroLimpio = campo.getText().toString().replaceAll("[^\\d]", "");

                if (numeroLimpio.length() > 3) {
                    numeroLimpio = numeroLimpio.substring(0, 3);
                }

                if (!numeroLimpio.isEmpty()) {
                    campo.setText("(" + numeroLimpio + ")");
                }

                return false;
            });
        }

        for (EditText et : numberFields) {
            et.addTextChangedListener(new TextWatcher() {
                private boolean isUpdating = false;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (isUpdating) return;
                    isUpdating = true;
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    if (clean.length() > 7) clean = clean.substring(0, 7);
                    
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < clean.length(); i++) {
                        formatted.append(clean.charAt(i));
                        if ((i == 2 || i == 4) && i != clean.length() - 1) {
                            formatted.append("-");
                        }
                    }
                    
                    et.setText(formatted.toString());
                    et.setSelection(formatted.length());
                    isUpdating = false;
                }
            });
        }
    }

    private void cargaDatosExistentes() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) return;

        if (Contrato.getPais() != null) {
            PaisSeleccionado = Contrato.getPais();
            PaisSeleccionado = Contrato.getPais();
            aplicarSeleccionPais(PaisSeleccionado);
        } else {
            LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
            String lang = currentLocales.isEmpty() ? "es" : currentLocales.get(0).getLanguage();
            if (lang.equals("en")) {
                PaisSeleccionado = "USA";
                aplicarSeleccionPais("USA");
            } else {
                PaisSeleccionado = "México";
                aplicarSeleccionPais("México");
            }
        }

        if (Contrato.getNacionalidad() != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerNacionalidad.getAdapter();
            if (adapter != null) {
                int pos = adapter.getPosition(Contrato.getNacionalidad());
                if (pos >= 0) binding.spinnerNacionalidad.setSelection(pos);
            }
        }

        List<String> emails = Contrato.getEmails();
        if (emails.size() > 0) binding.etEmail1.setText(emails.get(0));
        if (emails.size() > 1) binding.etEmail2.setText(emails.get(1));
        if (emails.size() > 2) binding.etEmail3.setText(emails.get(2));
        if (emails.size() > 3) binding.etEmail4.setText(emails.get(3));
        binding.cbNoCorreo.setChecked(Contrato.isNoCorreo());
        for (ContratoModelo.InfoTelefono p : Contrato.getTelefonos()) {
            if (p.etiqueta.equals("Casa 1")) {
                binding.etLadaCasa1.setText(p.lada);
                binding.etNumeroCasa1.setText(p.numero);
                binding.cbWsCasa1.setChecked(p.isWhatsApp);
                binding.rbCasa1.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Casa 2")) {
                binding.etLadaCasa2.setText(p.lada);
                binding.etNumeroCasa2.setText(p.numero);
                binding.cbWsCasa2.setChecked(p.isWhatsApp);
                binding.rbCasa2.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Oficina 1")) {
                binding.etLadaOficina1.setText(p.lada);
                binding.etNumeroOficina1.setText(p.numero);
                binding.cbWsOficina1.setChecked(p.isWhatsApp);
                binding.rbOficina1.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Oficina 2")) {
                binding.etLadaOficina2.setText(p.lada);
                binding.etNumeroOficina2.setText(p.numero);
                binding.cbWsOficina2.setChecked(p.isWhatsApp);
                binding.rbOficina2.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Celular 1")) {
                binding.etLadaCel1.setText(p.lada);
                binding.etNumeroCel1.setText(p.numero);
                binding.cbWsCel1.setChecked(p.isWhatsApp);
                binding.rbCelular1.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Celular 2")) {
                binding.etLadaCel2.setText(p.lada);
                binding.etNumeroCel2.setText(p.numero);
                binding.cbWsCel2.setChecked(p.isWhatsApp);
                binding.rbCelular2.setChecked(p.esPrincipal);
            } else if (p.etiqueta.equals("Mensajes")) {
                binding.etLadaMensajes.setText(p.lada);
                binding.etNumeroMensajes.setText(p.numero);
                binding.cbWsMensajes.setChecked(p.isWhatsApp);
                binding.rbMensajes.setChecked(p.esPrincipal);
            }
        }
        if(binding.cbNoCorreo.isChecked()){
            binding.etEmail1.setText("");
            binding.etEmail2.setText("");
            binding.etEmail3.setText("");
            binding.etEmail4.setText("");
        }
    }

    private void aplicarSeleccionPais(String pais) {
        switch (pais) {
            case "México":
                seleccionaBoton(binding.btnMexico);
                cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentMexico());
                break;
            case "USA":
                seleccionaBoton(binding.btnUSA);
                cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.DomicilioFragmentUSA());
                break;
            case "Canadá":
                seleccionaBoton(binding.btnCanada);
                cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentCanada());
                break;
            case "Otro":
                seleccionaBoton(binding.btnOtro);
                cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentOtro());
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        guardaDatosViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.AceptarTarea.setOnClickListener(v -> {
            if (validarCampos()) {
                guardaDatosViewModel();
                Navigation.findNavController(v).navigate(R.id.nav_redes_sociales);
            }
        });

        binding.btnLimpiarDomicilio.setOnClickListener(v -> limpiarDomicilio());
        binding.btnLimpiarTelefonos.setOnClickListener(v -> limpiarTelefonos());
        binding.btnLimpiarEmails.setOnClickListener(v -> limpiarEmails());
    }

    private void limpiarDomicilio() {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.addressContainer);
        if (currentFragment instanceof ClearableFragment) {
            ((ClearableFragment) currentFragment).clearFields();
        }
    }

    private void limpiarTelefonos() {
        EditText[] fields = {
                binding.etLadaCasa1, binding.etNumeroCasa1,
                binding.etLadaCasa2, binding.etNumeroCasa2,
                binding.etLadaOficina1, binding.etNumeroOficina1,
                binding.etLadaOficina2, binding.etNumeroOficina2,
                binding.etLadaCel1, binding.etNumeroCel1,
                binding.etLadaCel2, binding.etNumeroCel2,
                binding.etLadaMensajes, binding.etNumeroMensajes
        };
        for (EditText et : fields) et.setText("");

        CheckBox[] ws = {
                binding.cbWsCasa1, binding.cbWsCasa2, binding.cbWsOficina1,
                binding.cbWsOficina2, binding.cbWsCel1, binding.cbWsCel2, binding.cbWsMensajes
        };
        for (CheckBox cb : ws) cb.setChecked(false);

        for (RadioButton rb : phoneRadioButtons) rb.setChecked(false);
    }

    private void limpiarEmails() {
        binding.etEmail1.setText("");
        binding.etEmail2.setText("");
        binding.etEmail3.setText("");
        binding.etEmail4.setText("");
        binding.cbNoCorreo.setChecked(false);
    }

    private boolean validarCampos() {
        if (binding.spinnerNacionalidad.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Seleccione una nacionalidad", Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean tieneTelefono = !binding.etNumeroCasa1.getText().toString().isEmpty() ||
                !binding.etNumeroCasa2.getText().toString().isEmpty() ||
                !binding.etNumeroOficina1.getText().toString().isEmpty() ||
                !binding.etNumeroOficina2.getText().toString().isEmpty() ||
                !binding.etNumeroCel1.getText().toString().isEmpty() ||
                !binding.etNumeroCel2.getText().toString().isEmpty() ||
                !binding.etNumeroMensajes.getText().toString().isEmpty();

        if (!tieneTelefono) {
            Toast.makeText(requireContext(), "Debe añadir al menos un teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean tieneEmail = !binding.etEmail1.getText().toString().isEmpty() ||
                !binding.etEmail2.getText().toString().isEmpty() ||
                !binding.etEmail3.getText().toString().isEmpty() ||
                !binding.etEmail4.getText().toString().isEmpty();

        if (!tieneEmail && !binding.cbNoCorreo.isChecked()) {
            Toast.makeText(requireContext(), "Debe añadir un correo o marcar que no tiene", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!PaisSeleccionado.equals("Otro")) {
            Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.addressContainer);
            if (currentFragment instanceof ValidatableFragment) {
                if (!((ValidatableFragment) currentFragment).isValid()) {
                    Toast.makeText(requireContext(), "Complete todos los campos del domicilio", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private void setupWhatsAppCheckboxes() {
        CheckBox[] wsCheckboxes = {
                binding.cbWsCasa1, binding.cbWsCasa2,
                binding.cbWsOficina1, binding.cbWsOficina2,
                binding.cbWsCel1, binding.cbWsCel2,
                binding.cbWsMensajes
        };

        for (CheckBox cb : wsCheckboxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                buttonView.setText(isChecked ? "Si" : "No");
            });
            cb.setText(cb.isChecked() ?  "Si" : "No");
        }
    }

    private void setupBotones() {
        binding.btnMexico.setOnClickListener(v -> {
            PaisSeleccionado = "México";
            seleccionaBoton(binding.btnMexico);
            cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentMexico());
        });

        binding.btnUSA.setOnClickListener(v -> {
            PaisSeleccionado = "USA";
            seleccionaBoton(binding.btnUSA);
            cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.USAFragment.DomicilioFragmentUSA());
        });

        binding.btnCanada.setOnClickListener(v -> {
            PaisSeleccionado = "Canadá";
            seleccionaBoton(binding.btnCanada);
            cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentCanada());
        });

        binding.btnOtro.setOnClickListener(v -> {
            PaisSeleccionado = "Otro";
            seleccionaBoton(binding.btnOtro);
            cargaFragmentoDireccion(new com.example.contrato.PestañaDireccion.FragmentoDomicilio.DomicilioFragmentOtro());
        });
    }

    private void setupRadioButtons() {
        phoneRadioButtons = new ArrayList<>();
        phoneRadioButtons.add(binding.rbCasa1);
        phoneRadioButtons.add(binding.rbCasa2);
        phoneRadioButtons.add(binding.rbOficina1);
        phoneRadioButtons.add(binding.rbOficina2);
        phoneRadioButtons.add(binding.rbCelular1);
        phoneRadioButtons.add(binding.rbCelular2);
        phoneRadioButtons.add(binding.rbMensajes);

        for (RadioButton rb : phoneRadioButtons) {
            rb.setOnClickListener(v -> {
                for (RadioButton other : phoneRadioButtons) {
                    if (other != rb) {
                        other.setChecked(false);
                    }
                }
                rb.setChecked(true);
            });
        }
    }

    private void setupNacionalidadSpinner() {
        // Cargamos la lista desde strings.xml para soportar multi-idioma
        String[] nacionalidades = getResources().getStringArray(R.array.nacionalidades);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, nacionalidades);
        binding.spinnerNacionalidad.setAdapter(adapter);
    }

    private void seleccionaBoton(MaterialButton selected) {
        resetEstiloBoton(binding.btnMexico);
        resetEstiloBoton(binding.btnUSA);
        resetEstiloBoton(binding.btnCanada);
        resetEstiloBoton(binding.btnOtro);

        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0A0E21")));
        selected.setTextColor(Color.WHITE);
        selected.setStrokeWidth(0);
    }

    private void resetEstiloBoton(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        button.setTextColor(Color.parseColor("#1E293B"));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        button.setStrokeWidth(1);
    }

    private void cargaFragmentoDireccion(Fragment fragmento) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.addressContainer, fragmento)
                .commit();
    }

    private void guardaDatosViewModel() {
        ContratoModelo Contrato = viewModel.getContratoValue();
        if (Contrato == null) Contrato = new ContratoModelo();

        Contrato.setPais(PaisSeleccionado);
        
        if (binding.spinnerNacionalidad.getSelectedItem() != null) {
            Contrato.setNacionalidad(binding.spinnerNacionalidad.getSelectedItem().toString());
        }

        List<String> emails = new ArrayList<>();
        if (!binding.etEmail1.getText().toString().isEmpty()) emails.add(binding.etEmail1.getText().toString());
        if (!binding.etEmail2.getText().toString().isEmpty()) emails.add(binding.etEmail2.getText().toString());
        if (!binding.etEmail3.getText().toString().isEmpty()) emails.add(binding.etEmail3.getText().toString());
        if (!binding.etEmail4.getText().toString().isEmpty()) emails.add(binding.etEmail4.getText().toString());
        Contrato.setEmails(emails);
        Contrato.setNoCorreo(binding.cbNoCorreo.isChecked());

        List<ContratoModelo.InfoTelefono> phones = new ArrayList<>();
        agregaTelefono(phones, "Casa 1", binding.etLadaCasa1.getText().toString(), binding.etNumeroCasa1.getText().toString(), binding.cbWsCasa1.isChecked(), binding.rbCasa1.isChecked());
        agregaTelefono(phones, "Casa 2", binding.etLadaCasa2.getText().toString(), binding.etNumeroCasa2.getText().toString(), binding.cbWsCasa2.isChecked(), binding.rbCasa2.isChecked());
        agregaTelefono(phones, "Oficina 1", binding.etLadaOficina1.getText().toString(), binding.etNumeroOficina1.getText().toString(), binding.cbWsOficina1.isChecked(), binding.rbOficina1.isChecked());
        agregaTelefono(phones, "Oficina 2", binding.etLadaOficina2.getText().toString(), binding.etNumeroOficina2.getText().toString(), binding.cbWsOficina2.isChecked(), binding.rbOficina2.isChecked());
        agregaTelefono(phones, "Celular 1", binding.etLadaCel1.getText().toString(), binding.etNumeroCel1.getText().toString(), binding.cbWsCel1.isChecked(), binding.rbCelular1.isChecked());
        agregaTelefono(phones, "Celular 2", binding.etLadaCel2.getText().toString(), binding.etNumeroCel2.getText().toString(), binding.cbWsCel2.isChecked(), binding.rbCelular2.isChecked());
        agregaTelefono(phones, "Mensajes", binding.etLadaMensajes.getText().toString(), binding.etNumeroMensajes.getText().toString(), binding.cbWsMensajes.isChecked(), binding.rbMensajes.isChecked());
        Contrato.setTelefonos(phones);

        viewModel.setContrato(Contrato);
    }

    private void agregaTelefono(List<ContratoModelo.InfoTelefono> list, String tag, String lada, String num, boolean ws, boolean main) {
        if (!num.isEmpty()) {
            list.add(new ContratoModelo.InfoTelefono(tag, lada, num, ws, main));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
