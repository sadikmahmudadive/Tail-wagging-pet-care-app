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
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final List<CartItem> items;
    private final OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged(int position, int newQuantity);
        void onRemove(int position);
    }

    public CartAdapter(List<CartItem> items, OnCartActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.productName);
        holder.tvDesc.setText(item.description != null ? item.description : "");
        
        String priceText = String.format(Locale.getDefault(), "Tk %.2f x %d", item.price, item.quantity);
        holder.tvPrice.setText(priceText);
        holder.tvQuantity.setText(String.valueOf(item.quantity));

        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.pet1)
                .into(holder.ivProduct);

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) listener.onQuantityChanged(position, item.quantity + 1);
        });
        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null && item.quantity > 1) {
                listener.onQuantityChanged(position, item.quantity - 1);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                holder.btnRemove.setVisibility(holder.btnRemove.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
            return true;
        });

        holder.btnRemove.setOnClickListener(v -> {
            UiUtils.animateClick(v);
            v.postDelayed(() -> {
                if (listener != null) listener.onRemove(position);
            }, 250);
        });
        
        if (listener == null) {
            holder.btnPlus.setVisibility(View.GONE);
            holder.btnMinus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvDesc;
        ImageButton btnPlus, btnMinus;
        View btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivCartProduct);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvDesc = itemView.findViewById(R.id.tvCartProductDesc);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
            btnRemove = itemView.findViewById(R.id.btnRemoveFromCart);
        }
    }
}
