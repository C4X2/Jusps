package io.github.c4x2.jusps.http;

import io.github.c4x2.jusps.auth.TokenManager;
import io.github.c4x2.jusps.config.UspsConfig;
import io.github.c4x2.jusps.exception.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Core HTTP transport layer responsible for dispatching requests, injecting auth headers,
 * executing retries, and translating errors into strongly-typed SDK exceptions.
 */
@Slf4j
public class HttpTransport {

    private final UspsConfig config;
    private final HttpClient httpClient;
    private final TokenManager tokenManager;
    private final RetryPolicy retryPolicy;

    public HttpTransport(UspsConfig config, HttpClient httpClient, TokenManager tokenManager) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.tokenManager = Objects.requireNonNull(tokenManager, "tokenManager cannot be null");
        this.retryPolicy = new RetryPolicy(config.getMaxRetries(), config.getRetryBackoff());
    }

    /**
     * Executes an API request synchronously and deserializes the JSON response body into the given class.
     */
    public <T> T execute(HttpRequestBuilder requestBuilder, Class<T> responseClass) {
        HttpResponse rawResponse = executeRaw(requestBuilder);
        if (responseClass == Void.class || responseClass == null) {
            return null;
        }
        return JsonMapper.fromJson(rawResponse.getBody(), responseClass);
    }

    /**
     * Executes an API request asynchronously and deserializes the JSON response into a CompletableFuture.
     */
    public <T> CompletableFuture<T> executeAsync(HttpRequestBuilder requestBuilder, Class<T> responseClass) {
        return CompletableFuture.supplyAsync(() -> execute(requestBuilder, responseClass));
    }

    /**
     * Executes an API request and returns the raw HttpResponse containing status, headers, and binary/text payload.
     */
    public HttpResponse executeRaw(HttpRequestBuilder requestBuilder) {
        int attempt = 0;
        boolean tokenRetried = false;

        while (true) {
            try {
                HttpRequest request = buildNativeRequest(requestBuilder);
                log.debug("Dispatching USPS API request: {} {} (attempt {})", request.method(), request.uri(), attempt + 1);

                java.net.http.HttpResponse<byte[]> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                int statusCode = response.statusCode();
                byte[] binaryBody = response.body();
                String bodyString = binaryBody != null ? new String(binaryBody, StandardCharsets.UTF_8) : "";

                HttpResponse httpResponse = new HttpResponse(statusCode, response.headers(), bodyString, binaryBody);

                // Handle 401 Unauthorized: token may have been revoked; retry once with a freshly generated token
                if (statusCode == 401 && !tokenRetried && requestBuilder.isRequiresAuth()) {
                    log.warn("Received 401 Unauthorized; invalidating token cache and retrying once...");
                    tokenManager.invalidateToken();
                    tokenRetried = true;
                    attempt++;
                    continue;
                }

                // Check for successful response (2xx)
                if (httpResponse.isSuccessful()) {
                    return httpResponse;
                }

                // Check if we should retry transient 429 or 5xx errors
                if (retryPolicy.shouldRetry(attempt, statusCode)) {
                    String retryAfter = httpResponse.getHeader("Retry-After").orElse(null);
                    Duration delay = retryPolicy.calculateDelay(attempt, retryAfter);
                    log.warn("USPS API responded with status {}. Retrying in {} ms (attempt {}/{})",
                            statusCode, delay.toMillis(), attempt + 1, config.getMaxRetries());
                    retryPolicy.sleep(delay);
                    attempt++;
                    continue;
                }

                // Non-retryable error or retries exhausted; translate to typed SDK exception
                throw translateError(statusCode, bodyString, httpResponse);

            } catch (IOException e) {
                if (retryPolicy.shouldRetryOnException(attempt, e)) {
                    Duration delay = retryPolicy.calculateDelay(attempt, null);
                    log.warn("I/O error during USPS API request: {}. Retrying in {} ms (attempt {}/{})",
                            e.getMessage(), delay.toMillis(), attempt + 1, config.getMaxRetries());
                    try {
                        retryPolicy.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new UspsException("Request retry interrupted", ie);
                    }
                    attempt++;
                    continue;
                }
                throw new UspsException("Network error executing USPS API request: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UspsException("USPS API request interrupted", e);
            }
        }
    }

    private HttpRequest buildNativeRequest(HttpRequestBuilder builder) {
        String fullUrl = config.getEnvironment().getBaseUrl() + builder.getPath() + builder.buildQueryString();
        HttpRequest.Builder nativeBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(config.getReadTimeout())
                .header("User-Agent", config.getUserAgent());

        // Default accept header
        if (!builder.getHeaders().containsKey("Accept")) {
            nativeBuilder.header("Accept", "application/json");
        }

        // Authentication token injection
        if (builder.isRequiresAuth()) {
            String accessToken = tokenManager.getAccessToken();
            nativeBuilder.header("Authorization", "Bearer " + accessToken);
        }

        // Global payment token injection if applicable
        if (config.getPaymentAuthorizationToken() != null && !builder.getHeaders().containsKey("X-Payment-Authorization-Token")) {
            nativeBuilder.header("X-Payment-Authorization-Token", config.getPaymentAuthorizationToken());
        }

        // Custom headers from builder
        for (Map.Entry<String, String> header : builder.getHeaders().entrySet()) {
            nativeBuilder.header(header.getKey(), header.getValue());
        }

        // Request Body & Method
        Object bodyObj = builder.getBody();
        HttpRequest.BodyPublisher bodyPublisher;
        if (bodyObj == null) {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        } else if (bodyObj instanceof String strBody) {
            if (!builder.getHeaders().containsKey("Content-Type")) {
                nativeBuilder.header("Content-Type", "application/json");
            }
            bodyPublisher = HttpRequest.BodyPublishers.ofString(strBody, StandardCharsets.UTF_8);
        } else if (bodyObj instanceof byte[] bytes) {
            bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytes);
        } else {
            if (!builder.getHeaders().containsKey("Content-Type")) {
                nativeBuilder.header("Content-Type", "application/json");
            }
            String json = JsonMapper.toJson(bodyObj);
            bodyPublisher = HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
        }

        switch (builder.getMethod()) {
            case GET -> nativeBuilder.GET();
            case POST -> nativeBuilder.POST(bodyPublisher);
            case PUT -> nativeBuilder.PUT(bodyPublisher);
            case DELETE -> nativeBuilder.DELETE();
            case PATCH -> nativeBuilder.method("PATCH", bodyPublisher);
        }

        return nativeBuilder.build();
    }

    private UspsApiException translateError(int statusCode, String rawBody, HttpResponse response) {
        UspsApiError apiError = null;
        try {
            apiError = JsonMapper.fromJson(rawBody, UspsApiError.class);
        } catch (Exception ignored) {
            // Raw body might not be JSON (e.g. gateway HTML error)
        }

        String message = (apiError != null && apiError.getError() != null && apiError.getError().getMessage() != null)
                ? apiError.getError().getMessage()
                : "HTTP " + statusCode + " received from USPS API";

        return switch (statusCode) {
            case 401 -> new UspsAuthenticationException(statusCode, message, apiError, rawBody);
            case 403 -> new UspsAuthorizationException(statusCode, message, apiError, rawBody);
            case 404 -> new UspsNotFoundException(statusCode, message, apiError, rawBody);
            case 429 -> {
                Duration retryAfter = response.getHeader("Retry-After")
                        .map(val -> {
                            try { return Duration.ofSeconds(Long.parseLong(val.trim())); }
                            catch (Exception e) { return null; }
                        })
                        .orElse(null);
                yield new UspsRateLimitException(statusCode, message, apiError, rawBody, retryAfter);
            }
            default -> {
                if (statusCode >= 500) {
                    yield new UspsServerException(statusCode, message, apiError, rawBody);
                }
                yield new UspsApiException(statusCode, message, apiError, rawBody);
            }
        };
    }
}
