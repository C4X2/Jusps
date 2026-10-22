package com.usps.sdk.exception;

/**
 * Thrown when USPS returns a server-side error (HTTP 500, 502, 503, 504).
 */
public class UspsServerException extends UspsApiException {

    public UspsServerException(int statusCode, String message, UspsApiError apiError, String rawResponseBody) {
        super(statusCode, message, apiError, rawResponseBody);
    }
}
