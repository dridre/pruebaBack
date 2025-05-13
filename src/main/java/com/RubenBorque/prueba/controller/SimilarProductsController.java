package com.RubenBorque.prueba.controller;

import com.RubenBorque.prueba.model.ProductDetail;
import com.RubenBorque.prueba.service.SimilarProductsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class SimilarProductsController {
    private static final Logger logger = LoggerFactory.getLogger(SimilarProductsController.class);

    private final SimilarProductsService similarProductsService;

    public SimilarProductsController(SimilarProductsService similarProductsService) {
        this.similarProductsService = similarProductsService;
    }

    @GetMapping("/product/{productId}/similar")
    public Mono<ResponseEntity<List<ProductDetail>>> getSimilarProducts(@PathVariable String productId) {
        logger.info("Request received for similar products of productId: {}", productId);

        return similarProductsService.getSimilarProducts(productId)
                .map(ResponseEntity::ok);
    }
}