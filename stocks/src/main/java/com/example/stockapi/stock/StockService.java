package com.example.stockapi.stock;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class StockService {
    //private final Map<String, Stock> stockMap = new HashMap<>();

    private final MongoTemplate mongoTemplate;

    @Value("${mongo.collection}") // Inject collection name from properties/env
    private String collectionName;
    private final RestTemplate restTemplate = new RestTemplate();
    /// this is the error , we removed the if else here the api key is from the docker file
    private final String API_KEY = "GSBDSCF31HehCSxIozHtjw==CY8yGhcKCEU3ZNgG";
    public Integer Counter = 0;

    @Autowired
    public StockService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        restTemplate.setErrorHandler(new CustomErrorHandler());
    }
    // Add a new s  stock
    public String addToMap(Stock stock) {
        // Validate the input stock object
        if (stock.getName() == null || stock.getName().isBlank()) {
            stock.setName("NA");
        }
        if (stock.getPurchaseDate() == null || stock.getPurchaseDate().isBlank()) {
            stock.setPurchaseDate("NA");
        }

        Stock savedStock = mongoTemplate.insert(stock, collectionName);
        return savedStock.getId();

    }

    // Retrieve a stock by a ID
    public Stock getStock(String id) {
        return mongoTemplate.findById(id, Stock.class,collectionName);
    }

    // Retrieve all stocks
    public List<Stock> getAllStocks() {
        return mongoTemplate.findAll(Stock.class, collectionName);
    }

    public List<Stock> getAllStocksBothServices() {
        List<Stock> stocks= mongoTemplate.findAll(Stock.class, "stocks1");
        stocks.addAll( mongoTemplate.findAll(Stock.class, "stocks2"));
        return stocks;
    }

    public void deleteById(String id) {
        Stock stock = mongoTemplate.findById(id, Stock.class, collectionName);
        if (stock != null) {
            mongoTemplate.remove(stock, collectionName);
        }
    }

    public void updateStock(Stock stock) {
//        Stock updatedStock = stockMap.get(stock.getId());
//        updatedStock.setName(stock.getName());
//        updatedStock.setPurchaseDate(stock.getPurchaseDate());
//        updatedStock.setShares(stock.getShares());
//        updatedStock.setStockPrice(stock.getPrice());
//        updatedStock.setSymbol(stock.getSymbol());
//        stockMap.put(stock.getId(), updatedStock);
        mongoTemplate.save(stock, collectionName);
    }

    public StockValueResponse getStockValue(String id)throws Exception {
        Stock stock = mongoTemplate.findById(id, Stock.class, collectionName);
        if (stock == null) {
            return null;
        }

        String apiURL = "https://api.api-ninjas.com/v1/stockprice?ticker=" + stock.getSymbol();

        StockValueResponse result = new StockValueResponse();


            var headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Api-Key", API_KEY);

            var entity = new org.springframework.http.HttpEntity<>(headers);
            var response = restTemplate.exchange(apiURL, org.springframework.http.HttpMethod.GET, entity, String.class);

            String responseBody = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseBody);



            double ticker = Double.parseDouble(node.get("price").toString());
            double stockValue = ticker * stock.getShares();

            result.setTicker((float) ticker);
            result.setSymbol(stock.getSymbol());
            result.setStockValue((float) stockValue);




        return result;
    }

    private float getPortfolioValue() throws Exception{
        float value = 0;
        List <Stock> result = getAllStocks();
        for (Stock stock : result){
            value += getStockValue(stock.getId()).getStockValue();
        }

        return value;
    }

    public PortfolioValueResponse getPortfolio() throws Exception{
        return new PortfolioValueResponse(getPortfolioValue());
    }

}
