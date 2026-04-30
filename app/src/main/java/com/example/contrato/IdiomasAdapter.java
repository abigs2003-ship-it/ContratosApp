package com.example.contrato;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IdiomasAdapter extends ArrayAdapter<String> {

    public IdiomasAdapter(@NonNull Context context, @NonNull String[] objects) {
        super(context, R.layout.spinner_idiomas, R.id.text, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_idiomas);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_idiomas_dropdown);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        TextView textView = view.findViewById(R.id.text);
        ImageView imageView = view.findViewById(R.id.icon);

        textView.setText(getItem(position));

        if (resource == R.layout.spinner_idiomas_dropdown) {
            imageView.setColorFilter(Color.BLACK);
        } else {
            imageView.setColorFilter(Color.WHITE);
        }

        return view;
    }
}
