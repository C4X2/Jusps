package com.usps.sdk.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

/**
 * Represents an OAuth 2.0 access token issued by USPS.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthToken {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn; // in seconds, typically 1799 (30 minutes)

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private String scope;

    @Builder.Default
    private Instant issuedAt = Instant.now();

    public OAuthToken(String accessToken, String tokenType, long expiresIn, String refreshToken, String scope) {
        this(accessToken, tokenType, expiresIn, refreshToken, scope, Instant.now());
    }

    /**
     * Checks if the token is expired, applying a safety margin (buffer) in seconds.
     *
     * @param bufferSeconds safety buffer before actual expiration
     * @return true if expired or within safety buffer
     */
    public boolean isExpired(long bufferSeconds) {
        if (accessToken == null || issuedAt == null) {
            return true;
        }
        Instant expirationTime = issuedAt.plusSeconds(expiresIn);
        return Instant.now().isAfter(expirationTime.minusSeconds(bufferSeconds));
    }

    public boolean isExpired() {
        return isExpired(60); // default 60-second safety window
    }

    @Override
    public String toString() {
        return "OAuthToken{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", issuedAt=" + issuedAt +
                '}';
    }
}
