package com.example.tailwagging;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.142857); // 7 rows
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DayCell dayCell = dayCells.get(position);
        TextView tvDay = holder.dayOfMonth;

        tvDay.setText(dayCell.dayText);

        if (dayCell.dayText.isEmpty()) {
            tvDay.setBackgroundResource(android.R.color.transparent);
            tvDay.setTextColor(Color.TRANSPARENT);
        } else if (dayCell.isOtherMonth) {
            tvDay.setBackgroundResource(R.drawable.bg_calendar_dey_event_inactive);
            tvDay.setTextColor(Color.parseColor("#B0B0B0"));
        } else if (dayCell.isSelected || dayCell.isToday) {
            tvDay.setBackgroundResource(R.drawable.bg_calendar_dey_event_selected);
            tvDay.setTextColor(Color.WHITE);
        } else {
            tvDay.setBackgroundResource(android.R.color.transparent);
            tvDay.setTextColor(Color.parseColor("#222222"));
        }

        holder.itemView.setTag(dayCell); // So ViewHolder can access DayCell in click
    }

    @Override
    public int getItemCount() {
        return dayCells.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, DayCell dayCell);
    }
}