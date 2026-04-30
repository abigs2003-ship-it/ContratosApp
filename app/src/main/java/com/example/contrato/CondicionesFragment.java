package com.example.contrato;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.databinding.DialogDescuentosBinding;
import com.example.contrato.databinding.FragmentCondicionesBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CondicionesFragment extends Fragment {
    private FragmentCondicionesBinding binding;
    private List<DescuentoItem> listaDescuentos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCondicionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAnioUsoSpinner();
        setupMonedaSpinner();
        setupCalculosPrecios();
        setupDynamicContratos();
        setupEngancheCalculos();
        setupEngancheSalaCalculos();
        setupDynamicDescuentos();
        setupDynamicPagosDiferidos();
        setupTotalesCalculos();
        setupPaymentTypeLogic();
        setupTipoVentaLogic();
        setupDescuentosDialog();
        
        binding.btnLimpiarInventario.setOnClickListener(v -> limpiarInventario());
    }

    private void setupTipoVentaLogic() {
        binding.rgTipoVenta.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isUpgrade = checkedId == R.id.rbUpgrade;
            binding.btnUpgradeMonto.setEnabled(isUpgrade);
            if (!isUpgrade) {
                binding.editMontoCuenta.setText("0.00");
            }
        });
    }

    private void setupDescuentosDialog() {
        binding.btnVariosDesc.setOnClickListener(v -> showDescuentosDialog());
    }

    private void showDescuentosDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogDescuentosBinding dialogBinding = DialogDescuentosBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        DescuentoAdapter adapter = new DescuentoAdapter(new ArrayList<>(listaDescuentos));
        dialogBinding.rvDescuentos.setLayoutManager(new LinearLayoutManager(getContext()));
        dialogBinding.rvDescuentos.setAdapter(adapter);

        updateDialogTotal(dialogBinding, adapter.getItems());

        dialogBinding.btnAgregar.setOnClickListener(v -> {
            String desc = dialogBinding.editDescModal.getText().toString();
            String montoStr = dialogBinding.editMontoModal.getText().toString();
            double monto = parseDouble(montoStr);
            if (!desc.isEmpty() && monto > 0) {
                int nextNo = adapter.getItemCount() + 1;
                adapter.addItem(new DescuentoItem(nextNo, desc, "REM-" + nextNo, monto, monto, "REF-" + nextNo));
                updateDialogTotal(dialogBinding, adapter.getItems());
                dialogBinding.editDescModal.setText("");
                dialogBinding.editMontoModal.setText("");
            }
        });

        dialogBinding.btnQuitar.setOnClickListener(v -> {
            adapter.removeLast();
            updateDialogTotal(dialogBinding, adapter.getItems());
        });

        dialogBinding.btnQuitarTodos.setOnClickListener(v -> {
            adapter.clear();
            updateDialogTotal(dialogBinding, adapter.getItems());
        });

        dialogBinding.btnLimpiar.setOnClickListener(v -> {
            dialogBinding.editDescModal.setText("");
            dialogBinding.editMontoModal.setText("");
        });

        dialogBinding.btnRegresar.setOnClickListener(v -> dialog.dismiss());
        
        dialogBinding.btnAceptar.setOnClickListener(v -> {
            listaDescuentos = adapter.getItems();
            double total = 0;
            for (DescuentoItem item : listaDescuentos) total += item.montoDesc;
            binding.editVarios.setText(String.format(Locale.US, "%.2f", total));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateDialogTotal(DialogDescuentosBinding db, List<DescuentoItem> items) {
        double total = 0;
        for (DescuentoItem item : items) total += item.montoDesc;
        db.tvTotalDescuentos.setText(String.format(Locale.US, "Total: $%.2f", total));
    }

    private void setupAnioUsoSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, years);
        binding.spAnioUso.setAdapter(adapter);
    }

    private void setupMonedaSpinner() {
        String[] currencies = {"MXN", "USD", "CAD"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencies);
        binding.spMoneda.setAdapter(adapter);
    }

    private void setupCalculosPrecios() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculatePrecioNeto();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editPrecioBruto.addTextChangedListener(watcher);
        binding.editMontoCuenta.addTextChangedListener(watcher);
    }

    private void calculatePrecioNeto() {
        try {
            double bruto = parseDouble(binding.editPrecioBruto.getText().toString());
            double cuenta = parseDouble(binding.editMontoCuenta.getText().toString());
            double neto = bruto - cuenta;
            binding.editPrecioNeto.setText(String.format(Locale.US, "%.2f", neto));
            updateEngancheMN();
            updateEngancheSalaMN();
            calculateSaldoEnganche();
        } catch (Exception e) {
            binding.editPrecioNeto.setText("0.00");
        }
    }

    private void setupDynamicContratos() {
        binding.editNoContratosVenta.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.containerContratosDinamicos.removeAllViews();
                try {
                    int num = Integer.parseInt(s.toString());
                    int limit = Math.min(num, 6);
                    for (int i = 0; i < limit; i++) {
                        EditText et = createSmallEditText("No.");
                        binding.containerContratosDinamicos.addView(et);
                    }
                } catch (NumberFormatException ignored) {}
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupEngancheCalculos() {
        binding.editEnganchePorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEnganchePorcentaje.hasFocus()) updateEngancheMN();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.editEngancheMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheMonto.hasFocus()) updateEnganchePercent();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupEngancheSalaCalculos() {
        binding.editEngancheSalaPorcentaje.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaPorcentaje.hasFocus()) updateEngancheSalaMN();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.editEngancheSalaMonto.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (binding.editEngancheSalaMonto.hasFocus()) updateEngancheSalaPercent();
                calculateSaldoEnganche();
                calculateTotalPagoSala();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateEngancheMN() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double percent = parseDouble(binding.editEnganchePorcentaje.getText().toString());
            binding.editEngancheMonto.setText(String.format(Locale.US, "%.2f", neto * (percent / 100)));
        } catch (Exception ignored) {}
    }

    private void updateEnganchePercent() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double amount = parseDouble(binding.editEngancheMonto.getText().toString());
            binding.editEnganchePorcentaje.setText(String.format(Locale.US, "%.2f", (amount / neto) * 100));
        } catch (Exception ignored) {}
    }

    private void updateEngancheSalaMN() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double percent = parseDouble(binding.editEngancheSalaPorcentaje.getText().toString());
            binding.editEngancheSalaMonto.setText(String.format(Locale.US, "%.2f", neto * (percent / 100)));
        } catch (Exception ignored) {}
    }

    private void updateEngancheSalaPercent() {
        try {
            double neto = parseDouble(binding.editPrecioNeto.getText().toString());
            double amount = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            binding.editEngancheSalaPorcentaje.setText(String.format(Locale.US, "%.2f", (amount / neto) * 100));
        } catch (Exception ignored) {}
    }

    private void setupDynamicDescuentos() {
        binding.editNoDesc.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.containerDescuentosDinamicos.removeAllViews();
                try {
                    int num = Integer.parseInt(s.toString());
                    for (int i = 0; i < num; i++) {
                        EditText et = createSmallEditText("Desc " + (i + 1));
                        binding.containerDescuentosDinamicos.addView(et);
                    }
                } catch (Exception ignored) {}
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDynamicPagosDiferidos() {
        TextWatcher diferidoWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePagosDiferidos();
                calculateSaldoEnganche();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editEngDiferido.addTextChangedListener(diferidoWatcher);
        binding.editNoPagosEng.addTextChangedListener(diferidoWatcher);
    }

    private void updatePagosDiferidos() {
        binding.containerPagosDinamicos.removeAllViews();
        try {
            double totalDiferido = parseDouble(binding.editEngDiferido.getText().toString());
            int numPagos = Integer.parseInt(binding.editNoPagosEng.getText().toString());
            if (numPagos <= 0) return;
            double split = totalDiferido / numPagos;

            for (int i = 0; i < numPagos; i++) {
                EditText et = createSmallEditText(null);
                et.setText(String.format(Locale.US, "%.2f", split));
                binding.containerPagosDinamicos.addView(et);
            }
        } catch (Exception ignored) {}
    }

    private EditText createSmallEditText(String hint) {
        EditText et = new EditText(requireContext());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        lp.setMargins(0, 4, 8, 4);
        et.setLayoutParams(lp);
        et.setBackgroundResource(R.drawable.bg_card);
        et.setTextSize(12);
        et.setPadding(8, 0, 8, 0);
        if (hint != null) et.setHint(hint);
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return et;
    }

    private void calculateSaldoEnganche() {
        try {
            double engancheMonto = parseDouble(binding.editEngancheMonto.getText().toString());
            double salaMonto = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            double diferidoMonto = parseDouble(binding.editEngDiferido.getText().toString());
            double saldo = engancheMonto - salaMonto - diferidoMonto;
            binding.editSaldoEng.setText(String.format(Locale.US, "%.2f", saldo));
        } catch (Exception ignored) {}
    }

    private void setupTotalesCalculos() {
        TextWatcher totalsWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalPagoSala();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.editCostoContrato.addTextChangedListener(totalsWatcher);
        binding.editCostoMembresia.addTextChangedListener(totalsWatcher);
    }

    private void calculateTotalPagoSala() {
        try {
            double engancheSala = parseDouble(binding.editEngancheSalaMonto.getText().toString());
            double costoContrato = parseDouble(binding.editCostoContrato.getText().toString());
            double costoMembresia = parseDouble(binding.editCostoMembresia.getText().toString());
            double total = engancheSala + costoContrato + costoMembresia;
            binding.editTotalPagoSala.setText(String.format(Locale.US, "%.2f", total));
        } catch (Exception ignored) {}
    }

    private void setupPaymentTypeLogic() {
        binding.rgTipoPago.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbContado) {
                binding.editEnganchePorcentaje.setText("100");
                updateEngancheMN();
                binding.layoutCollapsibleFinancing.setVisibility(View.GONE);
            } else {
                binding.editEnganchePorcentaje.setText("");
                binding.editEngancheMonto.setText("");
                binding.layoutCollapsibleFinancing.setVisibility(View.VISIBLE);
            }
        });
    }

    private void limpiarInventario() {
        binding.editNoAnios.setText("");
        binding.spUnidad.setSelection(0);
        binding.spTemporada.setSelection(0);
        binding.spAnioUso.setSelection(0);
    }

    private double parseDouble(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Double.parseDouble(s.replaceAll("[$,]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Helper classes for Descuentos
    private static class DescuentoItem {
        final int no;
        final String descripcion;
        final String remision;
        final double montoRem;
        final double montoDesc;
        final String referencia;

        DescuentoItem(int no, String descripcion, String remision, double montoRem, double montoDesc, String referencia) {
            this.no = no;
            this.descripcion = descripcion;
            this.remision = remision;
            this.montoRem = montoRem;
            this.montoDesc = montoDesc;
            this.referencia = referencia;
        }
    }

    private static class DescuentoAdapter extends RecyclerView.Adapter<DescuentoAdapter.ViewHolder> {
        private final List<DescuentoItem> items;

        DescuentoAdapter(List<DescuentoItem> items) { this.items = items; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(4, 4, 4, 4);
            
            for (int i = 0; i < 6; i++) {
                TextView tv = new TextView(parent.getContext());
                int dipWidth;
                if (i == 0) dipWidth = 40;
                else if (i == 1) dipWidth = 120;
                else if (i == 2) dipWidth = 80;
                else dipWidth = 100;

                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipWidth, parent.getResources().getDisplayMetrics());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                
                tv.setLayoutParams(lp);
                tv.setTextSize(11);
                tv.setGravity(android.view.Gravity.CENTER);
                layout.addView(tv);
            }
            return new ViewHolder(layout);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DescuentoItem item = items.get(position);
            ViewGroup layout = (ViewGroup) holder.itemView;
            ((TextView) layout.getChildAt(0)).setText(String.valueOf(item.no));
            ((TextView) layout.getChildAt(1)).setText(item.descripcion);
            ((TextView) layout.getChildAt(2)).setText(item.remision);
            ((TextView) layout.getChildAt(3)).setText(String.format(Locale.US, "%.2f", item.montoRem));
            ((TextView) layout.getChildAt(4)).setText(String.format(Locale.US, "%.2f", item.montoDesc));
            ((TextView) layout.getChildAt(5)).setText(item.referencia);
        }

        @Override public int getItemCount() { return items.size(); }
        void addItem(DescuentoItem item) { items.add(item); notifyItemInserted(items.size() - 1); }
        void removeLast() { if (!items.isEmpty()) { items.remove(items.size() - 1); notifyItemRemoved(items.size()); } }
        void clear() { int size = items.size(); items.clear(); notifyItemRangeRemoved(0, size); }
        List<DescuentoItem> getItems() { return items; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }
}
