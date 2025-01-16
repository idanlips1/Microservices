package com.example.stockapi.stock;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final MongoTemplate mongoTemplate;

    @Value("${mongo.collection}")
    private String collectionName;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_KEY = "GSBDSCF31HehCSxIozHtjw==CY8yGhcKCEU3ZNgG";
    public Integer Counter = 0;

    @Autowired
    public StockService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        restTemplate.setErrorHandler(new CustomErrorHandler());
    }

    public String addToMap(Stock stock) {
        if (stock.getName() == null || stock.getName().isBlank()) {
            stock.setName("NA");
        }
        if (stock.getPurchaseDate() == null || stock.getPurchaseDate().isBlank()) {
            stock.setPurchaseDate("NA");
        }

        // Check if stock with same symbol exists using atomic operation
        Query query = new Query(Criteria.where("symbol").is(stock.getSymbol()));
        if (mongoTemplate.exists(query, Stock.class, collectionName)) {
            throw new IllegalArgumentException("Stock with symbol " + stock.getSymbol() + " already exists");
        }

        Stock savedStock = mongoTemplate.insert(stock, collectionName);
        return savedStock.getId();
    }

    public Stock getStock(String id) {
        return mongoTemplate.findById(id, Stock.class, collectionName);
    }

    public List<Stock> getAllStocks() {
        return mongoTemplate.findAll(Stock.class, collectionName);
    }

    public void deleteById(String id) {
        // Use findAndRemove for atomic delete operation
        Query query = new Query(Criteria.where("_id").is(id));
        Stock deletedStock = mongoTemplate.findAndRemove(query, Stock.class, collectionName);
        
        if (deletedStock != null) {
            logger.info("Successfully deleted stock with id: {}", id);
        }
    }

    public void updateStock(Stock stock) {
        // Use findAndModify for atomic update operation
        Query query = new Query(Criteria.where("_id").is(stock.getId()));
        Update update = new Update()
            .set("name", stock.getName())
            .set("symbol", stock.getSymbol())
            .set("purchase price", stock.getPrice())
            .set("purchase date", stock.getPurchaseDate())
            .set("shares", stock.getShares());

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(false);
        
        Stock updatedStock = mongoTemplate.findAndModify(query, update, options, Stock.class, collectionName);
        
        if (updatedStock == null) {
            throw new IllegalArgumentException("Stock not found with id: " + stock.getId());
        }
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
