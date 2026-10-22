package com.usps.sdk.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Mock implementation of java.net.http.HttpClient for hermetic, offline unit testing.
 */
public class MockHttpClient extends HttpClient {

    private final Queue<MockResponseSpec> responseQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Function<HttpRequest, MockResponseSpec>> routeHandlers = new ConcurrentHashMap<>();
    private final List<HttpRequest> recordedRequests = Collections.synchronizedList(new ArrayList<>());
    private final List<String> recordedRequestBodies = Collections.synchronizedList(new ArrayList<>());

    public static class MockResponseSpec {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;

        public MockResponseSpec(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body != null ? body : "";
            this.headers = headers != null ? headers : new HashMap<>();
        }

        public static MockResponseSpec of(int statusCode, String body) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("content-type", List.of("application/json"));
            return new MockResponseSpec(statusCode, body, headers);
        }

        public static MockResponseSpec of(int statusCode, String body, String headerKey, String headerVal) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("content-type", List.of("application/json"));
            headers.put(headerKey.toLowerCase(), List.of(headerVal));
            return new MockResponseSpec(statusCode, body, headers);
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
        public Map<String, List<String>> getHeaders() { return headers; }
    }

    public MockHttpClient enqueue(int statusCode, String body) {
        responseQueue.add(MockResponseSpec.of(statusCode, body));
        return this;
    }

    public MockHttpClient enqueue(MockResponseSpec spec) {
        responseQueue.add(spec);
        return this;
    }

    public MockHttpClient onRoute(String pathSubstring, Function<HttpRequest, MockResponseSpec> handler) {
        routeHandlers.put(pathSubstring, handler);
        return this;
    }

    public List<HttpRequest> getRecordedRequests() {
        return Collections.unmodifiableList(recordedRequests);
    }

    public List<String> getRecordedRequestBodies() {
        return Collections.unmodifiableList(recordedRequestBodies);
    }

    public HttpRequest getLastRequest() {
        if (recordedRequests.isEmpty()) return null;
        return recordedRequests.get(recordedRequests.size() - 1);
    }

    public String getLastRequestBody() {
        if (recordedRequestBodies.isEmpty()) return null;
        return recordedRequestBodies.get(recordedRequestBodies.size() - 1);
    }

    public void reset() {
        responseQueue.clear();
        routeHandlers.clear();
        recordedRequests.clear();
        recordedRequestBodies.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler)
            throws IOException, InterruptedException {
        recordedRequests.add(request);
        String bodyStr = extractBody(request);
        recordedRequestBodies.add(bodyStr);

        MockResponseSpec spec = null;
        String uriStr = request.uri().toString();
        for (Map.Entry<String, Function<HttpRequest, MockResponseSpec>> entry : routeHandlers.entrySet()) {
            if (uriStr.contains(entry.getKey())) {
                spec = entry.getValue().apply(request);
                break;
            }
        }

        if (spec == null) {
            spec = responseQueue.poll();
        }

        if (spec == null) {
            spec = MockResponseSpec.of(200, "{}");
        }

        return createMockResponse(request, spec, responseHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler) {
        try {
            return CompletableFuture.completedFuture(send(request, responseHandler));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return sendAsync(request, responseHandler);
    }

    @SuppressWarnings("unchecked")
    private <T> HttpResponse<T> createMockResponse(HttpRequest request, MockResponseSpec spec, HttpResponse.BodyHandler<T> handler) {
        final byte[] bytes = spec.getBody().getBytes(StandardCharsets.UTF_8);
        final HttpHeaders httpHeaders = HttpHeaders.of(spec.getHeaders(), (k, v) -> true);

        HttpResponse.ResponseInfo responseInfo = new HttpResponse.ResponseInfo() {
            @Override public int statusCode() { return spec.getStatusCode(); }
            @Override public HttpHeaders headers() { return httpHeaders; }
            @Override public Version version() { return Version.HTTP_2; }
        };

        HttpResponse.BodySubscriber<T> subscriber = handler.apply(responseInfo);
        subscriber.onSubscribe(new java.util.concurrent.Flow.Subscription() {
            @Override public void request(long n) {}
            @Override public void cancel() {}
        });
        subscriber.onNext(List.of(ByteBuffer.wrap(bytes)));
        subscriber.onComplete();

        T bodyObj;
        try {
            bodyObj = subscriber.getBody().toCompletableFuture().get();
        } catch (Exception e) {
            bodyObj = (T) bytes;
        }

        final T finalBody = bodyObj;
        return new HttpResponse<>() {
            @Override public int statusCode() { return spec.getStatusCode(); }
            @Override public HttpRequest request() { return request; }
            @Override public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
            @Override public HttpHeaders headers() { return httpHeaders; }
            @Override public T body() { return finalBody; }
            @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
            @Override public URI uri() { return request.uri(); }
            @Override public Version version() { return Version.HTTP_2; }
        };
    }

    private static String extractBody(HttpRequest request) {
        if (request.bodyPublisher().isEmpty()) return "";
        var subscriber = new BodyCaptureSubscriber();
        request.bodyPublisher().get().subscribe(subscriber);
        return subscriber.getBody();
    }

    private static class BodyCaptureSubscriber implements java.util.concurrent.Flow.Subscriber<ByteBuffer> {
        private final List<byte[]> chunks = new ArrayList<>();
        private final CompletableFuture<String> future = new CompletableFuture<>();

        @Override public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override public void onNext(ByteBuffer item) {
            byte[] arr = new byte[item.remaining()];
            item.get(arr);
            chunks.add(arr);
        }

        @Override public void onError(Throwable throwable) {
            future.completeExceptionally(throwable);
        }

        @Override public void onComplete() {
            int total = chunks.stream().mapToInt(c -> c.length).sum();
            byte[] merged = new byte[total];
            int pos = 0;
            for (byte[] c : chunks) {
                System.arraycopy(c, 0, merged, pos, c.length);
                pos += c.length;
            }
            future.complete(new String(merged, StandardCharsets.UTF_8));
        }

        public String getBody() {
            try { return future.get(); } catch (Exception e) { return ""; }
        }
    }

    // Required abstract method stubs
    @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
    @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
    @Override public Redirect followRedirects() { return Redirect.NEVER; }
    @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
    @Override public SSLContext sslContext() { return null; }
    @Override public SSLParameters sslParameters() { return null; }
    @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
    @Override public Version version() { return Version.HTTP_2; }
    @Override public Optional<Executor> executor() { return Optional.empty(); }
}
