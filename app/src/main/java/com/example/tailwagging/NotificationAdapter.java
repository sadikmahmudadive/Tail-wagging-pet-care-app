package com.example.tailwagging;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final Context context;
    private final List<NotificationItem> notifications;

    public NotificationAdapter(Context context, List<NotificationItem> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationItem item = notifications.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);
        holder.tvTime.setText(item.timestamp);

        if (item.isRead) {
            holder.unreadIndicator.setVisibility(View.INVISIBLE);
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View unreadIndicator;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            unreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }
    }
}