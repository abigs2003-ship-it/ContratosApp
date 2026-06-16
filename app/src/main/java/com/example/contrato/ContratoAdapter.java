package com.example.contrato;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.databinding.ItemContratoHistorialBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContratoAdapter extends RecyclerView.Adapter<ContratoAdapter.ViewHolder> {

    private List<ContratoModelo> contratoList;
    private OnContratoClickListener clickListener;
    private OnContratoStatusListener estatusListener;
    private boolean esModoEdicion = false;
    private static final String[] MESES_ES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};
    private static final String[] MESES_EN = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dec"};

    public interface OnContratoEditListener {
        void onContratoEditClick(ContratoModelo contrato);
    }

    private OnContratoEditListener editListener;

    public void setOnContratoEditListener(OnContratoEditListener listener) {
        this.editListener = listener;
    }
    public interface OnContratoClickListener {
        void onContratoClick(ContratoModelo contrato);
    }

    public interface OnContratoStatusListener {
        void onContratoStatusClick(ContratoModelo contrato);
    }

    public ContratoAdapter(
            List<ContratoModelo> contratoList,
            OnContratoClickListener clickListener
    ) {
        this.contratoList = contratoList;
        this.clickListener = clickListener;
    }

    public void setOnContratoEstatusListener(
            OnContratoStatusListener listener
    ) {
        this.estatusListener = listener;
    }

    public void setContratos(
            List<ContratoModelo> contratos
    ) {
        this.contratoList = contratos;
        notifyDataSetChanged();
    }

    public void setEsModoEdicion(
            boolean esModoEdicion
    ) {
        this.esModoEdicion = esModoEdicion;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemContratoHistorialBinding binding =
                ItemContratoHistorialBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContratoModelo contrato = contratoList.get(position);

        holder.binding.tvClientName.setText(contrato.getClientName());
        holder.binding.tvContractId.setText("ID: #" + contrato.getId());
        holder.binding.tvCreatedDate.setText("Creado: " + convertirMesANombre(contrato.getFechaCreacion()));

        if (contrato.getFechaModificacion() == null) {
            holder.binding.tvModifiedDate.setText("");
        } else {
            holder.binding.tvModifiedDate.setText("Modificado: " + convertirMesANombre(contrato.getFechaModificacion()));
        }

        holder.binding.tvEstatus.setText("Estatus: " + contrato.getEstatus());

        holder.binding.textContainer.setOnClickListener(null);
        holder.binding.textContainer.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 1000);
            Log.d("CONTRATO_CLICK", "Contrato clickeado: " + contrato.getId());
            clickListener.onContratoClick(contrato);
        });

        holder.binding.btnCancelar.setOnClickListener(null);
        holder.binding.btnReactivar.setOnClickListener(null);
        holder.binding.btnEditar.setOnClickListener(v -> {
            if (editListener != null) editListener.onContratoEditClick(contrato);
        });
        holder.binding.btnCancelar.setOnClickListener(v -> {
            if (estatusListener != null) estatusListener.onContratoStatusClick(contrato);
        });

        holder.binding.btnReactivar.setOnClickListener(v -> {
            if (estatusListener != null) estatusListener.onContratoStatusClick(contrato);
        });

        holder.binding.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
            intent.putExtra("ID_CONTRATO", Long.parseLong(contrato.getId()));
            holder.itemView.getContext().startActivity(intent);
        });

        if (esModoEdicion) {
            if ("A".equals(contrato.getEstatus()) ||"M".equals(contrato.getEstatus()) ) {
                holder.binding.btnCancelar.setVisibility(View.VISIBLE);
                holder.binding.btnReactivar.setVisibility(View.GONE);
            } else if("C".equals(contrato.getEstatus())){
                holder.binding.btnCancelar.setVisibility(View.GONE);
                holder.binding.btnReactivar.setVisibility(View.VISIBLE);
            }
            holder.binding.btnEditar.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnEditar.setVisibility(View.GONE);
            holder.binding.btnCancelar.setVisibility(View.GONE);
            holder.binding.btnReactivar.setVisibility(View.GONE);
        }
    }


    private boolean esIngles() {
        return Locale.getDefault().getLanguage().equals("en");
    }


    //cambia de 01/02/2000 a 01/feb/2000
    private String convertirMesANombre(String texto) {
        if (texto == null || texto.isEmpty()) return "";

        try {
            // Input format EXACTLY as your DB gives it
            SimpleDateFormat input = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.US
            );

            SimpleDateFormat output;

            if (esIngles()) {
                output = new SimpleDateFormat(
                        "MMM/dd/yyyy HH:mm",
                        Locale.US
                );
            } else {
                output = new SimpleDateFormat(
                        "dd/MMM/yyyy HH:mm",
                        new Locale("es")
                );
            }

            return output.format(input.parse(texto))
                    .toLowerCase();


        } catch (Exception e) {
            e.printStackTrace();
            return texto;
        }
    }
    @Override
    public int getItemCount() {
        return contratoList != null
                ? contratoList.size()
                : 0;
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ItemContratoHistorialBinding binding;

        public ViewHolder(
                ItemContratoHistorialBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
