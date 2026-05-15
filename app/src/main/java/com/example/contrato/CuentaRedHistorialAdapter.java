package com.example.contrato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.ContratoModelo.CuentaRed;
import java.util.List;

public class CuentaRedHistorialAdapter extends RecyclerView.Adapter<CuentaRedHistorialAdapter.ViewHolder> {

    private final List<CuentaRed> items;

    public CuentaRedHistorialAdapter(List<CuentaRed> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuenta_social_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuentaRed item = items.get(position);
        holder.tvNombre.setText(item.usuario);
        holder.tvPlatformTag.setText(item.red);
        holder.btnEliminar.setVisibility(View.GONE);

        if (item.red != null) {
            String red = item.red.toLowerCase();
            if (red.contains("facebook")) {
                holder.ivIcon.setImageResource(R.drawable.ic_facebook);
            } else if (red.contains("instagram")) {
                holder.ivIcon.setImageResource(R.drawable.ic_instagram);
            } else if (red.contains("twitter") || red.equals("x")) {
                holder.ivIcon.setImageResource(R.drawable.ic_x_twitter);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNombre, tvPlatformTag;
        View btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPlatformIcon);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPlatformTag = itemView.findViewById(R.id.tvPlatformTag);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
