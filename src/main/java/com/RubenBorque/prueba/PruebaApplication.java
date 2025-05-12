package com.RubenBorque.prueba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.time.Duration;

@SpringBootApplication
public class PruebaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PruebaApplication.class, args);
	}

	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build())
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.slidingWindowSize(10)
						.failureRateThreshold(50.0f)
						.waitDurationInOpenState(Duration.ofSeconds(10))
						.permittedNumberOfCallsInHalfOpenState(5)
						.build())
				.build());
	}
}