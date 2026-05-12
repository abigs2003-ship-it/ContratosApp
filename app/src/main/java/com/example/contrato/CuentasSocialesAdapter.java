package com.example.contrato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contrato.R;

import java.util.List;

public class CuentasSocialesAdapter extends RecyclerView.Adapter<CuentasSocialesAdapter.ViewHolder> {

    public enum Plataforma { FACEBOOK, INSTAGRAM, TWITTER }

    public static class CuentaSocial {
        public String nombre;
        public Plataforma plataforma;

        public CuentaSocial(String nombre, Plataforma plataforma) {
            this.nombre = nombre;
            this.plataforma = plataforma;
        }

        public String getNombre() { return nombre; }
        public Plataforma getPlataforma() { return plataforma; }


    }

    public interface OnEliminarListener {
        void onEliminar(int position);
    }

    private final List<CuentaSocial> items;
    private final OnEliminarListener listener;

    public CuentasSocialesAdapter(List<CuentaSocial> items, OnEliminarListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuenta_social, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CuentaSocial item = items.get(position);

        holder.tvNombre.setText(item.nombre);

        switch (item.plataforma) {
            case FACEBOOK:
                holder.ivIcon.setImageResource(R.drawable.ic_facebook);
                holder.ivIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_circle_facebook));
                holder.tvTag.setText("Facebook");
                holder.tvTag.setTextColor(0xFF1877F2);
                holder.tvTag.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_tag_facebook));
                break;

            case INSTAGRAM:
                holder.ivIcon.setImageResource(R.drawable.ic_instagram);
                holder.ivIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_circle_instagram));
                holder.tvTag.setText("Instagram");
                holder.tvTag.setTextColor(0xFFC13584);
                holder.tvTag.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_tag_instagram));
                break;

            case TWITTER:
                holder.ivIcon.setImageResource(R.drawable.ic_x_twitter);
                holder.ivIcon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_circle_twitter));
                holder.tvTag.setText("X");
                holder.tvTag.setTextColor(0xFF1DA1F2);
                holder.tvTag.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_tag_twitter));
                break;
        }

        holder.btnEliminar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                items.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) listener.onEliminar(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<CuentaSocial> getItems() {
        return items;
    }

    public void agregarCuenta(CuentaSocial cuenta) {
        items.add(cuenta);
        notifyItemInserted(items.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNombre, tvTag;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPlatformIcon);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvTag = itemView.findViewById(R.id.tvPlatformTag);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
