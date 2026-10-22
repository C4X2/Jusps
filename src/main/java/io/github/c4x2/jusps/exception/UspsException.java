package io.github.c4x2.jusps.exception;

/**
 * Base unchecked exception for all errors thrown by the USPS Java SDK.
 */
public class UspsException extends RuntimeException {

    public UspsException(String message) {
        super(message);
    }

    public UspsException(String message, Throwable cause) {
        super(message, cause);
    }
}
