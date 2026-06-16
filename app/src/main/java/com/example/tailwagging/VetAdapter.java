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

public class VetAdapter extends RecyclerView.Adapter<VetAdapter.VetViewHolder> {

    private final Context context;
    private final List<Vet> vets;

    public VetAdapter(Context context, List<Vet> vets) {
        this.context = context;
        this.vets = vets;
    }

    @NonNull
    @Override
    public VetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vet_horizontal, parent, false);
        return new VetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VetViewHolder holder, int position) {
        Vet vet = vets.get(position);
        holder.tvVetName.setText(vet.getName());
        holder.tvVetQualification.setText(vet.getQualification());
        holder.tvVetRating.setText(context.getString(R.string.rating_reviews_format, vet.getRating(), vet.getReviewsCount()));
        holder.tvVetTag.setText(vet.getTag());
        holder.tvVetDistance.setText(vet.getDistance());
        holder.tvVetPhone.setText(vet.getPhone() != null ? vet.getPhone() : "N/A");
        holder.tvVetExperience.setText(vet.getExperience());
        holder.tvLastVisit.setText(context.getString(R.string.last_visit_format, vet.getLastVisit()));

        if (vet.getImageUrl() != null && !vet.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(vet.getImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.ivVetProfile);
        } else {
            Glide.with(context)
                    .load(vet.getImageResId())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.ivVetProfile);
        }
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VetDetailsActivity.class);
            intent.putExtra("SELECTED_VET", vet);
            context.startActivity(intent);
        });
        
        holder.btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingActivity.class);
            intent.putExtra("VET_ID", vet.getId());
            intent.putExtra("VET_NAME", vet.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return vets.size();
    }

    public static class VetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVetProfile;
        TextView tvVetName, tvVetQualification, tvVetRating, tvVetTag, tvVetDistance, tvVetPhone, tvVetExperience, tvLastVisit;
        View btnBookAppointment;

        public VetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVetProfile = itemView.findViewById(R.id.ivVetProfile);
            tvVetName = itemView.findViewById(R.id.tvVetName);
            tvVetQualification = itemView.findViewById(R.id.tvVetQualification);
            tvVetRating = itemView.findViewById(R.id.tvVetRating);
            tvVetTag = itemView.findViewById(R.id.tvVetTag);
            tvVetDistance = itemView.findViewById(R.id.tvVetDistance);
            tvVetPhone = itemView.findViewById(R.id.tvVetPhone);
            tvVetExperience = itemView.findViewById(R.id.tvVetExperience);
            tvLastVisit = itemView.findViewById(R.id.tvLastVisit);
            btnBookAppointment = itemView.findViewById(R.id.btnBookAppointment);
        }
    }
}