package com.pragma.webflux.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    @Value("${api.endpoint}")
    private String path;
    private static final String PATH_VARIABLE="{id}";

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return route(GET(path),serverRequest->handler.list())
                .andRoute(GET(path + PATH_VARIABLE), handler::getProduct)
                .andRoute(POST(path), handler::createProduct)
                .andRoute(PUT(path + PATH_VARIABLE), handler::editProduct2)
                .andRoute(DELETE(path + PATH_VARIABLE), handler::deleteProduct)
                .andRoute(POST(path+ "upload/"+PATH_VARIABLE), handler::uploadImage)
                .andRoute(POST(path+"uploadWithImage"), handler::createWithImage)
                ;
    }
}
