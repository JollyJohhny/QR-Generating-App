package com.example.qrgeneratingapp;

public class Product {

    private String Name,Price,Details,ManufactureId,TimeStamp,ExpiryDate;
    public Product(){

    }


    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        ExpiryDate = expiryDate;
    }

    public Product(String name, String price, String details, String manufacturerid, String timestamp, String expiry) {
        Name = name;
        Price = price;
        Details = details;
        ManufactureId = manufacturerid;
        TimeStamp = timestamp;
        ExpiryDate = expiry;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public String getManufactureId() {
        return ManufactureId;
    }

    public void setManufactureId(String manufactureId) {
        ManufactureId = manufactureId;
    }
}
