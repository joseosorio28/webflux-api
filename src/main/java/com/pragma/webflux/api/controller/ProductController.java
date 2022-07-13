package com.pragma.webflux.api.controller;


import com.pragma.webflux.api.model.Category;
import com.pragma.webflux.api.model.Image;
import com.pragma.webflux.api.model.Product;
import com.pragma.webflux.api.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Controller
@SessionAttributes("product")
public class ProductController {

    @Value("${images.upload.path}")
    private String imagesPath;

    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
    private static final String TITLE = "title";
    private static final String PRODUCTS = "products";
    private static final String BUTTON = "button";

    private static final String LIST_PRODUCTS = "List of products";
    private static final String FORMAT = "Product: %s";

    @Autowired
    private IProductService productService;

    @ModelAttribute("categories")
    public Flux<Category> categories() {
        return productService.findAllCategories();
    }

    @GetMapping({"/list", "/"})
    public Mono<String> list(Model model) {
        Flux<Product> products = productService.findAllWithNameUppercase();
        products.subscribe(product -> LOG.info(String.format(FORMAT, product.getName())));
        model.addAttribute(PRODUCTS, products);
        model.addAttribute(TITLE, LIST_PRODUCTS);
        return Mono.just("list");
    }

    @GetMapping("/form")
    public Mono<String> create(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute(TITLE, "Product form");
        model.addAttribute(BUTTON, "Create");
        return Mono.just("form");
    }

    @PostMapping("/form")
    public Mono<String> save(
            @Valid Product product,
            BindingResult result,
            Model model,
            @RequestPart FilePart file,
            SessionStatus sessionStatus) {

        if (result.hasErrors()) {
            model.addAttribute(TITLE, "ERROR");
            model.addAttribute(BUTTON, "Create");
            return Mono.just("form");
        } else {
            sessionStatus.setComplete();

            Mono<Category> category = productService.findCategoryById(product.getCategory().getId());
            String filename = UUID.randomUUID().toString() + "-" + file.filename()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("\\", "");
            return category
                    .flatMap(c -> {
                        if (product.getCreateAt() == null) {
                            product.setCreateAt(new Date());
                        }
                        if (!file.filename().isEmpty()) {
                            Image image = new Image(filename);
                            product.getImages().add(image);
                            //productService.saveImage(image);
                        }
                        product.setCategory(c);
                        return productService.save(product);
                    })
                    .doOnNext(p -> LOG.info("Saved: " + p))
                    .flatMap(p->{
                        if (!file.filename().isEmpty()) {
                            return file.transferTo(new File(imagesPath+ filename));
                        }
                        return Mono.empty();
                    })
                    .thenReturn("redirect:/list?success=saved+product")
                    ;
        }
    }

    @GetMapping("/form/{id}")
    public Mono<String> edit(
            @PathVariable String id,
            Model model) {
        return productService.findById(id)
                .doOnNext(p -> {
                    model.addAttribute(TITLE, "Edit product");
                    model.addAttribute("product", p);
                    model.addAttribute(BUTTON, "Edit");
                    LOG.info("Product to update: " + p.getName() + ", Id:" + p.getId());
                })
                .defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("Does not exist"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/list?error=Product+doesn't+exist"));
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(
            @PathVariable String id,
            Model model) {
        return productService.findById(id)
                .defaultIfEmpty(new Product())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("Does not exist"));
                    }
                    return Mono.just(p);
                })
                .flatMap(productService::delete)
                .then(Mono.just("redirect:/list?success=deleted+product"))
                .onErrorResume(ex -> Mono.just("redirect:/list?error=Product+doesn't+exist"));
    }

    @GetMapping({"/list-datadriver"})
    public String listDataDriver(Model model) {
        Flux<Product> products = productService.findAllWithNameUppercase()
                .delayElements(Duration.ofSeconds(1));
        products.subscribe(product -> LOG.info(String.format(FORMAT, product.getName())));
        //model.addAttribute(PRODUCTS, new ReactiveDataDriverContextVariable(products, 2));
        model.addAttribute(TITLE, LIST_PRODUCTS);
        return "list";

    }

    @GetMapping({"/full-list"})
    public String fullList(Model model) {
        Flux<Product> products = productService.findAllWithNameUppercaseRepeat();
        products.subscribe(product -> LOG.info(String.format(FORMAT, product.getName())));
        //model.addAttribute(PRODUCTS, new ReactiveDataDriverContextVariable(products, 2));
        model.addAttribute(TITLE, LIST_PRODUCTS);
        return "list";
    }

    @GetMapping({"/full-chunked"})
    public String fullListChunked(Model model) {
        Flux<Product> products = productService.findAllWithNameUppercaseRepeat();
        products.subscribe(product -> LOG.info(String.format(FORMAT, product.getName())));
        //model.addAttribute(PRODUCTS, new ReactiveDataDriverContextVariable(products, 2));
        model.addAttribute(TITLE, LIST_PRODUCTS);
        return "list-chunked";

    }

}
