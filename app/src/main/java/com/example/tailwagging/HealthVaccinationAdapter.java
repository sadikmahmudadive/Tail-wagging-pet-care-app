package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class HealthVaccinationAdapter extends RecyclerView.Adapter<HealthVaccinationAdapter.ViewHolder> {

    private final List<Event> vaccinations;

    public HealthVaccinationAdapter(List<Event> vaccinations) {
        this.vaccinations = vaccinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_vaccination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = vaccinations.get(position);
        holder.tvVaccineName.setText(event.title);
        holder.tvDate.setText(event.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")));
        holder.tvDoctor.setText(event.note != null && !event.note.isEmpty() ? event.note : "General Clinic");
    }

    @Override
    public int getItemCount() {
        return vaccinations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVaccineName, tvDate, tvDoctor;

        ViewHolder(View itemView) {
            super(itemView);
            tvVaccineName = itemView.findViewById(R.id.tvVaccineName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
        }
    }
}