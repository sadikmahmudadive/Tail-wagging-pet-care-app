package com.example.tailwagging;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    public String orderId;
    public String userId;
    public String userName;
    public String address;
    public String phone;
    public String paymentMethod;
    public double subtotal;
    public double shippingCharges;
    public double total;
    public long timestamp;
    public String status; // Pending, Processing, Shipped, Delivered, Cancelled
    public List<CartItem> items;

    public Order() {}

    public Order(String orderId, String userId, String userName, String address, String phone, 
                 String paymentMethod, double subtotal, double shippingCharges, double total, 
                 List<CartItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.address = address;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.shippingCharges = shippingCharges;
        this.total = total;
        this.items = items;
        this.timestamp = System.currentTimeMillis();
        this.status = "Pending";
    }
}
