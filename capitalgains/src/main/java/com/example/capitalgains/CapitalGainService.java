package com.example.capitalgains;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CapitalGainService {
    private static final Logger logger = LoggerFactory.getLogger(CapitalGainService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${stocks1.url}")
    private String stocks1Url;
    
    @Value("${stocks2.url}")
    private String stocks2Url;

    public CapitalGainService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public float calculateCapitalGains(String portfolio, Integer numSharesGt, Integer numSharesLt) throws Exception {
        List<Map<String, Object>> stocks = new ArrayList<>();
        
        try {
            logger.info("Calculating capital gains for portfolio: {}", portfolio);
            // Determine which stock service to query based on portfolio parameter
            if (portfolio == null) {
                logger.info("Fetching stocks from both services");
                List<Map<String, Object>> stocks1 = restTemplate.getForObject(stocks1Url + "/stocks", List.class);
                List<Map<String, Object>> stocks2 = restTemplate.getForObject(stocks2Url + "/stocks", List.class);
                if (stocks1 != null) stocks.addAll(stocks1);
                if (stocks2 != null) stocks.addAll(stocks2);
            } else if ("stocks1".equalsIgnoreCase(portfolio)) {
                logger.info("Fetching stocks from stocks1 service");
                List<Map<String, Object>> stocks1 = restTemplate.getForObject(stocks1Url + "/stocks", List.class);
                if (stocks1 != null) stocks.addAll(stocks1);
            } else if ("stocks2".equalsIgnoreCase(portfolio)) {
                logger.info("Fetching stocks from stocks2 service");
                List<Map<String, Object>> stocks2 = restTemplate.getForObject(stocks2Url + "/stocks", List.class);
                if (stocks2 != null) stocks.addAll(stocks2);
            } else {
                throw new IllegalArgumentException("Invalid portfolio specified. Must be 'stocks1' or 'stocks2'");
            }

            logger.info("Retrieved {} stocks before filtering", stocks.size());

            // Filter stocks based on number of shares criteria
            stocks = stocks.stream()
                .filter(stock -> stock != null && stock.get("shares") != null)
                .filter(stock -> numSharesGt == null || ((Number) stock.get("shares")).intValue() > numSharesGt)
                .filter(stock -> numSharesLt == null || ((Number) stock.get("shares")).intValue() < numSharesLt)
                .collect(Collectors.toList());

            logger.info("After filtering: {} stocks match criteria", stocks.size());

            // Calculate total capital gains
            float totalGains = 0;
            for (Map<String, Object> stock : stocks) {
                float gain = calculateStockGain(stock);
                logger.info("Stock {} gain: {}", stock.get("symbol"), gain);
                totalGains += gain;
            }
            
            logger.info("Total capital gains: {}", totalGains);
            return totalGains;
        } catch (Exception e) {
            logger.error("Error calculating capital gains", e);
            throw new RuntimeException("Error calculating capital gains: " + e.getMessage(), e);
        }
    }

    private float calculateStockGain(Map<String, Object> stock) throws Exception {
        if (stock == null || !stock.containsKey("purchase price") || !stock.containsKey("shares") || !stock.containsKey("id")) {
            logger.error("Invalid stock data: {}", stock);
            throw new IllegalArgumentException("Invalid stock data");
        }

        float purchasePrice = ((Number) stock.get("purchase price")).floatValue();
        int shares = ((Number) stock.get("shares")).intValue();
        String stockId = stock.get("id").toString();
        
        logger.info("Calculating gain for stock ID: {}, Purchase price: {}, Shares: {}", stockId, purchasePrice, shares);
        
        // Try stocks1 first, if fails try stocks2
        float currentPrice;
        try {
            currentPrice = getCurrentPrice(stocks1Url, stockId);
        } catch (Exception e) {
            logger.warn("Failed to get price from stocks1, trying stocks2", e);
            try {
                currentPrice = getCurrentPrice(stocks2Url, stockId);
            } catch (Exception e2) {
                logger.error("Failed to get price from both services", e2);
                throw new RuntimeException("Could not get current price for stock " + stockId);
            }
        }
        
        float gain = (currentPrice - purchasePrice) * shares;
        logger.info("Current price: {}, Gain: {}", currentPrice, gain);
        return gain;
    }

    private float getCurrentPrice(String serviceUrl, String stockId) throws Exception {
        logger.info("Getting current price from {}/stock-value/{}", serviceUrl, stockId);
        ResponseEntity<String> response = restTemplate.exchange(
            serviceUrl + "/stock-value/" + stockId,
            HttpMethod.GET,
            HttpEntity.EMPTY,
            String.class
        );
        
        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from stock service");
        }
        
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode stockValue = jsonNode.get("stock value");
        if (stockValue == null) {
            logger.error("Invalid response format: {}", response.getBody());
            throw new RuntimeException("Invalid response format from stock service");
        }
        
        return stockValue.floatValue();
    }
}
