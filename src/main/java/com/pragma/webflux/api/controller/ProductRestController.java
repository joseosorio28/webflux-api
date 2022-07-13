package com.pragma.webflux.api.controller;


import com.pragma.webflux.api.model.Product;
import com.pragma.webflux.api.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ProductRestController.class);

    @Autowired
    private IProductService productService;

    @GetMapping
    public Flux<Product> index() {
        return productService.findAllWithNameUppercase()
                .doOnNext(product -> LOG.info(product.getName()));
    }

    @GetMapping("/{id}")
    public Mono<Product> show(@PathVariable String id) {
        return productService.findById(id);
    }
}
