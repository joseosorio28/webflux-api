package com.pragma.webflux.api;

import com.pragma.webflux.api.model.Category;
import com.pragma.webflux.api.model.Product;
import com.pragma.webflux.api.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringWebfluxApiRestApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringWebfluxApiRestApplication.class);

	@Autowired
	private IProductService productService;
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxApiRestApplication.class, args);
	}

	@Override
	public void run(String... args) {

		mongoTemplate.dropCollection("products").subscribe();
		mongoTemplate.dropCollection("categories").subscribe();

		Category electronics = new Category("Electronics");
		Category sports = new Category("Sports");
		Category computing = new Category("Computing");
		Category furniture = new Category("Furniture");

		Flux
				.just(electronics, sports, computing, furniture)
				.flatMap(productService::saveCategory)
				.doOnNext(c -> LOG.info(String.format("Insert: %s", c)))
				.thenMany(
						Flux
								.just(
										new Product("TV Panasonic Pantalla LCD", 456.89, electronics),
										new Product("Sony Camara HD Digital", 177.89, electronics),
										new Product("Apple iPod", 46.89, electronics),
										new Product("Sony Notebook", 846.89, computing),
										new Product("Hewlett Packard Multifuncional", 200.89, computing),
										new Product("Bianchi Bicicleta", 70.89, sports),
										new Product("HP Notebook Omen 17", 2500.89, computing),
										new Product("Mica Cómoda 5 Cajones", 150.89, furniture),
										new Product("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronics))
								.flatMap(product -> {
									product.setCreateAt(new Date());
									return productService.save(product);
								})
				)
				.subscribe(product -> LOG.info(String.format("Insert: %s", product)));
	}

}
