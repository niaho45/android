package com.example.gym.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gym.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(int position);
    }

    private final List<String> categories;
    private final OnCategorySelectedListener listener;
    private int selectedPosition;

    public CategoryAdapter(List<String> categories, int selectedPosition,
                           OnCategorySelectedListener listener) {
        this.categories = new ArrayList<>(categories);
        this.selectedPosition = Math.max(0, Math.min(selectedPosition, categories.size() - 1));
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION || position == selectedPosition) {
                return;
            }
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onCategorySelected(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(categories.get(position));
        holder.itemView.setSelected(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView textView;

        ViewHolder(@NonNull TextView itemView) {
            super(itemView);
            textView = itemView;
        }
    }
}
