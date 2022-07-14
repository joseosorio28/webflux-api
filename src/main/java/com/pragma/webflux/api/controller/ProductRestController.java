package com.pragma.webflux.api.controller;

import com.pragma.webflux.api.model.Image;
import com.pragma.webflux.api.model.Product;
import com.pragma.webflux.api.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Value("${images.upload.path}")
    private String imagesPath;

    @Autowired
    private IProductService productService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Product>>> listProducts() {
        return Mono.just(
                ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProduct(
            @PathVariable String id
    ) {
        return productService.findById(id)
                .map(p -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity
                        .notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createProduct(
            @Valid @RequestBody Mono<Product> monoProduct
    ) {
        Map<String, Object> response = new HashMap<>();
        return monoProduct
                .flatMap(product -> {
                    if (product.getCreateAt() == null) {
                        product.setCreateAt(new Date());
                    }
                    return productService.save(product)
                            .map(p -> {
                                response.put("Product", p);
                                return ResponseEntity
                                        .created(URI.create("/api/products/" + p.getId()))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(response);
                            });
                })
                .onErrorResume(t ->
                        Mono.just(t).cast(WebExchangeBindException.class)
                                .flatMap(e -> Mono.just(e.getFieldErrors()))
                                .flatMapMany(Flux::fromIterable)
                                .map(field -> "Field: '" + field.getField() + "' " + field.getDefaultMessage())
                                .collectList()
                                .flatMap(list -> {
                                            response.put("errors", list);
                                            return Mono.just(
                                                    ResponseEntity
                                                            .badRequest()
                                                            .body(response));
                                        }
                                )
                );

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> editProduct(
            @PathVariable String id,
            @RequestBody Product product
    ) {
        return productService.findById(id)
                .flatMap(p -> {
                    p.setName(product.getName());
                    p.setPrice(product.getPrice());
                    p.setCategory(product.getCategory());
                    return productService.save(p);
                })
                .map(p -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(p))
                .defaultIfEmpty(ResponseEntity
                        .notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(
            @PathVariable String id
    ) {
        return productService.findById(id)
                .flatMap(p -> productService.delete(p)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Product>> upload(
            @PathVariable String id,
            @RequestPart FilePart file
    ) {
        String filename = UUID.randomUUID() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", "");
        return productService.findById(id)
                .flatMap(p -> {
                    if (!file.filename().isEmpty()) {
                        Image image = new Image(filename);
                        p.getImages().add(image);
                    }
                    return file.transferTo(new File(imagesPath + filename))
                            .then(productService.save(p));
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity
                        .notFound().build());
    }
}
