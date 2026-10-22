package io.github.c4x2.jusps.auth;

import io.github.c4x2.jusps.config.UspsConfig;
import io.github.c4x2.jusps.exception.UspsAuthenticationException;
import io.github.c4x2.jusps.exception.UspsException;
import io.github.c4x2.jusps.http.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Low-level authentication service for requesting OAuth 2.0 tokens from the USPS API.
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String TOKEN_PATH = "/oauth2/v3/token";

    private final UspsConfig config;
    private final HttpClient httpClient;

    /**
     * Requests a new OAuth 2.0 token using the client_credentials grant type.
     *
     * @return the newly issued OAuthToken
     */
    public OAuthToken requestToken() {
        String tokenUrl = config.getEnvironment().getBaseUrl() + TOKEN_PATH;
        log.debug("Requesting OAuth 2.0 token from {}", tokenUrl);

        String formBody = "grant_type=client_credentials" +
                "&client_id=" + urlEncode(config.getClientId()) +
                "&client_secret=" + urlEncode(config.getClientSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("User-Agent", config.getUserAgent())
                .timeout(config.getReadTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(formBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            String responseBody = response.body();

            if (status >= 200 && status < 300) {
                OAuthToken token = JsonMapper.fromJson(responseBody, OAuthToken.class);
                if (token == null || token.getAccessToken() == null) {
                    throw new UspsAuthenticationException(status, "Received empty access token from USPS token endpoint", null, responseBody);
                }
                log.debug("Successfully acquired OAuth 2.0 token, expires in {}s", token.getExpiresIn());
                return token;
            } else {
                throw new UspsAuthenticationException(status, "Authentication failed with status " + status, null, responseBody);
            }
        } catch (IOException e) {
            throw new UspsException("Network error requesting OAuth token: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UspsException("OAuth token request interrupted", e);
        }
    }

    private static String urlEncode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
