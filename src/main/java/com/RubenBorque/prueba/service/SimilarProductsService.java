package com.RubenBorque.prueba.service;

import com.RubenBorque.prueba.client.ProductApiClient;
import com.RubenBorque.prueba.model.ProductDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SimilarProductsService {
    private static final Logger logger = LoggerFactory.getLogger(SimilarProductsService.class);

    private final ProductApiClient productApiClient;

    public SimilarProductsService(ProductApiClient productApiClient) {
        this.productApiClient = productApiClient;
    }

    public Mono<List<ProductDetail>> getSimilarProducts(String productId) {
        logger.info("Getting similar products for product: {}", productId);

        // First check if the product exists
        return productApiClient.getProductDetail(productId)
                .flatMap(productDetail -> {
                    // Get similar product IDs
                    return productApiClient.getSimilarProductIds(productId)
                            .flatMapMany(Flux::fromIterable)
                            // Get details for each similar product ID
                            .flatMap(this::getProductDetailSafely)
                            // Collect results into a list
                            .collectList();
                });
    }

    private Mono<ProductDetail> getProductDetailSafely(String productId) {
        return productApiClient.getProductDetail(productId)
                .onErrorResume(error -> {
                    logger.warn("Error getting product detail for ID {}: {}", productId, error.getMessage());
                    return Mono.empty();
                });
    }
}