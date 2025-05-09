package com.example.tailwagging;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthPickerAdapter extends RecyclerView.Adapter<MonthPickerAdapter.MonthViewHolder> {
    private final List<String> months;
    private int selectedIndex;
    private final OnMonthSelectedListener listener;

    public MonthPickerAdapter(List<String> months, int selectedIndex, OnMonthSelectedListener listener) {
        this.months = months;
        this.selectedIndex = selectedIndex;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_picker_item, parent, false);
        return new MonthViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        String month = months.get(position);
        holder.tvMonth.setText(month);

        boolean isSelected = (position == selectedIndex);

        holder.tvMonth.setSelected(isSelected);
        if (isSelected) {
            holder.tvMonth.setTextColor(Color.BLACK);
            holder.tvMonth.setTextSize(20);
            holder.tvMonth.setAlpha(1f);
            holder.tvMonth.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.tvMonth.setTextColor(Color.parseColor("#BBBBBB"));
            holder.tvMonth.setTextSize(18);
            holder.tvMonth.setAlpha(0.5f);
            holder.tvMonth.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedIndex;
            selectedIndex = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedIndex);
            if (listener != null) {
                listener.onMonthSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    public void setSelectedIndex(int index) {
        int previousSelected = selectedIndex;
        selectedIndex = index;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedIndex);
    }

    public interface OnMonthSelectedListener {
        void onMonthSelected(int monthIndex);
    }

    public static class MonthViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvMonth;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
}