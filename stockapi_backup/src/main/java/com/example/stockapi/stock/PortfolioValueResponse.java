package com.example.stockapi.stock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortfolioValueResponse {

    public String date;
    @JsonProperty("portfolio value")
    public float portfolioValue;

    public PortfolioValueResponse(float portfolioValue){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.date = today.format(formatter);
        this.portfolioValue = portfolioValue;
    }

}
