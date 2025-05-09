package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private List<Event> events;

    public EventsAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvEventTitle.setText(event.title);

        String dateStr = event.date != null ? event.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String fromStr = event.fromTime != null ? event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        String toStr = event.toTime != null ? event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        holder.tvEventDateTime.setText(dateStr + " | " + fromStr + " - " + toStr);

        holder.tvEventCategory.setText(event.category != null ? event.category : "");
        holder.tvEventNote.setText(event.note != null ? event.note : "");
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvEventDateTime, tvEventCategory, tvEventNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventNote = itemView.findViewById(R.id.tvEventNote);
        }
    }
}