package com.example.tailwagging;

import android.content.Intent;
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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final List<Order> orders;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault());

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvDate.setText(sdf.format(new Date(order.timestamp)));
        holder.tvStatus.setText(order.status != null ? order.status.toUpperCase() : "PENDING");
        holder.tvCustomer.setText("Customer: " + (order.userName != null ? order.userName : "Unknown"));
        
        StringBuilder itemsText = new StringBuilder();
        if (order.items != null) {
            for (int i = 0; i < order.items.size(); i++) {
                CartItem item = order.items.get(i);
                itemsText.append(item.quantity).append("x ").append(item.productName);
                if (i < order.items.size() - 1) itemsText.append(", ");
            }
        }
        holder.tvItems.setText(itemsText.toString());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "Tk %.2f", order.total));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrderDetailsActivity.class);
            intent.putExtra("SELECTED_ORDER", order);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvCustomer, tvItems, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            tvItems = itemView.findViewById(R.id.tvOrderItems);
            tvAmount = itemView.findViewById(R.id.tvOrderAmount);
        }
    }
}
