package com.usps.sdk.exception;

/**
 * Thrown when serialization of a request object or deserialization of an API response fails.
 */
public class UspsSerializationException extends UspsException {

    public UspsSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
