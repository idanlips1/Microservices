package com.example.stockapi.stock;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStockRepository extends MongoRepository<Stock, String> {

}
