package com.pragma.webflux.api.service;


import com.pragma.webflux.api.dao.CategoryRepository;
import com.pragma.webflux.api.dao.ImagesRepository;
import com.pragma.webflux.api.dao.ProductRepository;
import com.pragma.webflux.api.model.Category;
import com.pragma.webflux.api.model.Image;
import com.pragma.webflux.api.model.Product;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements IProductService{

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ImagesRepository imagesRepository;

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Flux<Product> findAllWithNameUppercase() {
        return productRepository.findAll()
                .map(product->{
                    product.setName(product.getName().toUpperCase());
                    return product;
                });
    }

    @Override
    public Flux<Product> findAllWithNameUppercaseRepeat() {
        return findAllWithNameUppercase().repeat(5000);
    }

    @Override
    public Mono<Product> findById(String id) {
        return productRepository.findAll()
                .filter(product -> product.getId().equals(id))
                .next()
                .map(product->{
                    product.setName(product.getName().toUpperCase());
                    return product;
                })
                .doOnNext(product -> LOG.info(product.getName()));
    }

    @Override
    public Mono<Product> save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Void> delete(Product product) {
        return productRepository.delete(product);
    }

    @Override
    public Flux<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Mono<Category> findCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Mono<Category> saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Flux<Image> findAllImages() {
        return imagesRepository.findAll();
    }

    @Override
    public Mono<Image> findImageById(String id) {
        return imagesRepository.findById(id);
    }

    @Override
    public Mono<Image> saveImage(Image image) {
        return imagesRepository.save(image);
    }

    @Override
    public Mono<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public Mono<Category> findCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
}
