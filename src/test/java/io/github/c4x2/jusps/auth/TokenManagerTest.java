package io.github.c4x2.jusps.auth;

import io.github.c4x2.jusps.config.UspsConfig;
import io.github.c4x2.jusps.config.UspsEnvironment;
import io.github.c4x2.jusps.exception.UspsAuthenticationException;
import io.github.c4x2.jusps.http.MockHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenManagerTest {

    private MockHttpClient mockHttpClient;
    private UspsConfig config;
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        mockHttpClient = new MockHttpClient();
        config = UspsConfig.builder()
                .clientId("TEST_KEY")
                .clientSecret("TEST_SECRET")
                .environment(UspsEnvironment.TESTING)
                .httpClient(mockHttpClient)
                .build();

        AuthenticationService authService = new AuthenticationService(config, mockHttpClient);
        tokenManager = new TokenManager(authService);
    }

    @Test
    void shouldFetchAndCacheToken() {
        String tokenJson = """
                {
                  "access_token": "token_12345",
                  "token_type": "Bearer",
                  "expires_in": 1800
                }
                """;
        mockHttpClient.enqueue(200, tokenJson);

        String token1 = tokenManager.getAccessToken();
        String token2 = tokenManager.getAccessToken();

        assertThat(token1).isEqualTo("token_12345");
        assertThat(token2).isEqualTo("token_12345");
        assertThat(mockHttpClient.getRecordedRequests()).hasSize(1);

        var request = mockHttpClient.getLastRequest();
        assertThat(request.uri().toString()).isEqualTo("https://apis-tem.usps.com/oauth2/v3/token");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(mockHttpClient.getLastRequestBody()).contains("grant_type=client_credentials");
        assertThat(mockHttpClient.getLastRequestBody()).contains("client_id=TEST_KEY");
        assertThat(mockHttpClient.getLastRequestBody()).contains("client_secret=TEST_SECRET");
    }

    @Test
    void shouldRefreshExpiredToken() {
        // First token has 0 expiry seconds (immediately expired)
        String expiredTokenJson = """
                {
                  "access_token": "expired_token",
                  "token_type": "Bearer",
                  "expires_in": 0
                }
                """;
        String freshTokenJson = """
                {
                  "access_token": "fresh_token",
                  "token_type": "Bearer",
                  "expires_in": 3600
                }
                """;

        mockHttpClient.enqueue(200, expiredTokenJson);
        mockHttpClient.enqueue(200, freshTokenJson);

        String token1 = tokenManager.getAccessToken();
        assertThat(token1).isEqualTo("expired_token");

        // Second call should detect expiration and fetch fresh token
        String token2 = tokenManager.getAccessToken();
        assertThat(token2).isEqualTo("fresh_token");
        assertThat(mockHttpClient.getRecordedRequests()).hasSize(2);
    }

    @Test
    void shouldHandleAuthenticationFailure() {
        mockHttpClient.enqueue(401, "{\"error\": \"invalid_client\"}");

        assertThatThrownBy(() -> tokenManager.getAccessToken())
                .isInstanceOf(UspsAuthenticationException.class)
                .hasMessageContaining("Authentication failed with status 401");
    }

    @Test
    void shouldInvalidateToken() {
        mockHttpClient.enqueue(200, "{\"access_token\": \"token_a\", \"expires_in\": 3600}");
        mockHttpClient.enqueue(200, "{\"access_token\": \"token_b\", \"expires_in\": 3600}");

        assertThat(tokenManager.getAccessToken()).isEqualTo("token_a");
        tokenManager.invalidateToken();
        assertThat(tokenManager.getAccessToken()).isEqualTo("token_b");
        assertThat(mockHttpClient.getRecordedRequests()).hasSize(2);
    }

    @Test
    void shouldHandleConcurrentRequestsThreadSafely() throws Exception {
        mockHttpClient.enqueue(200, "{\"access_token\": \"concurrent_token\", \"expires_in\": 3600}");

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                return tokenManager.getAccessToken();
            }));
        }

        latch.countDown();

        for (Future<String> future : futures) {
            assertThat(future.get()).isEqualTo("concurrent_token");
        }

        // Only a single network request should have been made
        assertThat(mockHttpClient.getRecordedRequests()).hasSize(1);
        executor.shutdown();
    }
}
