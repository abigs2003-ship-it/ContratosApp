package com.example.contrato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.databinding.ItemContratoHistorialBinding;
import com.example.contrato.databinding.ItemContratoHistorialBinding;
import java.util.List;

public class ContratoAdapter extends RecyclerView.Adapter<ContratoAdapter.ViewHolder> {

    private List<ContratoModelo> ContratoList;
    private OnContratoClickListener listener;
    private OnContratoDeleteListener deleteListener;
    private boolean isEditMode = false;

    public interface OnContratoClickListener {
        void onContratoClick(ContratoModelo Contrato);
    }

    public interface OnContratoDeleteListener {
        void onContratoDelete(ContratoModelo Contrato);
    }

    public ContratoAdapter(List<ContratoModelo> ContratoList, OnContratoClickListener listener) {
        this.ContratoList = ContratoList;
        this.listener = listener;
    }

    public void setOnContratoDeleteListener(OnContratoDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setContratos(List<ContratoModelo> Contratos) {
        this.ContratoList = Contratos;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContratoHistorialBinding binding = ItemContratoHistorialBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContratoModelo Contrato = ContratoList.get(position);
        holder.binding.tvClientName.setText(Contrato.getClientName());
        holder.binding.tvContractId.setText("ID: #" + Contrato.getId());
        holder.binding.tvCreatedDate.setText("Creado: " + Contrato.getCreationDate());
        holder.binding.tvModifiedDate.setText("Modificado: " + Contrato.getModifiedDate());
        

        
        holder.binding.textContainer.setOnClickListener(v -> listener.onContratoClick(Contrato));
        
        holder.binding.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onContratoDelete(Contrato);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ContratoList != null ? ContratoList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContratoHistorialBinding binding;
        public ViewHolder(ItemContratoHistorialBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
