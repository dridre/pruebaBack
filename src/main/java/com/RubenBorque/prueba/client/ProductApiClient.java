package com.RubenBorque.prueba.client;

import com.RubenBorque.prueba.exception.ExternalServiceException;
import com.RubenBorque.prueba.exception.ProductNotFoundException;
import com.RubenBorque.prueba.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ProductApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ProductApiClient.class);

    private final WebClient webClient;

    @Value("${external.api.similar-ids-path}")
    private String similarIdsPath;

    @Value("${external.api.product-detail-path}")
    private String productDetailPath;

    public ProductApiClient(@Value("${external.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @CircuitBreaker(name = "getSimilarIds", fallbackMethod = "getEmptySimilarIds")
    public Mono<List<String>> getSimilarProductIds(String productId) {
        logger.info("Getting similar product IDs for product: {}", productId);
        return webClient.get()
                .uri(similarIdsPath, productId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .doOnError(error -> logger.error("Error getting similar product IDs: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, error -> {
                    logger.error("Product not found: {}", productId);
                    return Mono.error(new ProductNotFoundException("Product with ID " + productId + " not found"));
                })
                .onErrorResume(error -> {
                    if (!(error instanceof ProductNotFoundException)) {
                        logger.error("External service error when getting similar IDs: {}", error.getMessage());
                        return Mono.error(new ExternalServiceException("Error fetching similar products: " + error.getMessage()));
                    }
                    return Mono.error(error);
                });
    }

    public Mono<List<String>> getEmptySimilarIds(String productId, Throwable t) {
        logger.warn("Circuit breaker triggered for getSimilarIds. Returning empty list. Error: {}", t.getMessage());
        if (t instanceof ProductNotFoundException) {
            return Mono.error(t);
        }
        return Mono.just(List.of());
    }

    @CircuitBreaker(name = "getProductDetail", fallbackMethod = "getProductDetailFallback")
    public Mono<ProductDetail> getProductDetail(String productId) {
        logger.info("Getting product detail for product: {}", productId);
        return webClient.get()
                .uri(productDetailPath, productId)
                .retrieve()
                .bodyToMono(ProductDetail.class)
                .doOnError(error -> logger.error("Error getting product detail: {}", error.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, error -> {
                    logger.error("Product detail not found: {}", productId);
                    return Mono.error(new ProductNotFoundException("Product with ID " + productId + " not found"));
                })
                .onErrorResume(error -> {
                    if (!(error instanceof ProductNotFoundException)) {
                        logger.error("External service error when getting product detail: {}", error.getMessage());
                        return Mono.error(new ExternalServiceException("Error fetching product details: " + error.getMessage()));
                    }
                    return Mono.error(error);
                });
    }

    public Mono<ProductDetail> getProductDetailFallback(String productId, Throwable t) {
        logger.warn("Circuit breaker triggered for getProductDetail. Error: {}", t.getMessage());
        // Propagate the specific exception
        return Mono.error(t);
    }
}