package com.usps.sdk.http;

import com.usps.sdk.auth.AuthenticationService;
import com.usps.sdk.auth.TokenManager;
import com.usps.sdk.config.UspsConfig;
import com.usps.sdk.config.UspsEnvironment;
import com.usps.sdk.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpTransportTest {

    private MockHttpClient mockHttpClient;
    private UspsConfig config;
    private TokenManager tokenManager;
    private HttpTransport transport;

    @BeforeEach
    void setUp() {
        mockHttpClient = new MockHttpClient();
        config = UspsConfig.builder()
                .clientId("TEST_KEY")
                .clientSecret("TEST_SECRET")
                .environment(UspsEnvironment.TESTING)
                .httpClient(mockHttpClient)
                .maxRetries(2)
                .retryBackoff(Duration.ofMillis(10))
                .build();

        AuthenticationService authService = new AuthenticationService(config, mockHttpClient);
        tokenManager = new TokenManager(authService);
        transport = new HttpTransport(config, mockHttpClient, tokenManager);

        mockHttpClient.onRoute("/oauth2/v3/token", req ->
                MockHttpClient.MockResponseSpec.of(200, "{\"access_token\":\"mock_token\",\"expires_in\":3600}"));
    }

    @Test
    void shouldTranslate403Forbidden() {
        mockHttpClient.enqueue(403, "{\"error\":{\"code\":\"403\",\"message\":\"Access forbidden\"}}");

        assertThatThrownBy(() -> transport.execute(HttpRequestBuilder.get("/test"), String.class))
                .isInstanceOf(UspsAuthorizationException.class)
                .hasMessageContaining("Access forbidden");
    }

    @Test
    void shouldTranslate429RateLimitWithRetryAfterHeader() {
        UspsConfig noRetryConfig = UspsConfig.builder()
                .clientId("TEST_KEY")
                .clientSecret("TEST_SECRET")
                .environment(UspsEnvironment.TESTING)
                .httpClient(mockHttpClient)
                .maxRetries(0)
                .build();
        HttpTransport noRetryTransport = new HttpTransport(noRetryConfig, mockHttpClient, tokenManager);

        mockHttpClient.enqueue(MockHttpClient.MockResponseSpec.of(429, "{\"error\":{\"message\":\"Rate limit exceeded\"}}", "Retry-After", "15"));

        assertThatThrownBy(() -> noRetryTransport.execute(HttpRequestBuilder.get("/test"), String.class))
                .isInstanceOf(UspsRateLimitException.class)
                .satisfies(e -> {
                    UspsRateLimitException rle = (UspsRateLimitException) e;
                    assertThat(rle.getRetryAfter()).isEqualTo(Duration.ofSeconds(15));
                });
    }

    @Test
    void shouldRetryOnTransientServerErrorAndSucceed() {
        // Enqueue 503 then 200
        mockHttpClient.enqueue(503, "{\"error\":{\"message\":\"Service unavailable\"}}");
        mockHttpClient.enqueue(200, "{\"status\":\"ok\"}");

        HttpResponse response = transport.executeRaw(HttpRequestBuilder.get("/test"));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"ok\"");
    }

    @Test
    void shouldRecoverFrom401ByRefreshingToken() {
        // First request to /test returns 401 Unauthorized
        mockHttpClient.enqueue(401, "{\"error\":{\"message\":\"Token expired\"}}");
        // Next request after re-authentication returns 200
        mockHttpClient.enqueue(200, "{\"status\":\"recovered\"}");

        HttpResponse response = transport.executeRaw(HttpRequestBuilder.get("/test"));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"status\":\"recovered\"");
    }
}
