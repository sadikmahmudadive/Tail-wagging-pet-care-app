package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthAllergyAdapter extends RecyclerView.Adapter<HealthAllergyAdapter.ViewHolder> {

    private final List<Event> allergies;

    public HealthAllergyAdapter(List<Event> allergies) {
        this.allergies = allergies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_allergy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = allergies.get(position);
        holder.tvAllergyName.setText(event.title);
        holder.tvDescription.setText(event.note != null && !event.note.isEmpty() ? event.note : "No details provided");
        holder.tvDoctor.setText(event.category); // Reusing category field or similar
    }

    @Override
    public int getItemCount() {
        return allergies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAllergyName, tvDescription, tvDoctor;

        ViewHolder(View itemView) {
            super(itemView);
            tvAllergyName = itemView.findViewById(R.id.tvAllergyName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
        }
    }
}