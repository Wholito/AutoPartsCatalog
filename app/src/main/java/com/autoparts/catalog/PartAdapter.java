package com.autoparts.catalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PartAdapter extends RecyclerView.Adapter<PartAdapter.ViewHolder> {

    private final List<Part> items = new ArrayList<>();
    private OnPartClickListener listener;

    public interface OnPartClickListener {
        void onPartClick(Part part);
    }

    public void setOnPartClickListener(OnPartClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Part> parts) {
        items.clear();
        items.addAll(parts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_part, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Part p = items.get(position);
        holder.title.setText(Objects.requireNonNullElse(p.getTitle(), ""));
        holder.description.setText(Objects.requireNonNullElse(p.getDescription(), ""));
        holder.date.setText(Objects.requireNonNullElse(p.getDate(), ""));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPartClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, description, date;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            date = itemView.findViewById(R.id.item_date);
        }
    }
}
