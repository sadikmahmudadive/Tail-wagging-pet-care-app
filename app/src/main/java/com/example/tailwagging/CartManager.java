package com.example.tailwagging;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.productId.equals(product.id)) {
                item.quantity += quantity;
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return cartItems;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
        }
    }

    public void updateQuantity(int position, int newQuantity) {
        if (position >= 0 && position < cartItems.size()) {
            if (newQuantity <= 0) {
                cartItems.remove(position);
            } else {
                cartItems.get(position).quantity = newQuantity;
            }
        }
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.price * item.quantity;
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
