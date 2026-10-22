package com.usps.sdk.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.http.HttpHeaders;
import java.util.Optional;

/**
 * Encapsulates raw HTTP response details from a USPS API invocation.
 */
@Getter
@AllArgsConstructor
public class HttpResponse {

    private final int statusCode;
    private final HttpHeaders headers;
    private final String body;
    private final byte[] binaryBody;

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public Optional<String> getHeader(String name) {
        if (headers == null) return Optional.empty();
        return headers.firstValue(name);
    }
}
