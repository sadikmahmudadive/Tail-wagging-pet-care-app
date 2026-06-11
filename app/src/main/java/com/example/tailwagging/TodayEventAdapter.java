package com.example.tailwagging;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodayEventAdapter extends RecyclerView.Adapter<TodayEventAdapter.TodayEventViewHolder> {
    private List<Event> events;
    private Context context;
    private OnEventChangedListener eventChangedListener;

    public interface OnEventChangedListener {
        void onEventDeleted();
    }

    private boolean isHorizontal = false;

    public TodayEventAdapter(Context context, List<Event> events, OnEventChangedListener listener) {
        this.events = new ArrayList<>(events);
        this.context = context;
        this.eventChangedListener = listener;
    }

    public void setHorizontal(boolean horizontal) {
        this.isHorizontal = horizontal;
    }

    @NonNull
    @Override
    public TodayEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_today_event, parent, false);
        
        if (isHorizontal) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = (int) context.getResources().getDimension(R.dimen.event_item_width);
            view.setLayoutParams(lp);
        }

        return new TodayEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayEventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvEventTime.setText(event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        holder.tvEventDate.setText(event.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
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

        TextView tvHeader = dialogView.findViewById(R.id.tvAddEventHeader);
        if (tvHeader != null) {
            tvHeader.setText("Edit Event");
        }

        EditText etTitle = dialogView.findViewById(R.id.etEventTitle);
        etTitle.setText(event.title);

        EditText etNote = dialogView.findViewById(R.id.etNote);
        etNote.setText(event.note);

        TextView tvFromTime = dialogView.findViewById(R.id.tvFromTime);
        tvFromTime.setText(event.fromTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        TextView tvToTime = dialogView.findViewById(R.id.tvToTime);
        tvToTime.setText(event.toTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        LinearLayout categoryLayout = dialogView.findViewById(R.id.categoryLayout);
        Spinner spinnerPetSelector = dialogView.findViewById(R.id.spinnerPetSelector);
        Button btnSaveEvent = dialogView.findViewById(R.id.btnSaveEvent);
        btnSaveEvent.setText("Update Event");

        // Logic for Time Pickers in Edit Mode
        tvFromTime.setOnClickListener(v -> showTimePickerDialog(tvFromTime, event, true));
        tvToTime.setOnClickListener(v -> showTimePickerDialog(tvToTime, event, false));

        // Logic for Category Chips in Edit Mode
        List<String> categories = java.util.Arrays.asList("Vet Appointment", "Vaccination", "Medication", "Food", "Training", "Grooming", "Hangout", "Other");
        final String[] selectedCat = {event.category};
        categoryLayout.removeAllViews();
        List<TextView> chipViews = new ArrayList<>();
        for (String cat : categories) {
            TextView chip = createCategoryChip(cat, context);
            categoryLayout.addView(chip);
            chipViews.add(chip);
            if (cat.equalsIgnoreCase(event.category)) {
                chip.setSelected(true);
                chip.setTextColor(android.graphics.Color.WHITE);
            }
            chip.setOnClickListener(v -> {
                for (TextView c : chipViews) {
                    c.setSelected(false);
                    c.setTextColor(android.graphics.Color.parseColor("#222222"));
                }
                chip.setSelected(true);
                chip.setTextColor(android.graphics.Color.WHITE);
                selectedCat[0] = chip.getText().toString();
            });
        }

        // Logic for Pet Selector in Edit Mode (This is tricky because adapter doesn't have pet list)
        // For now, keep current pet name if we can't fetch the list easily, or just hide it
        spinnerPetSelector.setVisibility(View.GONE);
        TextView tvPetLabel = new TextView(context);
        tvPetLabel.setText("Pet: " + (event.petName != null ? event.petName : "None"));
        tvPetLabel.setPadding(0, 20, 0, 0);
        ((ViewGroup)categoryLayout.getParent().getParent()).addView(tvPetLabel, 3);

        com.google.android.material.materialswitch.MaterialSwitch switchReminder = dialogView.findViewById(R.id.switchReminder);
        switchReminder.setChecked(event.isReminderEnabled);

        btnSaveEvent.setOnClickListener(v -> {
            event.title = etTitle.getText().toString().trim();
            event.note = etNote.getText().toString().trim();
            event.category = selectedCat[0];
            event.isReminderEnabled = switchReminder.isChecked();
            
            // Save updated event
            EventStore.getInstance(context).saveEvents();
            
            // Update Alarm
            AlarmHelper.cancelEventAlarm(context, event.id);
            if (event.isReminderEnabled) {
                AlarmHelper.setEventAlarm(context, event);
            }

            notifyDataSetChanged();
            if (eventChangedListener != null) eventChangedListener.onEventDeleted(); // Refresh callback
            dialog.dismiss();
            android.widget.Toast.makeText(context, "Event updated", android.widget.Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showTimePickerDialog(TextView textView, Event event, boolean isFromTime) {
        java.time.LocalTime currentTime = isFromTime ? event.fromTime : event.toTime;
        new android.app.TimePickerDialog(context, (view, hourOfDay, minute) -> {
            java.time.LocalTime newTime = java.time.LocalTime.of(hourOfDay, minute);
            textView.setText(newTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            if (isFromTime) event.fromTime = newTime;
            else event.toTime = newTime;
        }, currentTime.getHour(), currentTime.getMinute(), true).show();
    }

    private TextView createCategoryChip(String category, Context context) {
        TextView chip = new TextView(context);
        chip.setText(category);
        chip.setPadding(48, 0, 48, 0);
        chip.setHeight(96);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setTextColor(android.graphics.Color.parseColor("#222222"));
        chip.setTextSize(14);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 24, 0);
        chip.setLayoutParams(params);

        chip.setBackgroundResource(R.drawable.chip_category_selector);
        chip.setSelected(false);
        return chip;
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
        TextView tvEventTime, tvEventDate, tvEventTitle, tvEventCategory, tvPetName;
        ImageView ivPetIcon;

        public TodayEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            ivPetIcon = itemView.findViewById(R.id.ivPetIcon);
        }
    }
}