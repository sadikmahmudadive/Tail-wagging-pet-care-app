package com.example.tailwagging;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserLogAdapter extends RecyclerView.Adapter<UserLogAdapter.ViewHolder> {

    private final List<UserLog> logs;
    private final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

    public UserLogAdapter(List<UserLog> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserLog log = logs.get(position);
        holder.tvUser.setText(log.userName != null ? log.userName : "Anonymous");
        holder.tvTime.setText(sdf.format(new Date(log.timestamp)));
        holder.tvAction.setText(log.action);
        holder.tvDetail.setText(log.detail);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvTime, tvAction, tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvLogUser);
            tvTime = itemView.findViewById(R.id.tvLogTime);
            tvAction = itemView.findViewById(R.id.tvLogAction);
            tvDetail = itemView.findViewById(R.id.tvLogDetail);
        }
    }
}
