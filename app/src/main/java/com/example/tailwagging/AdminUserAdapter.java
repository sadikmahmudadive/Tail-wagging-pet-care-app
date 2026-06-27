package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private final List<AdminUser> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDelete(AdminUser user);
        void onToggleVerify(AdminUser user);
        void onManageShop(AdminUser user);
    }

    public AdminUserAdapter(List<AdminUser> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = users.get(position);
        holder.tvName.setText(user.name);
        holder.tvEmail.setText(user.email);
        
        holder.tvRole.setText(user.role);
        if (user.isVerified) {
            holder.tvRole.setTextColor(holder.itemView.getContext().getColor(R.color.health_green));
            holder.ivVerifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvRole.setTextColor(holder.itemView.getContext().getColor(R.color.dark_blue));
            holder.ivVerifiedBadge.setVisibility(View.GONE);
        }

        if ("Pet Shop".equalsIgnoreCase(user.role)) {
            holder.btnManageShop.setVisibility(View.VISIBLE);
            holder.btnManageShop.setOnClickListener(v -> listener.onManageShop(user));
        } else {
            holder.btnManageShop.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(holder.ivPhoto);

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
        
        // Long click on role to toggle verify
        holder.tvRole.setOnClickListener(v -> listener.onToggleVerify(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivPhoto;
        TextView tvName, tvEmail, tvRole;
        ImageView ivVerifiedBadge;
        ImageButton btnDelete, btnManageShop;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivUserPhoto);
            ivVerifiedBadge = itemView.findViewById(R.id.ivVerifiedBadge);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            btnManageShop = itemView.findViewById(R.id.btnManageShopItems);
        }
    }
}
