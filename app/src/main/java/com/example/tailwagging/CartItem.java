package com.example.tailwagging;

import java.io.Serializable;

public class CartItem implements Serializable {
    public String productId;
    public String productName;
    public String brand;
    public String description;
    public double price;
    public int quantity;
    public String imageUrl;
    public String shopId;

    public CartItem() {}

    public CartItem(Product product, int quantity) {
        this.productId = product.id;
        this.productName = product.name;
        this.brand = product.brand;
        this.description = product.description;
        this.price = product.price;
        this.quantity = quantity;
        this.imageUrl = product.imageUrl;
        this.shopId = product.shopId;
    }
}
