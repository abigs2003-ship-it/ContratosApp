package com.example.contrato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contrato.databinding.ItemContractHistoryBinding;
import java.util.List;

public class ContratoAdapter extends RecyclerView.Adapter<ContratoAdapter.ViewHolder> {

    private List<ContratoModelo> contractList;
    private OnContractClickListener listener;
    private OnContractDeleteListener deleteListener;
    private boolean isEditMode = false;

    public interface OnContractClickListener {
        void onContractClick(ContratoModelo contract);
    }

    public interface OnContractDeleteListener {
        void onContractDelete(ContratoModelo contract);
    }

    public ContratoAdapter(List<ContratoModelo> contractList, OnContractClickListener listener) {
        this.contractList = contractList;
        this.listener = listener;
    }

    public void setOnContractDeleteListener(OnContractDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setContracts(List<ContratoModelo> contracts) {
        this.contractList = contracts;
        notifyDataSetChanged();
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContractHistoryBinding binding = ItemContractHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContratoModelo contract = contractList.get(position);
        holder.binding.tvClientName.setText(contract.getClientName());
        holder.binding.tvContractId.setText("ID: #" + contract.getId());
        holder.binding.tvCreatedDate.setText("Creado: " + contract.getCreationDate());
        holder.binding.tvModifiedDate.setText("Modificado: " + contract.getModifiedDate());
        
        holder.binding.textContainer.setOnClickListener(v -> listener.onContractClick(contract));
        
        holder.binding.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onContractDelete(contract);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contractList != null ? contractList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemContractHistoryBinding binding;
        public ViewHolder(ItemContractHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
