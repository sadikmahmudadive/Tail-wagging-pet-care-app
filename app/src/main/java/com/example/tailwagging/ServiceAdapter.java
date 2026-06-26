package com.example.tailwagging;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private final Context context;
    private final List<Vet> services;

    public ServiceAdapter(Context context, List<Vet> services) {
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service_card, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Vet service = services.get(position);
        holder.tvName.setText(service.getName());
        holder.tvRating.setText(String.format(Locale.getDefault(), "%.1f {%d reviews}", service.getRating(), service.getReviewsCount()));
        holder.tvDistance.setText(service.getDistance());
        holder.tvPhone.setText(service.getPhone() != null ? service.getPhone() : "N/A");
        holder.tvHours.setText(service.getBusinessHours());
        
        holder.ivVerified.setVisibility(service.isVerified() ? View.VISIBLE : View.GONE);
        
        // Randomly set OPEN/CLOSED for display purposes if not in data
        boolean isOpen = position % 2 == 0;
        holder.tvStatus.setText(isOpen ? "OPEN" : "CLOSED");
        holder.tvStatus.setTextColor(isOpen ? 0xFF66BB6A : 0xFFE57373);

        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Glide.with(context).load(service.getImageUrl()).placeholder(R.drawable.ic_profile).into(holder.ivLogo);
        } else {
            Glide.with(context).load(service.getImageResId()).into(holder.ivLogo);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VetDetailsActivity.class);
            intent.putExtra("SELECTED_VET", service);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo, ivVerified;
        TextView tvName, tvRating, tvStatus, tvDistance, tvPhone, tvHours;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivServiceLogo);
            ivVerified = itemView.findViewById(R.id.ivVerifiedBadge);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvRating = itemView.findViewById(R.id.tvServiceRating);
            tvStatus = itemView.findViewById(R.id.tvServiceStatus);
            tvDistance = itemView.findViewById(R.id.tvServiceDistance);
            tvPhone = itemView.findViewById(R.id.tvServicePhone);
            tvHours = itemView.findViewById(R.id.tvServiceHours);
        }
    }
}