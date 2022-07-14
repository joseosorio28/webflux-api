package com.pragma.webflux.api;

import com.pragma.webflux.api.model.Product;
import com.pragma.webflux.api.service.IProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringWebfluxApiRestApplicationMockTests {

    @Autowired
    private WebTestClient client;
    @Autowired
    private IProductService productService;
    @Value("${api.endpoint}")
    private String path;
    private static final String PATH_VARIABLE="{id}";

    @Test
    void canListAllProducts() {
        client
                .get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Product.class)
                .consumeWith(
                        response -> {
                            List<Product> products = response.getResponseBody();
                            assertNotNull(products);
                            products.forEach(p ->
                                    System.out.println(p.getName()));
                            assertTrue(products.size() > 0);
                        }
                )

        ;
    }

    @Test
    void canGetProduct() {
        String name = "TV Panasonic Pantalla LCD";
        Product p = productService.findByName(name).block();

        assert p != null;
        client
                .get()
                .uri(path + PATH_VARIABLE, Collections.singletonMap("id", p.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(p.getId())
                .jsonPath("$.name").isEqualTo(name.toUpperCase());

    }

    @Test
    void canCreateProduct() {
        Mono<Product> p = Mono.just(
                new Product("TV Panasonic Led 65 inch",
                        200.0,
                        productService.findCategoryByName("Electronics").block()
                ));
        client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(p, Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo(Objects.requireNonNull(p.block()).getName())
                .jsonPath("$.category.name").isEqualTo(Objects.requireNonNull(p.block()).getCategory().getName());

    }

    @Test
    void canCreateProductTest2() {
        Product p = new Product(
                "TV Panasonic Led 65 inch",
                200.0,
                productService.findCategoryByName("Electronics").block()
        );
        client
                .post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(p), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Product.class)
                .consumeWith(
                        response -> {
                            Product product = response.getResponseBody();
                            assert product != null;
                            assertEquals(p.getName(), product.getName());
                            assertEquals(p.getPrice(), product.getPrice());
                            assertEquals(p.getCategory().getName(), product.getCategory().getName());
                        }
                )
        ;

    }

    @Test
    void canUpdateProduct() {
        String name = "TV Panasonic Pantalla LCD";
        Product actualProduct = productService.findByName(name).block();
        Product updatedProduct = new Product(
                "Asus Notebook",
                1000.0,
                productService.findCategoryByName("Computing").block()
        );
        assert actualProduct != null;
        client
                .put()
                .uri(path + PATH_VARIABLE, Collections.singletonMap("id", actualProduct.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedProduct), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Product.class)
                .consumeWith(
                        response -> {
                            Product product = response.getResponseBody();
                            assert product != null;
                            assertEquals(actualProduct.getId(), product.getId());
                            assertEquals(updatedProduct.getName(), product.getName());
                            assertEquals(updatedProduct.getPrice(), product.getPrice());
                            assertEquals(updatedProduct.getCategory().getName(), product.getCategory().getName());
                        }
                );
    }

    @Test
    void canDeleteProduct() {
        String name = "HP Notebook Omen 17";
        Product p = productService.findByName(name).block();

        assert p != null;
        client
                .delete()
                .uri(path + PATH_VARIABLE, Collections.singletonMap("id", p.getId()))
                .exchange()
                .expectStatus().isNoContent()
        ;

        client
                .get()
                .uri(path + PATH_VARIABLE, Collections.singletonMap("id", p.getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
        ;

    }


}
