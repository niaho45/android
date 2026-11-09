package com.example.gym.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gym.R;
import com.example.gym.model.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise);
    }

    private final List<Exercise> items = new ArrayList<>();
    private final OnExerciseClickListener clickListener;

    public ExerciseAdapter(OnExerciseClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<Exercise> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && clickListener != null) {
                clickListener.onExerciseClick(items.get(position));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = items.get(position);
        holder.tvExercisePart.setText(exercise.bodyPart);
        holder.tvExerciseName.setText(exercise.name);
        holder.tvExerciseEquipment.setText(exercise.equipment);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvExercisePart;
        final TextView tvExerciseName;
        final TextView tvExerciseEquipment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExercisePart = itemView.findViewById(R.id.tvExercisePart);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseEquipment = itemView.findViewById(R.id.tvExerciseEquipment);
        }
    }
}
