package com.example.stockapi.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stocks")
public class Stock {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("purchase price")
    private float price;

    @JsonProperty("purchase date")
    private String purchaseDate;

    @JsonProperty("shares")
    private int shares;

    public Stock(String id, String name, String symbol, float price, String purchaseDate, int shares) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.symbol = symbol;
        this.purchaseDate = purchaseDate;
        this.shares = shares;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public float getPrice() {
        return this.price;
    }

    public String getPurchaseDate() {
        return this.purchaseDate;
    }

    public int getShares() {
        return this.shares;
    }

    public void setId(Integer counter) {
        this.id = counter.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setStockPrice(float price) {
        this.price = price;
    }

    public void setPurchaseDate(String date) {
        this.purchaseDate = date;
    }

    public void setShares(int numOfShares) {
        this.shares = numOfShares;
    }
    @JsonIgnore
    public boolean isStockValid() {
        return
                isNotEmpty(this.getId())
                && isNotEmpty(this.getName())
                && isNotEmpty(this.getSymbol())
                && this.getPrice() > 0 // Assuming price should be greater than 0
                && isNotEmpty(this.getPurchaseDate())
                && this.getShares() >= 0; // Assuming shares should be greater than 0
    }
    @JsonIgnore
    public boolean isPostValid() {
        return
                 isNotEmpty(this.getSymbol())
                && this.getPrice() > 0 // Assuming price should be greater than 0
                && this.getShares() >= 0; // Assuming shares should be greater than 0
    }

    // Helper method to check if a String is null or empty
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    

    @Override
    public String toString() {
        return "Stock{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", purchasePrice=" + price +
                ", purchaseDate='" + purchaseDate + '\'' +
                ", shares=" + shares +
                '}';
    }

}
