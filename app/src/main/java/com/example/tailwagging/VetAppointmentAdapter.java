package com.example.tailwagging;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class VetAppointmentAdapter extends RecyclerView.Adapter<VetAppointmentAdapter.ApptViewHolder> {

    private final Context context;
    private final List<Appointment> appointments;
    private final DatabaseReference dbRef;

    public VetAppointmentAdapter(Context context, List<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
        this.dbRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference();
    }

    @NonNull
    @Override
    public ApptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vet_appointment, parent, false);
        return new ApptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApptViewHolder holder, int position) {
        Appointment appt = appointments.get(position);
        holder.tvPetName.setText(appt.petName);
        holder.tvDate.setText(appt.date);
        holder.tvTime.setText(appt.time);
        
        String status = appt.status != null ? appt.status.toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);
        
        if ("COMPLETED".equals(status)) {
            holder.btnComplete.setVisibility(View.GONE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_rounded_gray);
        } else {
            holder.btnComplete.setVisibility(View.VISIBLE);
        }

        // Display Owner Name
        if (appt.ownerName != null && !appt.ownerName.isEmpty()) {
            holder.tvOwnerName.setText(String.format("Owner: %s", appt.ownerName));
        } else {
            holder.tvOwnerName.setText(String.format("Owner: %s", appt.userId != null ? "User " + appt.userId.substring(0, 4) : "Unknown"));
        }

        // Load Pet Image
        Glide.with(context)
                .load(appt.petImageUrl)
                .placeholder(R.drawable.ic_pet_placeholder)
                .error(R.drawable.ic_pet_placeholder)
                .into(holder.ivPetAppt);

        holder.btnComplete.setOnClickListener(v -> {
            dbRef.child("appointments").child(appt.id).child("status").setValue("Completed")
                    .addOnSuccessListener(aVoid -> {
                        appt.status = "Completed";
                        notifyItemChanged(position);
                    });
        });

        holder.btnCall.setOnClickListener(v -> {
            if (appt.userId != null) {
                dbRef.child("users").child(appt.userId).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String phone = snapshot.getValue(String.class);
                        if (phone != null && !phone.isEmpty()) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + phone));
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "No phone number found for this owner", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ClientListActivity) {
                // Fetch full pet object and open details
                DatabaseReference petRef = FirebaseDatabase.getInstance("https://tail-wagging-d03de-default-rtdb.firebaseio.com/").getReference("pets").child(appt.petId);
                petRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Pet pet = snapshot.getValue(Pet.class);
                        if (pet != null) {
                            Intent intent = new Intent(context, PetDetailsActivity.class);
                            intent.putExtra("SELECTED_PET", pet);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Pet profile not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ApptViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetName, tvOwnerName, tvDate, tvTime, tvStatus;
        ImageView ivPetAppt;
        View btnComplete, btnCall;

        public ApptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.tvApptPetName);
            tvOwnerName = itemView.findViewById(R.id.tvApptOwnerName);
            tvDate = itemView.findViewById(R.id.tvApptDate);
            tvTime = itemView.findViewById(R.id.tvApptTime);
            tvStatus = itemView.findViewById(R.id.tvApptStatus);
            ivPetAppt = itemView.findViewById(R.id.ivPetAppt);
            btnComplete = itemView.findViewById(R.id.btnCompleteAppt);
            btnCall = itemView.findViewById(R.id.btnCallOwner);
        }
    }
}