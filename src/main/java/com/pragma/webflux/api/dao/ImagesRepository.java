package com.pragma.webflux.api.dao;


import com.pragma.webflux.api.model.Image;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagesRepository extends ReactiveMongoRepository<Image,String> {
}
