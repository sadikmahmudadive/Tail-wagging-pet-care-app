package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServiceRecordAdapter extends RecyclerView.Adapter<ServiceRecordAdapter.RecordViewHolder> {

    private final List<ServiceRecord> records;

    public ServiceRecordAdapter(List<ServiceRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        ServiceRecord record = records.get(position);
        holder.tvTitle.setText(record.title);
        holder.tvDate.setText(record.date);
        holder.tvProvider.setText(String.format("By: %s", record.providerName));
        holder.tvDescription.setText(record.description);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvProvider, tvDescription;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecordTitle);
            tvDate = itemView.findViewById(R.id.tvRecordDate);
            tvProvider = itemView.findViewById(R.id.tvRecordProvider);
            tvDescription = itemView.findViewById(R.id.tvRecordDescription);
        }
    }
}