package com.example.stockapi.stock;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StockValueResponse {
    private String symbol;
    private float ticker;
    @JsonProperty("stock value")
    private float stockValue;


    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public float getTicker() {
        return ticker;
    }

    public void setTicker(float ticker) {
        this.ticker = ticker;
    }

    public float getStockValue() {
        return stockValue;

    }

    public void setStockValue(float stockValue) {
        this.stockValue = stockValue;
    }
}
