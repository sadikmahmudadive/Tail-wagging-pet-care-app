package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TodayEventAdapter extends RecyclerView.Adapter<TodayEventAdapter.TodayEventViewHolder> {
    private List<Event> events;
    private Context context;
    private OnEventChangedListener eventChangedListener;

    public interface OnEventChangedListener {
        void onEventDeleted();
    }

    public TodayEventAdapter(Context context, List<Event> events, OnEventChangedListener listener) {
        this.events = events;
        this.context = context;
        this.eventChangedListener = listener;
    }

    @NonNull
    @Override
    public TodayEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_event, parent, false);
        return new TodayEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayEventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvEventTime.setText(event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "-" + event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        holder.tvEventTitle.setText(event.title);
        holder.tvEventCategory.setText(event.category);
        holder.tvEventDescription.setText(event.note);

        holder.ivMore.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        EventStore.getInstance(context).removeEvent(event.id);
                        AlarmHelper.cancelEventAlarm(context, event.id);
                        events.remove(position);
                        notifyItemRemoved(position);
                        if (eventChangedListener != null) eventChangedListener.onEventDeleted();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public static class TodayEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTime, tvEventTitle, tvEventCategory, tvEventDescription;
        ImageView ivMore;

        public TodayEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            ivMore = itemView.findViewById(R.id.ivMore);
        }
    }
}