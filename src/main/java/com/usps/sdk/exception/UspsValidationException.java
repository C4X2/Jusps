package com.usps.sdk.exception;

/**
 * Thrown when client-side parameter or configuration validation fails before an API request is made.
 */
public class UspsValidationException extends UspsException {

    public UspsValidationException(String message) {
        super(message);
    }

    public UspsValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
