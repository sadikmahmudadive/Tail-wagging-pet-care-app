package com.example.tailwagging;

import java.io.Serializable;

public class Product implements Serializable {
    public String id;
    public String shopId;
    public String name;
    public String category;
    public double price;
    public int stockQuantity;
    public String imageUrl;
    public String description;
    public String brand;
    public int soldCount;

    public Product() {}

    public Product(String id, String shopId, String name, String category, double price, int stockQuantity, String imageUrl, String description, String brand) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.description = description;
        this.brand = brand;
        this.soldCount = 0;
    }
}
