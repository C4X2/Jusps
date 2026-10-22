package io.github.c4x2.jusps.exception;

/**
 * Thrown when an API request is forbidden (HTTP 403 Forbidden), such as when an app does not have permission for the requested product.
 */
public class UspsAuthorizationException extends UspsApiException {

    public UspsAuthorizationException(int statusCode, String message, UspsApiError apiError, String rawResponseBody) {
        super(statusCode, message, apiError, rawResponseBody);
    }
}
