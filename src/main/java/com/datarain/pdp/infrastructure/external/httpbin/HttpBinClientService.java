package com.datarain.pdp.infrastructure.external.httpbin;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HttpBinClientService {

    private final RestTemplate restTemplate;

    public HttpBinClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "httpbin", fallbackMethod = "fallback")
    @Retry(name = "httpbin")
    @TimeLimiter(name = "httpbin")
    public CompletableFuture<String> delay() {
        return CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(
                        "https://httpbin.org/delay/5",
                        String.class
                )
        );
    }

    public CompletableFuture<String> fallback(Throwable ex) {
        log.warn("httpbin failed", ex);
        return CompletableFuture.completedFuture("httpbin unavailable");
    }
}
