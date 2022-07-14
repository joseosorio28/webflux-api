package com.pragma.webflux.api.dao;


import com.pragma.webflux.api.model.Product;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveMongoRepository<Product,String> {


    Mono<Product> findByName(String name);

    @Query("{'name':?0}")
    Mono<Product> getProduct(String name);

}
