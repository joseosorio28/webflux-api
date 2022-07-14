package com.pragma.webflux.api.service;


import com.pragma.webflux.api.model.Category;
import com.pragma.webflux.api.model.Image;
import com.pragma.webflux.api.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductService {

    Flux<Product> findAll();
    Flux<Product> findAllWithNameUppercase();
    Flux<Product> findAllWithNameUppercaseRepeat();
    Mono<Product> findById(String id);
    Mono<Product> save(Product product);
    Mono<Void> delete(Product product);

    Flux<Category> findAllCategories();
    Mono<Category> findCategoryById(String id);
    Mono<Category> saveCategory(Category category);

    Flux<Image> findAllImages();
    Mono<Image> findImageById(String id);
    Mono<Image> saveImage(Image image);

    Mono<Product> findByName(String name);
    Mono<Category> findCategoryByName(String name);

}
