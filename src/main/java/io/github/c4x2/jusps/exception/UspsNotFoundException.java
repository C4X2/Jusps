package io.github.c4x2.jusps.exception;

/**
 * Thrown when an API resource is not found (HTTP 404 Not Found), such as an unknown tracking number or non-existent label.
 */
public class UspsNotFoundException extends UspsApiException {

    public UspsNotFoundException(int statusCode, String message, UspsApiError apiError, String rawResponseBody) {
        super(statusCode, message, apiError, rawResponseBody);
    }
}
