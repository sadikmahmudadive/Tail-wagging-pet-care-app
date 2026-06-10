package com.example.tailwagging;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<DayCell> dayCells;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<DayCell> dayCells, OnItemListener onItemListener) {
        this.dayCells = dayCells;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_day_cell, parent, false);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayCell dayCell = dayCells.get(position);
        TextView tvDay = holder.dayOfMonth;
        View indicator = holder.eventIndicator;

        tvDay.setText(dayCell.dayText);

        if (dayCell.dayText == null || dayCell.dayText.isEmpty()) {
            tvDay.setBackgroundResource(android.R.color.transparent);
            tvDay.setTextColor(Color.TRANSPARENT);
            indicator.setVisibility(View.GONE);
        } else if (dayCell.isOtherMonth) {
            tvDay.setBackgroundResource(android.R.color.transparent);
            tvDay.setTextColor(ContextCompat.getColor(tvDay.getContext(), R.color.grey_medium));
            indicator.setVisibility(View.GONE);
        } else if (dayCell.isSelected) {
            tvDay.setBackgroundResource(R.drawable.bg_calendar_dey_event_selected);
            tvDay.setTextColor(Color.WHITE);
            indicator.setVisibility(dayCell.hasEvents ? View.VISIBLE : View.GONE);
        } else if (dayCell.isToday) {
            tvDay.setBackgroundResource(R.drawable.bg_calendar_dey_event_inactive);
            tvDay.setTextColor(ContextCompat.getColor(tvDay.getContext(), R.color.md_theme_light_primary));
            indicator.setVisibility(dayCell.hasEvents ? View.VISIBLE : View.GONE);
        } else {
            tvDay.setBackgroundResource(android.R.color.transparent);
            tvDay.setTextColor(ContextCompat.getColor(tvDay.getContext(), R.color.black));
            indicator.setVisibility(dayCell.hasEvents ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setTag(dayCell); // So ViewHolder can access DayCell in click
    }

    @Override
    public int getItemCount() {
        return dayCells.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, DayCell dayCell);
        void onItemLongClick(int position, DayCell dayCell); // Added for long press
    }
}