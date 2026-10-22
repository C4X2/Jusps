package com.usps.sdk.exception;

import lombok.Getter;

/**
 * Exception thrown when the USPS API returns an HTTP error status code (4xx, 5xx).
 */
@Getter
public class UspsApiException extends UspsException {

    private final int statusCode;
    private final UspsApiError apiError;
    private final String rawResponseBody;

    public UspsApiException(int statusCode, String message, UspsApiError apiError, String rawResponseBody) {
        super(buildMessage(statusCode, message, apiError));
        this.statusCode = statusCode;
        this.apiError = apiError;
        this.rawResponseBody = rawResponseBody;
    }

    public UspsApiException(int statusCode, String message, String rawResponseBody) {
        this(statusCode, message, null, rawResponseBody);
    }

    private static String buildMessage(int statusCode, String message, UspsApiError apiError) {
        StringBuilder sb = new StringBuilder();
        sb.append("USPS API error (status ").append(statusCode).append("): ").append(message);
        if (apiError != null && apiError.getError() != null) {
            if (apiError.getError().getMessage() != null) {
                sb.append(" - ").append(apiError.getError().getMessage());
            }
            if (!apiError.getError().getErrors().isEmpty()) {
                sb.append(" Details: ").append(apiError.getError().getErrors());
            }
        }
        return sb.toString();
    }
}
