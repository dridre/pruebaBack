package com.RubenBorque.prueba.client;

import com.RubenBorque.prueba.model.ProductDetailDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
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
                .onErrorResume(error -> {
                    logger.error("Falling back to empty list for similar IDs due to: {}", error.getMessage());
                    return Mono.just(List.of());
                });
    }

    public Mono<List<String>> getEmptySimilarIds(String productId, Throwable t) {
        logger.warn("Circuit breaker triggered for getSimilarIds. Returning empty list. Error: {}", t.getMessage());
        return Mono.just(List.of());
    }

    @CircuitBreaker(name = "getProductDetail", fallbackMethod = "getProductDetailFallback")
    public Mono<ProductDetailDTO> getProductDetail(String productId) {
        logger.info("Getting product detail for product: {}", productId);
        return webClient.get()
                .uri(productDetailPath, productId)
                .retrieve()
                .bodyToMono(ProductDetailDTO.class)
                .doOnError(error -> logger.error("Error getting product detail: {}", error.getMessage()));
    }

    public Mono<ProductDetailDTO> getProductDetailFallback(String productId, Throwable t) {
        logger.warn("Circuit breaker triggered for getProductDetail. Error: {}", t.getMessage());
        // Throw a 404 if the product doesn't exist
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or service unavailable");
    }
}