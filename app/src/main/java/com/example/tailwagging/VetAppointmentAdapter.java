package com.example.tailwagging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class VetAppointmentAdapter extends RecyclerView.Adapter<VetAppointmentAdapter.ApptViewHolder> {

    private final Context context;
    private final List<VetDashboardActivity.MapAppointment> appointments;
    private final DatabaseReference dbRef;

    public VetAppointmentAdapter(Context context, List<VetDashboardActivity.MapAppointment> appointments) {
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
        VetDashboardActivity.MapAppointment appt = appointments.get(position);
        holder.tvPetName.setText(appt.petName);
        holder.tvDate.setText(appt.date);
        holder.tvTime.setText(appt.time);
        
        String status = appt.status != null ? appt.status.toUpperCase() : "PENDING";
        holder.tvStatus.setText(status);
        
        if ("COMPLETED".equals(status)) {
            holder.btnComplete.setVisibility(View.GONE);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_rounded_gray); // Could use a green one
        } else {
            holder.btnComplete.setVisibility(View.VISIBLE);
        }

        holder.tvOwnerName.setText(String.format("Owner: %s", appt.userId != null ? "Client " + appt.userId.substring(0, 4) : "User"));

        holder.btnComplete.setOnClickListener(v -> {
            dbRef.child("appointments").child(appt.id).child("status").setValue("Completed")
                    .addOnSuccessListener(aVoid -> {
                        appt.status = "Completed";
                        notifyItemChanged(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ApptViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetName, tvOwnerName, tvDate, tvTime, tvStatus;
        View btnComplete;

        public ApptViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetName = itemView.findViewById(R.id.tvApptPetName);
            tvOwnerName = itemView.findViewById(R.id.tvApptOwnerName);
            tvDate = itemView.findViewById(R.id.tvApptDate);
            tvTime = itemView.findViewById(R.id.tvApptTime);
            tvStatus = itemView.findViewById(R.id.tvApptStatus);
            btnComplete = itemView.findViewById(R.id.btnCompleteAppt);
        }
    }
}