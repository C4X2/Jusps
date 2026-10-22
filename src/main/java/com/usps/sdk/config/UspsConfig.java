package com.usps.sdk.config;

import com.usps.sdk.exception.UspsValidationException;
import lombok.Getter;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Immutable configuration for the USPS SDK client.
 */
@Getter
public final class UspsConfig {

    private final String clientId;
    private final String clientSecret;
    private final UspsEnvironment environment;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final int maxRetries;
    private final Duration retryBackoff;
    private final HttpClient httpClient;
    private final String paymentAuthorizationToken;
    private final String crid;
    private final String mid;
    private final String userAgent;

    private UspsConfig(Builder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.environment = builder.environment != null ? builder.environment : UspsEnvironment.PRODUCTION;
        this.connectTimeout = builder.connectTimeout != null ? builder.connectTimeout : Duration.ofSeconds(15);
        this.readTimeout = builder.readTimeout != null ? builder.readTimeout : Duration.ofSeconds(30);
        this.maxRetries = builder.maxRetries >= 0 ? builder.maxRetries : 3;
        this.retryBackoff = builder.retryBackoff != null ? builder.retryBackoff : Duration.ofMillis(500);
        this.httpClient = builder.httpClient;
        this.paymentAuthorizationToken = builder.paymentAuthorizationToken;
        this.crid = builder.crid;
        this.mid = builder.mid;
        this.userAgent = builder.userAgent != null ? builder.userAgent : "usps-java-sdk/1.0.0 (Java " + System.getProperty("java.version") + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clientId;
        private String clientSecret;
        private UspsEnvironment environment = UspsEnvironment.PRODUCTION;
        private Duration connectTimeout = Duration.ofSeconds(15);
        private Duration readTimeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Duration retryBackoff = Duration.ofMillis(500);
        private HttpClient httpClient;
        private String paymentAuthorizationToken;
        private String crid;
        private String mid;
        private String userAgent;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder environment(UspsEnvironment environment) {
            this.environment = environment;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryBackoff(Duration retryBackoff) {
            this.retryBackoff = retryBackoff;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder paymentAuthorizationToken(String paymentAuthorizationToken) {
            this.paymentAuthorizationToken = paymentAuthorizationToken;
            return this;
        }

        public Builder crid(String crid) {
            this.crid = crid;
            return this;
        }

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public UspsConfig build() {
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new UspsValidationException("clientId must not be null or blank");
            }
            if (clientSecret == null || clientSecret.trim().isEmpty()) {
                throw new UspsValidationException("clientSecret must not be null or blank");
            }
            return new UspsConfig(this);
        }

        public com.usps.sdk.UspsClient buildClient() {
            return new com.usps.sdk.UspsClient(build());
        }
    }
}
