package com.example.gym.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gym.R;
import com.example.gym.model.TrainingItem;

import java.util.List;
import java.util.Locale;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemove(int position);
    }

    public interface OnEditClickListener {
        void onEdit(int position);
    }

    private final List<TrainingItem> items;
    private final String bodyPart;
    private final OnEditClickListener editListener;
    private final OnRemoveClickListener removeListener;

    public TrainingAdapter(List<TrainingItem> items, String bodyPart,
                           OnEditClickListener editListener,
                           OnRemoveClickListener removeListener) {
        this.items = items;
        this.bodyPart = bodyPart;
        this.editListener = editListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_training, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && editListener != null) {
                editListener.onEdit(position);
            }
        });
        holder.btnRemoveExercise.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && removeListener != null) {
                removeListener.onRemove(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrainingItem item = items.get(position);
        holder.tvTrainingPart.setText(bodyPart);
        holder.tvTrainingExerciseName.setText(item.name);
        holder.tvTrainingExerciseDetail.setText(
                String.format(Locale.getDefault(), "%d 组 × %d 次", item.sets, item.reps));
        if (item.weight > 0) {
            holder.tvTrainingWeight.setText(
                    String.format(Locale.US, "· %.1f kg", item.weight));
            holder.tvTrainingWeight.setVisibility(View.VISIBLE);
        } else {
            holder.tvTrainingWeight.setVisibility(View.GONE);
        }
        if (item.equipment != null && !item.equipment.isEmpty()) {
            holder.tvTrainingEquipment.setText("· " + item.equipment);
            holder.tvTrainingEquipment.setVisibility(View.VISIBLE);
        } else {
            holder.tvTrainingEquipment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTrainingPart;
        final TextView tvTrainingExerciseName;
        final TextView tvTrainingExerciseDetail;
        final TextView tvTrainingWeight;
        final TextView tvTrainingEquipment;
        final TextView btnRemoveExercise;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainingPart = itemView.findViewById(R.id.tvTrainingPart);
            tvTrainingExerciseName = itemView.findViewById(R.id.tvTrainingExerciseName);
            tvTrainingExerciseDetail = itemView.findViewById(R.id.tvTrainingExerciseDetail);
            tvTrainingWeight = itemView.findViewById(R.id.tvTrainingWeight);
            tvTrainingEquipment = itemView.findViewById(R.id.tvTrainingEquipment);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
        }
    }
}
