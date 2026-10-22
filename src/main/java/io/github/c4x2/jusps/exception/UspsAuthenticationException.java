package io.github.c4x2.jusps.exception;

/**
 * Thrown when API requests fail authentication (HTTP 401 Unauthorized), such as invalid client credentials or an expired token.
 */
public class UspsAuthenticationException extends UspsApiException {

    public UspsAuthenticationException(int statusCode, String message, UspsApiError apiError, String rawResponseBody) {
        super(statusCode, message, apiError, rawResponseBody);
    }
}
