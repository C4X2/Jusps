package com.usps.sdk.http;

import lombok.Getter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Fluent request descriptor for building USPS API requests.
 */
@Getter
public final class HttpRequestBuilder {

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, String> queryParams = new LinkedHashMap<>();
    private Object body;
    private boolean requiresAuth = true;

    private HttpRequestBuilder(HttpMethod method, String path) {
        this.method = Objects.requireNonNull(method, "method cannot be null");
        this.path = Objects.requireNonNull(path, "path cannot be null");
    }

    public static HttpRequestBuilder get(String path) {
        return new HttpRequestBuilder(HttpMethod.GET, path);
    }

    public static HttpRequestBuilder post(String path) {
        return new HttpRequestBuilder(HttpMethod.POST, path);
    }

    public static HttpRequestBuilder put(String path) {
        return new HttpRequestBuilder(HttpMethod.PUT, path);
    }

    public static HttpRequestBuilder delete(String path) {
        return new HttpRequestBuilder(HttpMethod.DELETE, path);
    }

    public HttpRequestBuilder header(String name, String value) {
        if (name != null && value != null) {
            headers.put(name, value);
        }
        return this;
    }

    public HttpRequestBuilder queryParam(String name, Object value) {
        if (name != null && value != null) {
            String strVal = String.valueOf(value);
            if (!strVal.isEmpty()) {
                queryParams.put(name, strVal);
            }
        }
        return this;
    }

    public HttpRequestBuilder body(Object body) {
        this.body = body;
        return this;
    }

    public HttpRequestBuilder requiresAuth(boolean requiresAuth) {
        this.requiresAuth = requiresAuth;
        return this;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public String buildQueryString() {
        if (queryParams.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (first) {
                sb.append("?");
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
