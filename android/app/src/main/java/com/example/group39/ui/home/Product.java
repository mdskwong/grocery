package com.example.group39.ui.home;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class Product {
    @PrimaryKey(autoGenerate = true)
    public Integer recordId;

    public String date;
    public String location;
    public String shopName;
    public String categoryName;
    public String brandName;
    public String productName;
    public String description;
    public double price;
    public String currency;
    public String photoUri;  // if storing photo path or URI
    public String remarks;

    // Constructor, getters, and setters can be added here
}
