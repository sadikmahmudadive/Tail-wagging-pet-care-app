package com.example.tailwagging;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.tvDay);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this); // Set long click listener
    }

    @Override
    public void onClick(View view) {
        if (onItemListener != null) {
            DayCell cell = (DayCell) itemView.getTag();
            onItemListener.onItemClick(getAdapterPosition(), cell);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (onItemListener != null) {
            DayCell cell = (DayCell) itemView.getTag();
            onItemListener.onItemLongClick(getAdapterPosition(), cell);
            return true;
        }
        return false;
    }
}