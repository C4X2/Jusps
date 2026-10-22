package com.usps.sdk.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe manager responsible for caching, validating, and automatically refreshing OAuth 2.0 access tokens.
 */
@Slf4j
@RequiredArgsConstructor
public class TokenManager {

    private final AuthenticationService authService;
    private final ReentrantLock lock = new ReentrantLock();

    @Getter
    private volatile OAuthToken currentToken;

    /**
     * Gets a valid OAuth 2.0 access token string. Refreshes if expired or uninitialized.
     *
     * @return valid Bearer token string
     */
    public String getAccessToken() {
        OAuthToken token = currentToken;
        if (token != null && !token.isExpired()) {
            return token.getAccessToken();
        }

        lock.lock();
        try {
            // Double check within lock
            token = currentToken;
            if (token != null && !token.isExpired()) {
                return token.getAccessToken();
            }

            log.info("OAuth token is absent or expired, refreshing...");
            currentToken = authService.requestToken();
            return currentToken.getAccessToken();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Forces immediate invalidation of the current token (e.g. if a 401 is received from an API call).
     */
    public void invalidateToken() {
        lock.lock();
        try {
            currentToken = null;
        } finally {
            lock.unlock();
        }
    }
}
