package com.usps.sdk.exception;

import lombok.Getter;

import java.time.Duration;

/**
 * Thrown when an API request exceeds rate limits or quota (HTTP 429 Too Many Requests).
 */
@Getter
public class UspsRateLimitException extends UspsApiException {

    private final Duration retryAfter;

    public UspsRateLimitException(int statusCode, String message, UspsApiError apiError, String rawResponseBody, Duration retryAfter) {
        super(statusCode, message, apiError, rawResponseBody);
        this.retryAfter = retryAfter;
    }
}
