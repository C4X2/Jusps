package io.github.c4x2.jusps.exception;

/**
 * Thrown when serialization of a request object or deserialization of an API response fails.
 */
public class UspsSerializationException extends UspsException {

    public UspsSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
