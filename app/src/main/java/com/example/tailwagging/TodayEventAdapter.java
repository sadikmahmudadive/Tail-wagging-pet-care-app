package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TodayEventAdapter extends RecyclerView.Adapter<TodayEventAdapter.TodayEventViewHolder> {
    private List<Event> events;
    private Context context;
    private OnEventChangedListener eventChangedListener;

    public interface OnEventChangedListener {
        void onEventDeleted();
    }

    public TodayEventAdapter(Context context, List<Event> events, OnEventChangedListener listener) {
        this.events = new ArrayList<>(events);
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
        holder.tvEventTime.setText(event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        holder.tvEventTitle.setText(event.title);
        holder.tvEventCategory.setText(event.category);
        
        if (event.petName != null) {
            holder.tvPetName.setText(event.petName);
            holder.tvPetName.setVisibility(View.VISIBLE);
            holder.ivPetIcon.setVisibility(View.VISIBLE);
        } else {
            holder.tvPetName.setVisibility(View.GONE);
            holder.ivPetIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            showEventDetailsDialog(event);
        });

        holder.itemView.setOnLongClickListener(v -> {
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
            return true;
        });
    }

    private void showEventDetailsDialog(Event event) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.etEventTitle); // It's an EditText in layout
        tvTitle.setFocusable(false);
        tvTitle.setClickable(false);
        tvTitle.setText(event.title);

        TextView tvNote = dialogView.findViewById(R.id.etNote); // It's an EditText in layout
        tvNote.setFocusable(false);
        tvNote.setClickable(false);
        tvNote.setText(event.note);

        TextView tvFromTime = dialogView.findViewById(R.id.tvFromTime);
        tvFromTime.setText(event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        TextView tvToTime = dialogView.findViewById(R.id.tvToTime);
        tvToTime.setText(event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        // Hide elements not needed for view mode
        dialogView.findViewById(R.id.categoryLayout).setVisibility(View.GONE);
        // Also hide the HorizontalScrollView container of categoryLayout
        if (dialogView.findViewById(R.id.categoryLayout).getParent() instanceof View) {
            ((View) dialogView.findViewById(R.id.categoryLayout).getParent()).setVisibility(View.GONE);
        }
        dialogView.findViewById(R.id.spinnerPetSelector).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnSaveEvent).setVisibility(View.GONE);
        
        com.google.android.material.materialswitch.MaterialSwitch switchReminder = dialogView.findViewById(R.id.switchReminder);
        switchReminder.setChecked(event.isReminderEnabled);
        switchReminder.setEnabled(false); // Read-only in details view
        
        // Show pet name if exists
        if (event.petName != null) {
            TextView labelPet = new TextView(context);
            labelPet.setText(String.format("Pet: %s", event.petName));
            labelPet.setPadding(0, 20, 0, 0);
            labelPet.setTextColor(ContextCompat.getColor(context, R.color.black));
            labelPet.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // The parent of HorizontalScrollView is the main LinearLayout
            ViewGroup mainContainer = (ViewGroup) dialogView.findViewById(R.id.categoryLayout).getParent().getParent();
            // Find index of HorizontalScrollView and insert label before it
            int index = mainContainer.indexOfChild((View) dialogView.findViewById(R.id.categoryLayout).getParent());
            mainContainer.addView(labelPet, index);
        }

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events); // Defensive copy
        notifyDataSetChanged();
    }

    public static class TodayEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTime, tvEventTitle, tvEventCategory, tvPetName;
        ImageView ivPetIcon;

        public TodayEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            ivPetIcon = itemView.findViewById(R.id.ivPetIcon);
        }
    }
}